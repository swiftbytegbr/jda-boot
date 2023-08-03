package de.jonafaust.jdaboot.variables;

import java.util.Locale;

public interface TranslationBundle {

    String getTranslation(String key, Locale locale);

}
