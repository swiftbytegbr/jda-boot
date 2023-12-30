package de.swiftbyte.jdaboot.command.info;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;

/**
 * The MessageContextCommandInfo class extends MessageContextInteractionEvent and implements the ContextCommandInfo interface.
 * It represents the context information for a message context command.
 *
 * @since alpha.4
 */
public class MessageContextCommandInfo extends MessageContextInteractionEvent implements ContextCommandInfo {

    /**
     * Constructor for MessageContextCommandInfo. Initializes the context information with the specified JDA instance, response number, and message context interaction.
     *
     * @param api The JDA instance associated with the event.
     * @param responseNumber The response number for the event.
     * @param interaction The message context interaction associated with the event.
     * @since alpha.4
     */
    public MessageContextCommandInfo(net.dv8tion.jda.api.JDA api, long responseNumber, MessageContextInteraction interaction) {
        super(api, responseNumber, interaction);
    }
}