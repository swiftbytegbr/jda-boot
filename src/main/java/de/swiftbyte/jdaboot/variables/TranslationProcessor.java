package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.DiscordLocale;

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
    public static String processTranslation(DiscordLocale locale, String old) {

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
    public static String getTranslatedString(DiscordLocale locale, String key) {
        Locale.setDefault(Locale.ENGLISH);
        TranslationProvider translationProvider = JDABootConfigurationManager.getTranslationProvider();
        String translation = "MISSING TRANSLATION";
        try {
            translation = translationProvider.getTranslation(key, new Locale(locale.getLocale()));
        } catch (MissingResourceException e) {
            log.warn("Translation key not found in resources '{}' at:", key, e);
        }
        return translation;
    }

}
