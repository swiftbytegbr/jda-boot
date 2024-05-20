package de.swiftbyte.jdaboot.interaction.command;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * The ContextCommandExecutor interface represents a command that operates within a certain context.
 * It provides methods to handle enabling the command and executing the command when it's invoked.
 *
 * @param <T> The type of the context in which the command operates.
 * @since alpha.4
 */
interface ContextCommandExecutor<T extends GenericContextInteractionEvent<?>> {

    /**
     * Called when the context command is enabled. The default implementation does nothing.
     *
     * @param data The data of the context command.
     * @since alpha.4
     */
    default void onEnable(CommandData data) {
    }

    /**
     * Called when the context command is invoked.
     *
     * @param event The context of the command.
     * @since alpha.4
     */
    void onCommand(T event);

}
