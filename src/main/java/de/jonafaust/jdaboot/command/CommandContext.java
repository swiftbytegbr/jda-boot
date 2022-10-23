package de.jonafaust.jdaboot.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;

public class CommandContext extends SlashCommandInteractionEvent {

    public CommandContext(@NotNull JDA api, long responseNumber, @NotNull SlashCommandInteraction interaction) {
        super(api, responseNumber, interaction);
    }
}
