package de.swiftbyte.jdaboot.variables;

import de.swiftbyte.jdaboot.JDABoot;
import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VariableProcessor {

    public String processVariable(DiscordLocale locale, String old, HashMap<String, String> variables, DefaultVariable[] defaultVariable) {

        String newText = old;

        newText = JDABoot.getInstance().getTranslator().processTranslation(locale, newText);
        newText = processVariable(newText, variables, defaultVariable);

        return newText;
    }

    public String processVariable(String old, HashMap<String, String> variables, DefaultVariable[] defaultVariable) {

        String newText = old;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (entry.getKey() == null) log.error("Can not use null as variable key on variable " + entry.getValue());
            newText = newText.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        for (DefaultVariable variable : defaultVariable)
            newText = newText.replace("${" + variable.variable() + "}", variable.value());

        return newText;
    }
}
