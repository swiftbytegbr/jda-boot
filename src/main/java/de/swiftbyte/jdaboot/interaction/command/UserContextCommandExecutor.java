package de.swiftbyte.jdaboot.interaction.command;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

/**
 * The UserContextCommandExecutor interface extends the ContextCommand interface with UserContextCommandInfo as its type parameter.
 * It represents a command that operates within the context of a user.
 *
 * @since alpha.4
 */
public interface UserContextCommandExecutor extends ContextCommandExecutor<UserContextInteractionEvent> {

}