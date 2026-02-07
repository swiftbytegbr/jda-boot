package de.swiftbyte.jdaboot.interaction.command;

import de.swiftbyte.jdaboot.embed.TemplateEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * The SlashCommandExecutor interface represents a slash command in the application.
 * It provides methods to handle enabling the command and executing the command when it's invoked.
 *
 * @since alpha.4
 */
public abstract class SlashCommandExecutor {

    /**
     * Called when the slash command is enabled. The default implementation does nothing.
     *
     * @param data The data of the slash command.
     * @since alpha.4
     */
    public void onEnable(SlashCommandData data) {
    }

    /**
     * Called when the slash command is invoked.
     *
     * @param event The event of the slash command interaction.
     * @since alpha.4
     */
    public abstract void onCommand(SlashCommandInteractionEvent event);

    /**
     * Called when the slash command is auto-completed.
     *
     * @since alpha.4
     */
    public void onAutoComplete(AutoCompleteQuery query, CommandAutoCompleteInteractionEvent event) {
    }

    protected void reply(SlashCommandInteractionEvent event, TemplateEmbed embed) {
        reply(event, embed, event.getUserLocale());
    }

    protected void reply(SlashCommandInteractionEvent event, TemplateEmbed embed, DiscordLocale locale) {
        event.replyEmbeds(embed.advancedEmbed(locale).build()).queue();
    }

    protected void replyEphemeral(SlashCommandInteractionEvent event, TemplateEmbed embed) {
        replyEphemeral(event, embed, event.getUserLocale());
    }

    protected void replyEphemeral(SlashCommandInteractionEvent event, TemplateEmbed embed, DiscordLocale locale) {
        event.replyEmbeds(embed.advancedEmbed(locale).build()).setEphemeral(true).queue();
    }
}
