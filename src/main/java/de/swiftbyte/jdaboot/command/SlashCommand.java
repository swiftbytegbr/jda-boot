package de.swiftbyte.jdaboot.command;

import de.swiftbyte.jdaboot.command.info.SlashCommandInfo;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * The SlashCommand interface represents a slash command in the application.
 * It provides methods to handle enabling the command and executing the command when it's invoked.
 *
 * @since alpha.4
 */
public interface SlashCommand {

    /**
     * Called when the slash command is enabled. The default implementation does nothing.
     *
     * @param data The data of the slash command.
     * @since alpha.4
     */
    default void onEnable(SlashCommandData data) {}

    /**
     * Called when the slash command is invoked.
     *
     * @param ctx The context of the slash command.
     * @since alpha.4
     */
    void onCommand(SlashCommandInfo ctx);

}
