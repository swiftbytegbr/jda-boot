package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalDefinition;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * The TemplateModal class is responsible for generating advanced modals based on a provided template.
 * It uses the ModalById or ModalByClass annotation
 * to define the template and the AdvancedModal class to generate the final modal.
 *
 * @since 1.0.0-alpha.7
 */
public class TemplateModal {

    @Getter(AccessLevel.PACKAGE)
    private final ModalDefinition definition;

    @Getter(AccessLevel.PACKAGE)
    private final String id;

    /**
     * Constructor for TemplateModal. Initializes the template with the specified ModalDefinition annotation.
     *
     * @param modalDefinition The ModalDefinition annotation to use as a template.
     * @since 1.0.0-alpha.7
     */
    protected TemplateModal(ModalDefinition modalDefinition, String id) {
        this.definition = modalDefinition;
        this.id = id;
    }

    /**
     * Generates an AdvancedModal based on the template and the specified locale.
     *
     * @param locale The locale to use for generating the AdvancedModal.
     * @return The generated AdvancedModal.
     * @since 1.0.0-alpha.7
     */
    public AdvancedModal advancedModal(DiscordLocale locale) {
        return new AdvancedModal(this, locale);
    }

    /**
     * Generates an AdvancedModal based on the template and the English US locale.
     *
     * @return The generated AdvancedModal.
     * @since 1.0.0-alpha.7
     */
    public AdvancedModal advancedModal() {
        return new AdvancedModal(this, DiscordLocale.ENGLISH_US);
    }
}
