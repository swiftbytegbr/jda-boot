package de.swiftbyte.jdaboot.command.info;

import de.swiftbyte.jdaboot.JDABoot;
import de.swiftbyte.jdaboot.variables.Translator;

public interface CommandInfo {

    default Translator getTranslator() {
        return JDABoot.getInstance().getTranslator();
    }

}
