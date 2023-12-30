package de.swiftbyte.jdaboot.command;

import de.swiftbyte.jdaboot.command.info.UserContextCommandInfo;

/**
 * The UserContextCommand interface extends the ContextCommand interface with UserContextCommandInfo as its type parameter.
 * It represents a command that operates within the context of a user.
 *
 * @since alpha.4
 */
public interface UserContextCommand extends ContextCommand<UserContextCommandInfo> {}