package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The VariableProcessor class is responsible for processing variables in a given string.
 * It replaces placeholders in the string with the corresponding values from a provided map of variables.
 * It also supports default variables, which are replaced in the string if they are present.
 *
 * @since alpha.4
 */
@Slf4j
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
    public static String processVariable(DiscordLocale locale, String old, HashMap<String, String> variables, DefaultVariable[] defaultVariable) {

        String newText = old;

        newText = TranslationProcessor.processTranslation(locale, newText);
        newText = processVariable(newText, variables, defaultVariable);

        if (isIncompletelyProcessed(newText, true)) {
            newText = processVariable(locale, newText, variables, defaultVariable);
        }

        return newText;
    }

    /**
     * Processes the variables in the given string using the provided variables map, and default variables.
     * Replaces placeholders in the string with the corresponding values.
     *
     * @param old             The original string with placeholders.
     * @param variables       The map of variables to replace in the string.
     * @param defaultVariable The array of default variables to replace in the string.
     * @return The processed string with placeholders replaced by variable values.
     * @since alpha.4
     */
    public static String processVariable(String old, HashMap<String, String> variables, DefaultVariable[] defaultVariable) {

        String newText = old;

        for (DefaultVariable variable : defaultVariable)
            newText = newText.replace("${" + variable.variable() + "}", variable.value());

        Pattern p = Pattern.compile(Pattern.quote("${") + "(.*?)" + Pattern.quote("}"));
        Matcher m = p.matcher(newText);
        while (m.find()) {
            String value = variables.get(m.group().replace("${", "").replace("}", ""));
            if (value == null) throw new RuntimeException("Variable " + m.group() + " not found in variables map.");
            newText = newText.replace(m.group(), value);
        }

        p = Pattern.compile(Pattern.quote("?{") + "(.*?)" + Pattern.quote("}"));
        m = p.matcher(newText);

        while (m.find()) {
            if (JDABootConfigurationManager.getConfigProviderChain().hasKey(m.group().replace("?{", "").replace("}", ""))) {
                String value = JDABootConfigurationManager.getConfigProviderChain().getString(m.group().replace("?{", "").replace("}", ""));
                if (value == null) throw new RuntimeException("Variable " + m.group() + " not found in variables map.");
                newText = newText.replace(m.group(), JDABootConfigurationManager.getConfigProviderChain().getString(m.group().replace("?{", "").replace("}", "")));
            }
        }

        if (isIncompletelyProcessed(newText, false)) {
            newText = processVariable(newText, variables, defaultVariable);
        }

        return newText;
    }

    private static boolean isIncompletelyProcessed(String newText, boolean withLanguage) {
        Pattern languagePattern = Pattern.compile(Pattern.quote("#{") + "(.*?)" + Pattern.quote("}"));
        Matcher languageMatcher = languagePattern.matcher(newText);
        Pattern configPattern = Pattern.compile(Pattern.quote("?{") + "(.*?)" + Pattern.quote("}"));
        Matcher configMatcher = configPattern.matcher(newText);
        Pattern variablePattern = Pattern.compile(Pattern.quote("${") + "(.*?)" + Pattern.quote("}"));
        Matcher variableMatcher = variablePattern.matcher(newText);

        if (withLanguage && languageMatcher.find()) {
            return true;
        }

        return configMatcher.find() || variableMatcher.find();
    }
}
