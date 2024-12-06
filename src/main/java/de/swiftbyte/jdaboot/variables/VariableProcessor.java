package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        List<String> unknownVariables = new ArrayList<>();
        String newText = old;

        newText = TranslationProcessor.processTranslation(locale, newText);
        newText = processVariable(newText, variables, defaultVariable, unknownVariables);

        if (isIncompletelyProcessed(newText, true, unknownVariables)) {
            newText = processVariable(locale, newText, variables, defaultVariable);
        }

        return newText;
    }

    /**
     * Processes the variables in the given string using the provided variables map, and default variables.
     * Replaces placeholders in the string with the corresponding values.
     *
     * @param old              The original string with placeholders.
     * @param variables        The map of variables to replace in the string.
     * @param defaultVariable  The array of default variables to replace in the string.
     * @param unknownVariables The list of already unknown variables.
     * @return The processed string with placeholders replaced by variable values.
     * @since alpha.4
     */
    public static String processVariable(String old, HashMap<String, String> variables, DefaultVariable[] defaultVariable, List<String> unknownVariables) {

        String newText = old;

        for (DefaultVariable variable : defaultVariable)
            newText = newText.replace("${" + variable.variable() + "}", variable.value());

        Pattern p = Pattern.compile(Pattern.quote("${") + "(.*?)" + Pattern.quote("}"));
        Matcher m = p.matcher(newText);
        while (m.find()) {
            String value = variables.get(m.group().replace("${", "").replace("}", ""));
            if (value == null) {
                unknownVariables.add(m.group());
                continue;
            }
            newText = newText.replace(m.group(), value);
        }

        p = Pattern.compile(Pattern.quote("?{") + "(.*?)" + Pattern.quote("}"));
        m = p.matcher(newText);

        while (m.find()) {
            if (JDABootConfigurationManager.getConfigProviderChain().hasKey(m.group().replace("?{", "").replace("}", ""))) {
                String value = JDABootConfigurationManager.getConfigProviderChain().getString(m.group().replace("?{", "").replace("}", ""));
                if (value == null) {
                    unknownVariables.add(m.group());
                    continue;
                }
                newText = newText.replace(m.group(), JDABootConfigurationManager.getConfigProviderChain().getString(m.group().replace("?{", "").replace("}", "")));
            } else {
                unknownVariables.add(m.group());
            }
        }

        if (isIncompletelyProcessed(newText, false, unknownVariables)) {
            newText = processVariable(newText, variables, defaultVariable, unknownVariables);
        }

        return newText;
    }

    private static boolean isIncompletelyProcessed(String newText, boolean withLanguage, List<String> ignoredVariables) {
        Pattern languagePattern = Pattern.compile(Pattern.quote("#{") + "(.*?)" + Pattern.quote("}"));
        Matcher languageMatcher = languagePattern.matcher(newText);
        Pattern configPattern = Pattern.compile(Pattern.quote("?{") + "(.*?)" + Pattern.quote("}"));
        Matcher configMatcher = configPattern.matcher(newText);
        Pattern variablePattern = Pattern.compile(Pattern.quote("${") + "(.*?)" + Pattern.quote("}"));
        Matcher variableMatcher = variablePattern.matcher(newText);

        while (withLanguage && languageMatcher.find())
            if (!ignoredVariables.contains(languageMatcher.group())) return true;
        while (configMatcher.find()) if (!ignoredVariables.contains(configMatcher.group())) return true;
        while (variableMatcher.find()) if (!ignoredVariables.contains(variableMatcher.group())) return true;
        return false;
    }
}
