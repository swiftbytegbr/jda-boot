package de.swiftbyte.jdaboot.interactions.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * The SlashCommandExecutor interface represents a slash command in the application.
 * It provides methods to handle enabling the command and executing the command when it's invoked.
 *
 * @since alpha.4
 */
public interface SlashCommandExecutor {

    /**
     * Called when the slash command is enabled. The default implementation does nothing.
     *
     * @param data The data of the slash command.
     * @since alpha.4
     */
    default void onEnable(SlashCommandData data) {
    }

    /**
     * Called when the slash command is invoked.
     *
     * @since alpha.4
     */
    void onCommand(SlashCommandInteractionEvent event);

    /**
     * Called when the slash command is auto-completed.
     *
     * @since alpha.4
     */
    default void onAutoComplete(AutoCompleteQuery query, CommandAutoCompleteInteractionEvent event) {

    }

}
