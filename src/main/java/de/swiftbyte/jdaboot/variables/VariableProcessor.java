package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import de.swiftbyte.jdaboot.exceptions.VariableCycleException;
import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import lombok.CustomLog;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The VariableProcessor class is responsible for processing variables in a given string.
 * It replaces placeholders in the string with the corresponding values from a provided map of variables.
 * It also supports default variables, which are replaced in the string if they are present.
 *
 * @since alpha.4
 */
@CustomLog
public class VariableProcessor {

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

    /**
     * Processes variables using XML default variable definitions.
     *
     * @param locale          The locale used for translations.
     * @param old             The source text.
     * @param variables       The variables available for replacement.
     * @param defaultVariable The XML default variables.
     * @return The processed text.
     * @since 1.0.0-beta.2
     */
    public static @NonNull String processVariable(@NonNull DiscordLocale locale, @NonNull String old, @NonNull Map<@NonNull String, @NonNull String> variables, @NonNull XmlDefaultVariable @NonNull [] defaultVariable) {
        return processVariable(locale, old, variables, toDefaultMap(defaultVariable));
    }

    /**
     * Processes placeholders without explicit variables.
     *
     * @param locale The locale used for translations.
     * @param old    The source text.
     * @return The processed text.
     * @since alpha.4
     */
    public static @NonNull String processVariable(@NonNull DiscordLocale locale, @NonNull String old) {
        return processVariable(locale, old, new HashMap<>(), new HashMap<>());
    }

    /**
     * Processes placeholders using the default locale and no explicit variables.
     *
     * @param old The source text.
     * @return The processed text.
     * @since alpha.4
     */
    public static @NonNull String processVariable(@NonNull String old) {
        return processVariable(DiscordLocale.ENGLISH_US, old, new HashMap<>(), new HashMap<>());
    }

    /**
     * Processes all supported placeholders with explicit and default variables.
     *
     * @param locale           The locale used for translations.
     * @param old              The source text.
     * @param variables        The explicit variables.
     * @param defaultVariables The default variables.
     * @return The processed text.
     * @since 1.0.0-beta.2
     */
    private static @NonNull String processVariable(@NonNull DiscordLocale locale, @NonNull String old, @NonNull Map<@NonNull String, @NonNull String> variables, @NonNull Map<@NonNull String, @NonNull String> defaultVariables) {
        PlaceholderEngine engine = PlaceholderEngine.builder()
                .resolver('$', key -> {
                    String variable = getVariable(key, variables);
                    return variable != null ? variable : defaultVariables.get(key);
                })
                .resolver('?', VariableProcessor::getConfigValue)
                .resolver('#', key -> TranslationProcessor.getTranslatedString(locale, key))
                .build();

        try {
            return engine.resolve(old);
        } catch (PlaceholderEngine.PlaceholderCycleException e) {
            throw new VariableCycleException(
                    "Detected cyclic variable references while processing placeholders",
                    formatReferences(e.getCycle())
            );
        } catch (PlaceholderEngine.PlaceholderDepthException e) {
            throw new VariableCycleException(
                    "Variable processing reached the safety depth limit of " + e.getMaxDepth(),
                    formatReferences(e.getResolutionPath())
            );
        }
    }

    /**
     * Converts annotation default variables into a lookup map.
     *
     * @param defaultVariable The annotation default variables.
     * @return The default variable lookup map.
     * @since alpha.4
     */
    private static @NonNull HashMap<@NonNull String, @NonNull String> toDefaultMap(@NonNull DefaultVariable @NonNull [] defaultVariable) {
        HashMap<String, String> defaultVariables = new HashMap<>(defaultVariable.length);
        for (DefaultVariable variable : defaultVariable) {
            defaultVariables.put(variable.variable(), variable.value());
        }
        return defaultVariables;
    }

    /**
     * Converts XML default variables into a lookup map.
     *
     * @param defaultVariable The XML default variables.
     * @return The default variable lookup map.
     * @since 1.0.0-beta.2
     */
    private static @NonNull HashMap<@NonNull String, @NonNull String> toDefaultMap(@NonNull XmlDefaultVariable @NonNull [] defaultVariable) {
        HashMap<String, String> defaultVariables = new HashMap<>(defaultVariable.length);
        for (XmlDefaultVariable variable : defaultVariable) {
            defaultVariables.put(variable.key(), variable.value());
        }
        return defaultVariables;
    }

    /**
     * Resolves a configuration value.
     *
     * @param key The configuration key.
     * @return The configured value, or {@code null} when the key is unknown.
     * @since 1.0.0-beta.2
     */
    private static @Nullable String getConfigValue(@NonNull String key) {
        if (!JDABootConfigurationManager.getConfigProviderChain().hasKey(key)) {
            return null;
        }
        return JDABootConfigurationManager.getConfigProviderChain().getString(key);
    }

    /**
     * Resolves an explicit or global variable.
     *
     * @param key       The variable key.
     * @param variables The explicit variables.
     * @return The variable value, or {@code null} when the key is unknown.
     * @since alpha.4
     */
    private static @Nullable String getVariable(@NonNull String key, @NonNull Map<@NonNull String, @NonNull String> variables) {
        if (variables.containsKey(key)) {
            return variables.get(key);
        } else if (GlobalVariables.hasVariable(key)) {
            return GlobalVariables.get(key);
        } else {
            return null;
        }
    }

    /**
     * Formats placeholder references for compatibility with the existing cycle exception.
     *
     * @param references The placeholder references.
     * @return The formatted reference path.
     * @since 1.0.0-beta.2
     */
    private static @NonNull String formatReferences(
            @NonNull Iterable<PlaceholderEngine.PlaceholderReference> references) {
        StringBuilder result = new StringBuilder();
        for (PlaceholderEngine.PlaceholderReference reference : references) {
            if (!result.isEmpty()) {
                result.append(" -> ");
            }
            result.append(reference.token());
        }
        return result.isEmpty() ? "unknown" : result.toString();
    }
}
