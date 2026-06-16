package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.embed.TemplateEmbed;
import de.swiftbyte.jdaboot.interaction.component.v2.TemplateComponentV2;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * The ModalExecutor class represents a bot modal in the application.
 * It provides a method to handle modal submit events.
 *
 * @since 1.0.0-alpha.7
 */
public abstract class ModalExecutor {

    /**
     * Called when the modal is submitted.
     *
     * @param event The modal interaction event.
     * @since 1.0.0-alpha.7
     */
    public abstract void onModalSubmit(@NonNull ModalInteractionEvent event, @NonNull Map<@NonNull String, @NonNull String> variables);

    /**
     * Replies with an embed using the user's locale.
     *
     * @param event The interaction event.
     * @param embed The embed template.
     * @since 1.0.0-beta.2
     */
    protected void reply(@NonNull ModalInteractionEvent event, @NonNull TemplateEmbed embed) {
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
    protected void reply(@NonNull ModalInteractionEvent event, @NonNull TemplateEmbed embed, @NonNull DiscordLocale locale) {
        event.replyEmbeds(embed.advancedEmbed(locale).build()).queue();
    }

    /**
     * Replies with Component V2 using the user's locale.
     *
     * @param event     The interaction event.
     * @param component The component template.
     * @since 1.0.0-beta.2
     */
    protected void reply(@NonNull ModalInteractionEvent event, @NonNull TemplateComponentV2 component) {
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
    protected void reply(@NonNull ModalInteractionEvent event, @NonNull TemplateComponentV2 component, @NonNull DiscordLocale locale) {
        event.replyComponents(component.advancedComponent(locale).build()).useComponentsV2().queue();
    }

    /**
     * Replies ephemerally with an embed using the user's locale.
     *
     * @param event The interaction event.
     * @param embed The embed template.
     * @since 1.0.0-beta.2
     */
    protected void replyEphemeral(@NonNull ModalInteractionEvent event, @NonNull TemplateEmbed embed) {
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
    protected void replyEphemeral(@NonNull ModalInteractionEvent event, @NonNull TemplateEmbed embed, @NonNull DiscordLocale locale) {
        event.replyEmbeds(embed.advancedEmbed(locale).build()).setEphemeral(true).queue();
    }

    /**
     * Replies ephemerally with Component V2 using the user's locale.
     *
     * @param event     The interaction event.
     * @param component The component template.
     * @since 1.0.0-beta.2
     */
    protected void replyEphemeral(@NonNull ModalInteractionEvent event, @NonNull TemplateComponentV2 component) {
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
    protected void replyEphemeral(@NonNull ModalInteractionEvent event, @NonNull TemplateComponentV2 component, @NonNull DiscordLocale locale) {
        event.replyComponents(component.advancedComponent(locale).build()).useComponentsV2().setEphemeral(true).queue();
    }
}
