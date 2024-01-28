package de.swiftbyte.jdaboot.command.info;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction;

/**
 * The UserContextCommandInfo class extends UserContextInteractionEvent and implements the ContextCommandInfo interface.
 * It represents the context information for a user context command.
 *
 * @since alpha.4
 */
public class UserContextCommandInfo extends UserContextInteractionEvent implements ContextCommandInfo {

    /**
     * Constructor for UserContextCommandInfo. Initializes the context information with the specified JDA instance, response number, and user context interaction.
     *
     * @param api            The JDA instance associated with the event.
     * @param responseNumber The response number for the event.
     * @param interaction    The user context interaction associated with the event.
     * @since alpha.4
     */
    public UserContextCommandInfo(net.dv8tion.jda.api.JDA api, long responseNumber, UserContextInteraction interaction) {
        super(api, responseNumber, interaction);
    }

}
