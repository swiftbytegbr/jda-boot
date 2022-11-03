package de.jonafaust.jdaboot.command.info;

import de.jonafaust.jdaboot.JDABoot;
import de.jonafaust.jdaboot.variables.Translator;

public interface CommandInfo {

    default Translator getTranslator() {
        return JDABoot.getInstance().getTranslator();
    }

}
