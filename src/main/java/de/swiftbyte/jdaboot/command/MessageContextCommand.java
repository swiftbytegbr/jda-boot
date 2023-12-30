package de.swiftbyte.jdaboot.command;

import de.swiftbyte.jdaboot.command.info.MessageContextCommandInfo;

/**
 * The MessageContextCommand interface extends the ContextCommand interface with MessageContextCommandInfo as its type parameter.
 * It represents a command that operates within the context of a message.
 *
 * @since alpha.4
 */
public interface MessageContextCommand extends ContextCommand<MessageContextCommandInfo> {}