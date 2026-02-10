package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import de.swiftbyte.jdaboot.exceptions.VariableCycleException;
import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import lombok.CustomLog;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The VariableProcessor class is responsible for processing variables in a given string.
 * It replaces placeholders in the string with the corresponding values from a provided map of variables.
 * It also supports default variables, which are replaced in the string if they are present.
 *
 * @since alpha.4
 */
@CustomLog
public class VariableProcessor {
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile(Pattern.quote("#{") + "(.*?)" + Pattern.quote("}"));
    private static final Pattern CONFIG_PATTERN = Pattern.compile(Pattern.quote("?{") + "(.*?)" + Pattern.quote("}"));
    private static final Pattern VARIABLE_PATTERN = Pattern.compile(Pattern.quote("${") + "(.*?)" + Pattern.quote("}"));
    private static final int MAX_PROCESSING_PASSES = 64;

    /**
     * Processes the variables in the given string using the provided locale, variable map, and default variables.
     * Replaces placeholders in the string with the corresponding values.
     *
     * @param locale          The locale to use for processing the variables.
     * @param old             The original string with placeholders.
     * @param variables       The map of variables to replace in the string.
     * @param defaultVariable The array of default variables to replace in the string.
     * @return The processed string with placeholders replaced by variable values.
     * @since alpha.4
     */
    public static @NonNull String processVariable(@NonNull DiscordLocale locale, @NonNull String old, @NonNull Map<@NonNull String, @NonNull String> variables, @NonNull DefaultVariable @NonNull [] defaultVariable) {
        return processVariable(locale, old, variables, toDefaultMap(defaultVariable));
    }

    public static @NonNull String processVariable(@NonNull DiscordLocale locale, @NonNull String old, @NonNull Map<@NonNull String, @NonNull String> variables, @NonNull XmlDefaultVariable @NonNull [] defaultVariable) {
        return processVariable(locale, old, variables, toDefaultMap(defaultVariable));
    }

    private static @NonNull String processVariable(@NonNull DiscordLocale locale, @NonNull String old, @NonNull Map<@NonNull String, @NonNull String> variables, @NonNull Map<@NonNull String, @NonNull String> defaultVariables) {
        return processVariableInternal(locale, old, variables, defaultVariables, new LinkedHashSet<>());
    }

    private static @NonNull String processVariableInternal(@Nullable DiscordLocale locale, @NonNull String old, @NonNull Map<@NonNull String, @NonNull String> variables, @NonNull Map<@NonNull String, @NonNull String> defaultVariables, @NonNull Set<@NonNull String> unknownVariables) {
        String newText = old;
        HashMap<String, Integer> seenStateIndexes = new HashMap<>();
        List<String> stateHistory = new ArrayList<>();

        for (int pass = 0; pass < MAX_PROCESSING_PASSES; pass++) {
            boolean changed = false;

            if (locale != null) {
                String translatedText = TranslationProcessor.processTranslation(locale, newText);
                if (!translatedText.equals(newText)) {
                    changed = true;
                    newText = translatedText;
                }
            }

            ReplacementResult variableResult = replaceVariables(newText, variables, defaultVariables, unknownVariables);
            newText = variableResult.text;
            changed = changed || variableResult.changed;

            ReplacementResult configResult = replaceConfigValues(newText, unknownVariables);
            newText = configResult.text;
            changed = changed || configResult.changed;

            if (!isIncompletelyProcessed(newText, locale != null, unknownVariables)) {
                return newText;
            }

            if (!changed) {
                return newText;
            }

            Integer firstSeenStateIndex = seenStateIndexes.putIfAbsent(newText, stateHistory.size());
            if (firstSeenStateIndex != null) {
                throw new VariableCycleException(
                        "Detected cyclic variable references while processing placeholders",
                        getLoopDetails(stateHistory, firstSeenStateIndex, stateHistory.size(), newText)
                );
            }
            stateHistory.add(newText);
        }

        throw new VariableCycleException(
                String.format("Variable processing reached the safety iteration limit of %d passes.", MAX_PROCESSING_PASSES),
                getLoopDetails(stateHistory, 0, stateHistory.size(), newText)
        );
    }

    private static @NonNull HashMap<@NonNull String, @NonNull String> toDefaultMap(@NonNull DefaultVariable @NonNull [] defaultVariable) {
        HashMap<String, String> defaultVariables = new HashMap<>(defaultVariable.length);
        for (DefaultVariable variable : defaultVariable) {
            defaultVariables.put(variable.variable(), variable.value());
        }
        return defaultVariables;
    }

    private static @NonNull HashMap<@NonNull String, @NonNull String> toDefaultMap(@NonNull XmlDefaultVariable @NonNull [] defaultVariable) {
        HashMap<String, String> defaultVariables = new HashMap<>(defaultVariable.length);
        for (XmlDefaultVariable variable : defaultVariable) {
            defaultVariables.put(variable.key(), variable.value());
        }
        return defaultVariables;
    }

    private static boolean isIncompletelyProcessed(@NonNull String newText, boolean withLanguage, @NonNull Set<@NonNull String> ignoredVariables) {
        if (withLanguage && hasUnresolvedPlaceholder(newText, LANGUAGE_PATTERN, ignoredVariables)) {
            return true;
        }

        return hasUnresolvedPlaceholder(newText, CONFIG_PATTERN, ignoredVariables)
                || hasUnresolvedPlaceholder(newText, VARIABLE_PATTERN, ignoredVariables);
    }

    private static boolean hasUnresolvedPlaceholder(@NonNull String text, @NonNull Pattern pattern, @NonNull Set<@NonNull String> ignoredVariables) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            if (!ignoredVariables.contains(matcher.group())) {
                return true;
            }
        }
        return false;
    }

    private static @NonNull ReplacementResult replaceVariables(@NonNull String text, @NonNull Map<@NonNull String, @NonNull String> variables, @NonNull Map<@NonNull String, @NonNull String> defaultVariables, @NonNull Set<@NonNull String> unknownVariables) {
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text.length());
        boolean changed = false;

        while (matcher.find()) {
            String token = matcher.group();
            String key = matcher.group(1);
            String replacement = defaultVariables.containsKey(key)
                    ? defaultVariables.get(key)
                    : getVariable(key, variables);

            if (replacement == null) {
                unknownVariables.add(token);
                matcher.appendReplacement(result, Matcher.quoteReplacement(token));
                continue;
            }

            if (!replacement.equals(token)) {
                changed = true;
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return new ReplacementResult(result.toString(), changed);
    }

    private static @NonNull ReplacementResult replaceConfigValues(@NonNull String text, @NonNull Set<@NonNull String> unknownVariables) {
        Matcher matcher = CONFIG_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder(text.length());
        boolean changed = false;

        while (matcher.find()) {
            String token = matcher.group();
            String key = matcher.group(1);

            if (!JDABootConfigurationManager.getConfigProviderChain().hasKey(key)) {
                unknownVariables.add(token);
                matcher.appendReplacement(result, Matcher.quoteReplacement(token));
                continue;
            }

            String replacement = JDABootConfigurationManager.getConfigProviderChain().getString(key);
            if (replacement == null) {
                unknownVariables.add(token);
                matcher.appendReplacement(result, Matcher.quoteReplacement(token));
                continue;
            }

            if (!replacement.equals(token)) {
                changed = true;
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return new ReplacementResult(result.toString(), changed);
    }

    private static @Nullable String getVariable(@NonNull String key, @NonNull Map<@NonNull String, @NonNull String> variables) {
        if (variables.containsKey(key)) {
            return variables.get(key);
        } else if (GlobalVariables.hasVariable(key)) {
            return GlobalVariables.get(key);
        } else {
            return null;
        }
    }

    private static @NonNull String getLoopDetails(@NonNull List<@NonNull String> stateHistory, int startInclusive, int endExclusive, @NonNull String currentText) {
        Set<String> loopTokens = new LinkedHashSet<>();

        int safeStart = Math.max(0, Math.min(startInclusive, stateHistory.size()));
        int safeEnd = Math.max(safeStart, Math.min(endExclusive, stateHistory.size()));

        for (int i = safeStart; i < safeEnd; i++) {
            collectLoopTokens(stateHistory.get(i), VARIABLE_PATTERN, loopTokens);
            collectLoopTokens(stateHistory.get(i), CONFIG_PATTERN, loopTokens);
            collectLoopTokens(stateHistory.get(i), LANGUAGE_PATTERN, loopTokens);
        }

        collectLoopTokens(currentText, VARIABLE_PATTERN, loopTokens);
        collectLoopTokens(currentText, CONFIG_PATTERN, loopTokens);
        collectLoopTokens(currentText, LANGUAGE_PATTERN, loopTokens);

        if (loopTokens.isEmpty()) {
            return "unknown";
        }
        return String.join(", ", loopTokens);
    }

    private static void collectLoopTokens(@NonNull String text, @NonNull Pattern pattern, @NonNull Set<@NonNull String> loopTokens) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            loopTokens.add(matcher.group(1));
        }
    }

    private record ReplacementResult(@NonNull String text, boolean changed) {}
}
