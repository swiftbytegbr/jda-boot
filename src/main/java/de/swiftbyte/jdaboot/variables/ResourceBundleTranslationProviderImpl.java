package de.swiftbyte.jdaboot.variables;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * The ResourceBundleTranslationProviderImpl class implements the TranslationProvider interface.
 * It provides a way to translate a given key into a specific locale using a resource bundle.
 *
 * @since alpha.4
 */
public class ResourceBundleTranslationProviderImpl implements TranslationProvider {

    /**
     * Retrieves the translated string for the given key using the provided locale.
     * It uses a resource bundle to find the translated string.
     *
     * @param key    The key of the string to translate.
     * @param locale The locale to use for retrieving the translated string.
     * @return The translated string.
     * @since alpha.4
     */
    @Override
    public String getTranslation(String key, Locale locale) {
        ResourceBundle resourceBundle = PropertyResourceBundle.getBundle("messages", locale);
        return resourceBundle.getString(key);
    }
}
