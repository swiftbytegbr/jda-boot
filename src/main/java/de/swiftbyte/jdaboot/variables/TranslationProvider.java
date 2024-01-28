package de.swiftbyte.jdaboot.variables;

import java.util.Locale;

/**
 * The TranslationProvider interface defines a contract for classes that provide translation services.
 * Implementations of this interface should provide a way to translate a given key into a specific locale.
 *
 * @since alpha.4
 */
public interface TranslationProvider {

    /**
     * Retrieves the translated string for the given key using the provided locale.
     *
     * @param key    The key of the string to translate.
     * @param locale The locale to use for retrieving the translated string.
     * @return The translated string.
     * @since alpha.4
     */
    String getTranslation(String key, Locale locale);

}
