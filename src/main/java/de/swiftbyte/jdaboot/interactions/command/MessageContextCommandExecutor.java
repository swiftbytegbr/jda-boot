package de.swiftbyte.jdaboot.interactions.command;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

/**
 * The MessageContextCommandExecutor interface extends the ContextCommand interface with MessageContextCommandInfo as its type parameter.
 * It represents a command that operates within the context of a message.
 *
 * @since alpha.4
 */
public interface MessageContextCommandExecutor extends ContextCommandExecutor<MessageContextInteractionEvent> {

}