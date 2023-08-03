package de.jonafaust.jdaboot.variables;

import de.jonafaust.jdaboot.JDABoot;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {

    public String processTranslation(DiscordLocale locale, String old) {

        String newText = old;

        Pattern p = Pattern.compile(Pattern.quote("#{") + "(.*?)" + Pattern.quote("}"));
        Matcher m = p.matcher(newText);

        while (m.find()) {
            if (getTranslatedString(locale, m.group().replace("#{", "").replace("}", "")) != null)
                newText = newText.replace(m.group(), getTranslatedString(locale, m.group().replace("#{", "").replace("}", "")));
        }

        return newText;

    }

    public String getTranslatedString(DiscordLocale locale, String key) {
        Locale.setDefault(Locale.ENGLISH);
        TranslationBundle translationBundle = JDABoot.getInstance().getTranslationProvider().get();
        return translationBundle.getTranslation(key, new Locale(locale.getLocale()));
    }

}
