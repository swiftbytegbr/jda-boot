package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import de.swiftbyte.jdaboot.exceptions.TranslationCycleException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The TranslationProcessor class is responsible for processing translations in a given string.
 * It replaces placeholders in the string with the corresponding translated values from a provided translation bundle.
 *
 * @since alpha.4
 */
@Slf4j
public class TranslationProcessor {
    private static final Pattern TRANSLATION_PATTERN = Pattern.compile(Pattern.quote("#{") + "(.*?)" + Pattern.quote("}"));
    private static final int MAX_PROCESSING_PASSES = 64;

    /**
     * Processes the translations in the given string using the provided locale.
     * Replaces placeholders in the string with the corresponding translated values.
     *
     * @param locale The locale to use for processing the translations.
     * @param old    The original string with placeholders.
     * @return The processed string with placeholders replaced by translated values.
     * @since alpha.4
     */
    public static @NonNull String processTranslation(@NonNull DiscordLocale locale, @NonNull String old) {

        String newText = old;
        HashMap<String, String> translationCache = new HashMap<>();
        HashMap<String, Integer> seenStateIndexes = new HashMap<>();
        List<String> stateHistory = new ArrayList<>();

        for (int i = 0; i < MAX_PROCESSING_PASSES; i++) {
            Matcher matcher = TRANSLATION_PATTERN.matcher(newText);
            StringBuilder result = new StringBuilder(newText.length());
            boolean changed = false;

            while (matcher.find()) {
                String key = matcher.group(1);
                String replacement = translationCache.computeIfAbsent(key, cachedKey -> getTranslatedString(locale, cachedKey));
                String token = matcher.group();

                if (!replacement.equals(token)) {
                    changed = true;
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }

            matcher.appendTail(result);
            newText = result.toString();

            if (!TRANSLATION_PATTERN.matcher(newText).find()) {
                return newText;
            }

            if (!changed) {
                throw new TranslationCycleException(
                        "Detected cyclic translation references while processing placeholders",
                        getLoopDetails(stateHistory, 0, stateHistory.size(), newText)
                );
            }

            Integer firstSeenStateIndex = seenStateIndexes.putIfAbsent(newText, stateHistory.size());
            if (firstSeenStateIndex != null) {
                throw new TranslationCycleException(
                        "Detected cyclic translation references while processing placeholders",
                        getLoopDetails(stateHistory, firstSeenStateIndex, stateHistory.size(), newText)
                );
            }
            stateHistory.add(newText);
        }

        throw new TranslationCycleException(
                String.format("Translation processing reached the safety iteration limit of %d passes", MAX_PROCESSING_PASSES),
                getLoopDetails(stateHistory, 0, stateHistory.size(), newText)
        );
    }

    /**
     * Retrieves the translated string for the given key using the provided locale.
     *
     * @param locale The locale to use for retrieving the translated string.
     * @param key    The key of the string to translate.
     * @return The translated string.
     * @since alpha.4
     */
    public static @NonNull String getTranslatedString(@NonNull DiscordLocale locale, @NonNull String key) {
        TranslationProvider translationProvider = JDABootConfigurationManager.getTranslationProvider();
        Locale targetLocale = Locale.forLanguageTag(locale.getLocale());
        if (targetLocale.getLanguage().isEmpty()) {
            targetLocale = Locale.ENGLISH;
        }

        try {
            return translationProvider.getTranslation(key, targetLocale);
        } catch (MissingResourceException ignored) {
            try {
                return translationProvider.getTranslation(key, Locale.ENGLISH);
            } catch (MissingResourceException e) {
                log.warn("Translation key '{}' was not found for locale '{}' or fallback locale 'en'", key, locale.getLocale(), e);
                return "MISSING TRANSLATION";
            }
        }
    }

    private static @NonNull String getLoopDetails(@NonNull List<@NonNull String> stateHistory, int startInclusive, int endExclusive, @NonNull String currentText) {
        Set<String> loopKeys = new LinkedHashSet<>();

        int safeStart = Math.max(0, Math.min(startInclusive, stateHistory.size()));
        int safeEnd = Math.max(safeStart, Math.min(endExclusive, stateHistory.size()));

        for (int i = safeStart; i < safeEnd; i++) {
            collectLoopKeys(stateHistory.get(i), loopKeys);
        }
        collectLoopKeys(currentText, loopKeys);

        if (loopKeys.isEmpty()) {
            return "unknown";
        }
        return String.join(", ", loopKeys);
    }

    private static void collectLoopKeys(@NonNull String text, @NonNull Set<@NonNull String> loopKeys) {
        Matcher matcher = TRANSLATION_PATTERN.matcher(text);
        while (matcher.find()) {
            loopKeys.add(matcher.group(1));
        }
    }

}
