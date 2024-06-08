package de.swiftbyte.jdaboot.interaction.button;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.HashMap;

/**
 * The ButtonExecutor interface represents a bot button in the application.
 * It provides a method to handle button click events.
 *
 * @since alpha.4
 */
public interface ButtonExecutor {

    /**
     * Called when the button is clicked.
     *
     * @param event     The button interaction event.
     * @param variables The variables set in the advanced button, empty when fix id is used and button was created before a restart.
     * @since alpha.4
     */
    void onButtonClick(ButtonInteractionEvent event, HashMap<String, String> variables);

}
