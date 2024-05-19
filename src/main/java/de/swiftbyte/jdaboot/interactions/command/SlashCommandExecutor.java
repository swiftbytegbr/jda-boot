package de.swiftbyte.jdaboot.interactions.command;

import de.swiftbyte.jdaboot.embeds.TemplateEmbed;
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

    protected SlashCommandInteractionEvent event;

    protected void call(SlashCommandInteractionEvent event) {
        this.event = event;
        onCommand();
    }

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
     * @since alpha.4
     */
    public abstract void onCommand();

    /**
     * Called when the slash command is auto-completed.
     *
     * @since alpha.4
     */
    public void onAutoComplete(AutoCompleteQuery query, CommandAutoCompleteInteractionEvent event) {
    }

    protected void reply(TemplateEmbed embed) {
        reply(embed, event.getUserLocale());
    }

    protected void reply(TemplateEmbed embed, DiscordLocale locale) {
        event.replyEmbeds(embed.generateAdvancedEmbed(locale).generateEmbed()).queue();
    }

    protected void replyEphemeral(TemplateEmbed embed) {
        replyEphemeral(embed, event.getUserLocale());
    }

    protected void replyEphemeral(TemplateEmbed embed, DiscordLocale locale) {
        event.replyEmbeds(embed.generateAdvancedEmbed(locale).generateEmbed()).setEphemeral(true).queue();
    }
}
