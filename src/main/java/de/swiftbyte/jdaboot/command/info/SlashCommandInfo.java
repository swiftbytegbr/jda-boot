package de.swiftbyte.jdaboot.command.info;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;

/**
 * The SlashCommandInfo class extends SlashCommandInteractionEvent and implements the CommandInfo interface.
 * It represents the context information for a slash command.
 *
 * @since alpha.4
 */
public class SlashCommandInfo extends SlashCommandInteractionEvent implements CommandInfo {

    /**
     * Constructor for SlashCommandInfo. Initializes the context information with the specified JDA instance, response number, and slash command interaction.
     *
     * @param api The JDA instance associated with the event.
     * @param responseNumber The response number for the event.
     * @param interaction The slash command interaction associated with the event.
     * @since alpha.4
     */
    public SlashCommandInfo(@NotNull JDA api, long responseNumber, @NotNull SlashCommandInteraction interaction) {
        super(api, responseNumber, interaction);
    }
}
