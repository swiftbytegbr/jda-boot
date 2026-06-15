package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import de.swiftbyte.jdaboot.exceptions.TranslationCycleException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.MissingResourceException;

/**
 * The TranslationProcessor class is responsible for processing translations in a given string.
 * It replaces placeholders in the string with the corresponding translated values from a provided translation bundle.
 *
 * @since alpha.4
 */
@Slf4j
public class TranslationProcessor {

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
        PlaceholderEngine engine = PlaceholderEngine.builder()
                .resolver('#', key -> getTranslatedString(locale, key))
                .build();

        try {
            return engine.resolve(old);
        } catch (PlaceholderEngine.PlaceholderCycleException e) {
            throw new TranslationCycleException(
                    "Detected cyclic translation references while processing placeholders",
                    formatReferences(e.getCycle())
            );
        } catch (PlaceholderEngine.PlaceholderDepthException e) {
            throw new TranslationCycleException(
                    "Translation processing reached the safety depth limit of " + e.getMaxDepth(),
                    formatReferences(e.getResolutionPath())
            );
        }
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
