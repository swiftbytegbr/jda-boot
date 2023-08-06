package de.swiftbyte.jdaboot.variables;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ResourceTranslationBundle implements TranslationBundle {

    @Override
    public String getTranslation(String key, Locale locale) {
        ResourceBundle resourceBundle = PropertyResourceBundle.getBundle("messages", locale);
        return resourceBundle.getString(key);
    }
}
