package de.swiftbyte.jdaboot.command.info;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;

public class MessageContextCommandInfo extends MessageContextInteractionEvent implements ContextCommandInfo {

    public MessageContextCommandInfo(net.dv8tion.jda.api.JDA api, long responseNumber, MessageContextInteraction interaction) {
        super(api, responseNumber, interaction);
    }
}