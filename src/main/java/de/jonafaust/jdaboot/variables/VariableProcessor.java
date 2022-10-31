package de.jonafaust.jdaboot.variables;

import de.jonafaust.jdaboot.annotation.DefaultVariable;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.HashMap;
import java.util.Map;

public class VariableProcessor {

    public String processVariable(DiscordLocale locale, String old, HashMap<String, String> variables, DefaultVariable[] defaultVariable) {

        String newText = old;

        //TODO Add Localisation

        for (Map.Entry<String, String> entry : variables.entrySet())
            newText = newText.replace("${" + entry.getKey() + "}", entry.getValue());
        for (DefaultVariable variable : defaultVariable)
            newText = newText.replace("${" + variable.variable() + "}", variable.value());

        return newText;
    }
}
