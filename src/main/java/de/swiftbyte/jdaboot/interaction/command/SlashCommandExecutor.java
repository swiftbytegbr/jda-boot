package de.swiftbyte.jdaboot.interaction.command;

import de.swiftbyte.jdaboot.embed.TemplateEmbed;
import de.swiftbyte.jdaboot.interaction.component.v2.TemplateComponentV2;
import de.swiftbyte.jdaboot.interaction.modal.TemplateModal;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jspecify.annotations.NonNull;

/**
 * The SlashCommandExecutor class represents a slash command in the application.
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
    public void onEnable(@NonNull SlashCommandData data) {
    }

    /**
     * Called when the slash command is invoked.
     *
     * @param event The event of the slash command interaction.
     * @since alpha.4
     */
    public abstract void onCommand(@NonNull SlashCommandInteractionEvent event);

    /**
     * Called when the slash command is auto-completed.
     *
     * @since alpha.4
     */
    public void onAutoComplete(@NonNull AutoCompleteQuery query, @NonNull CommandAutoCompleteInteractionEvent event) {
    }

    protected void reply(@NonNull SlashCommandInteractionEvent event, @NonNull TemplateEmbed embed) {
        reply(event, embed, event.getUserLocale());
    }

    protected void reply(@NonNull SlashCommandInteractionEvent event, @NonNull TemplateEmbed embed, @NonNull DiscordLocale locale) {
        event.replyEmbeds(embed.advancedEmbed(locale).build()).queue();
    }

    protected void reply(@NonNull SlashCommandInteractionEvent event, @NonNull TemplateComponentV2 component) {
        reply(event, component, event.getUserLocale());
    }

    protected void reply(@NonNull SlashCommandInteractionEvent event, @NonNull TemplateComponentV2 component, @NonNull DiscordLocale locale) {
        event.replyComponents(component.advancedComponent(locale).build()).useComponentsV2().queue();
    }

    protected void reply(@NonNull SlashCommandInteractionEvent event, @NonNull TemplateModal modal) {
        reply(event, modal, event.getUserLocale());
    }

    protected void reply(@NonNull SlashCommandInteractionEvent event, @NonNull TemplateModal modal, @NonNull DiscordLocale locale) {
        event.replyModal(modal.advancedModal(locale).build()).queue();
    }

    protected void replyEphemeral(@NonNull SlashCommandInteractionEvent event, @NonNull TemplateEmbed embed) {
        replyEphemeral(event, embed, event.getUserLocale());
    }

    protected void replyEphemeral(@NonNull SlashCommandInteractionEvent event, @NonNull TemplateEmbed embed, @NonNull DiscordLocale locale) {
        event.replyEmbeds(embed.advancedEmbed(locale).build()).setEphemeral(true).queue();
    }

    protected void replyEphemeral(@NonNull SlashCommandInteractionEvent event, @NonNull TemplateComponentV2 component) {
        replyEphemeral(event, component, event.getUserLocale());
    }

    protected void replyEphemeral(@NonNull SlashCommandInteractionEvent event, @NonNull TemplateComponentV2 component, @NonNull DiscordLocale locale) {
        event.replyComponents(component.advancedComponent(locale).build()).useComponentsV2().setEphemeral(true).queue();
    }
}
