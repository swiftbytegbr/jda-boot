package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.MissingResourceException;
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

        Pattern p = Pattern.compile(Pattern.quote("#{") + "(.*?)" + Pattern.quote("}"));
        Matcher m = p.matcher(newText);

        while (m.find()) {
            if (getTranslatedString(locale, m.group().replace("#{", "").replace("}", "")) != null) {
                newText = newText.replace(m.group(), getTranslatedString(locale, m.group().replace("#{", "").replace("}", "")));
            }
        }

        return newText;

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

}
