package de.swiftbyte.jdaboot.interaction.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.HashMap;

/**
 * The ModalExecutor interface represents a bot modal in the application.
 * It provides a method to handle modal submit events.
 *
 * @since 1.0.0-alpha.7
 */
public interface ModalExecutor {

    /**
     * Called when the modal is submitted.
     *
     * @param event The modal interaction event.
     * @since 1.0.0-alpha.7
     */
    void onModalSubmit(ModalInteractionEvent event, HashMap<String, String> variables);

}
