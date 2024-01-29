package de.swiftbyte.jdaboot.interactions.buttons;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * The BotButton interface represents a bot button in the application.
 * It provides a method to handle button click events.
 *
 * @since alpha.4
 */
public interface BotButton {

    /**
     * Called when the button is clicked.
     *
     * @param event The button interaction event.
     * @since alpha.4
     */
    void onButtonClick(ButtonInteractionEvent event);

}
