package de.jonafaust.jdaboot.command.info;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction;

public class UserContextCommandInfo extends UserContextInteractionEvent implements ContextCommandInfo {

    public UserContextCommandInfo(net.dv8tion.jda.api.JDA api, long responseNumber, UserContextInteraction interaction) {
        super(api, responseNumber, interaction);
    }

}
