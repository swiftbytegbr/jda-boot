package de.swiftbyte.jdaboot.command.info;

import de.swiftbyte.jdaboot.JDABoot;
import de.swiftbyte.jdaboot.variables.Translator;

/**
 * The CommandInfo interface represents the information for a command.
 * It provides a method to get the Translator instance from JDABoot.
 *
 * @since alpha.4
 */
public interface CommandInfo {

    /**
     * Retrieves the Translator instance from JDABoot.
     *
     * @return The Translator instance.
     * @since alpha.4
     */
    default Translator getTranslator() {
        return JDABoot.getInstance().getTranslator();
    }

}
