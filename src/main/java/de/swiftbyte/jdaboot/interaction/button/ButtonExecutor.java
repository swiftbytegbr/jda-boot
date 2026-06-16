package de.swiftbyte.jdaboot.interaction.button;

import de.swiftbyte.jdaboot.embed.TemplateEmbed;
import de.swiftbyte.jdaboot.interaction.component.v2.TemplateComponentV2;
import de.swiftbyte.jdaboot.interaction.modal.TemplateModal;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * The ButtonExecutor class represents a bot button in the application.
 * It provides a method to handle button click events.
 *
 * @since alpha.4
 */
public abstract class ButtonExecutor {

    /**
     * Called when the button is clicked.
     *
     * @param event     The button interaction event.
     * @param variables The variables set in the advanced button, empty when fix id is used and button was created before a restart.
     * @since alpha.4
     */
    public abstract void onButtonClick(@NonNull ButtonInteractionEvent event, @NonNull Map<@NonNull String, @NonNull String> variables);

    /**
     * Replies with an embed using the user's locale.
     *
     * @param event The interaction event.
     * @param embed The embed template.
     * @since 1.0.0-beta.2
     */
    protected void reply(@NonNull ButtonInteractionEvent event, @NonNull TemplateEmbed embed) {
        reply(event, embed, event.getUserLocale());
    }

    /**
     * Replies with an embed using the provided locale.
     *
     * @param event  The interaction event.
     * @param embed  The embed template.
     * @param locale The locale used to build the embed.
     * @since 1.0.0-beta.2
     */
    protected void reply(@NonNull ButtonInteractionEvent event, @NonNull TemplateEmbed embed, @NonNull DiscordLocale locale) {
        event.replyEmbeds(embed.advancedEmbed(locale).build()).queue();
    }

    /**
     * Replies with Component V2 using the user's locale.
     *
     * @param event     The interaction event.
     * @param component The component template.
     * @since 1.0.0-beta.2
     */
    protected void reply(@NonNull ButtonInteractionEvent event, @NonNull TemplateComponentV2 component) {
        reply(event, component, event.getUserLocale());
    }

    /**
     * Replies with Component V2 using the provided locale.
     *
     * @param event     The interaction event.
     * @param component The component template.
     * @param locale    The locale used to build the component.
     * @since 1.0.0-beta.2
     */
    protected void reply(@NonNull ButtonInteractionEvent event, @NonNull TemplateComponentV2 component, @NonNull DiscordLocale locale) {
        event.replyComponents(component.advancedComponent(locale).build()).useComponentsV2().queue();
    }

    /**
     * Opens a modal using the user's locale.
     *
     * @param event The interaction event.
     * @param modal The modal template.
     * @since 1.0.0-beta.2
     */
    protected void reply(@NonNull ButtonInteractionEvent event, @NonNull TemplateModal modal) {
        reply(event, modal, event.getUserLocale());
    }

    /**
     * Opens a modal using the provided locale.
     *
     * @param event  The interaction event.
     * @param modal  The modal template.
     * @param locale The locale used to build the modal.
     * @since 1.0.0-beta.2
     */
    protected void reply(@NonNull ButtonInteractionEvent event, @NonNull TemplateModal modal, @NonNull DiscordLocale locale) {
        event.replyModal(modal.advancedModal(locale).build()).queue();
    }

    /**
     * Replies ephemerally with an embed using the user's locale.
     *
     * @param event The interaction event.
     * @param embed The embed template.
     * @since 1.0.0-beta.2
     */
    protected void replyEphemeral(@NonNull ButtonInteractionEvent event, @NonNull TemplateEmbed embed) {
        replyEphemeral(event, embed, event.getUserLocale());
    }

    /**
     * Replies ephemerally with an embed using the provided locale.
     *
     * @param event  The interaction event.
     * @param embed  The embed template.
     * @param locale The locale used to build the embed.
     * @since 1.0.0-beta.2
     */
    protected void replyEphemeral(@NonNull ButtonInteractionEvent event, @NonNull TemplateEmbed embed, @NonNull DiscordLocale locale) {
        event.replyEmbeds(embed.advancedEmbed(locale).build()).setEphemeral(true).queue();
    }

    /**
     * Replies ephemerally with Component V2 using the user's locale.
     *
     * @param event     The interaction event.
     * @param component The component template.
     * @since 1.0.0-beta.2
     */
    protected void replyEphemeral(@NonNull ButtonInteractionEvent event, @NonNull TemplateComponentV2 component) {
        replyEphemeral(event, component, event.getUserLocale());
    }

    /**
     * Replies ephemerally with Component V2 using the provided locale.
     *
     * @param event     The interaction event.
     * @param component The component template.
     * @param locale    The locale used to build the component.
     * @since 1.0.0-beta.2
     */
    protected void replyEphemeral(@NonNull ButtonInteractionEvent event, @NonNull TemplateComponentV2 component, @NonNull DiscordLocale locale) {
        event.replyComponents(component.advancedComponent(locale).build()).useComponentsV2().setEphemeral(true).queue();
    }
}
