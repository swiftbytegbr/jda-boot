package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalDefinition;
import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import de.swiftbyte.jdaboot.interaction.modal.model.XmlModalLayoutDefinition;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The TemplateModal class is responsible for generating advanced modals based on a provided template.
 * It uses the ModalById or ModalByClass annotation
 * to define the template and the AdvancedModal class to generate the final modal.
 *
 * @since 1.0.0-alpha.7
 */
public class TemplateModal {

    @Getter(AccessLevel.PACKAGE)
    private final @Nullable ModalDefinition definition;

    @Getter(AccessLevel.PACKAGE)
    private final @Nullable XmlModalLayoutDefinition xmlDefinition;

    @Getter(AccessLevel.PACKAGE)
    private final @NonNull XmlDefaultVariable @NonNull [] xmlAnnotationDefaultVars;

    @Getter(AccessLevel.PACKAGE)
    private final @NonNull String id;

    /**
     * Constructor for TemplateModal. Initializes the template with the specified ModalDefinition annotation.
     *
     * @param modalDefinition The ModalDefinition annotation to use as a template.
     * @since 1.0.0-alpha.7
     */
    protected TemplateModal(@NonNull ModalDefinition modalDefinition, @NonNull String id) {
        this.definition = modalDefinition;
        this.xmlDefinition = null;
        this.xmlAnnotationDefaultVars = new XmlDefaultVariable[0];
        this.id = id;
    }

    /**
     * Constructor for an XML modal template definition.
     *
     * @param xmlDefinition Parsed XML modal definition.
     * @param id            Resolved modal id.
     * @since 1.0.0-beta.2
     */
    protected TemplateModal(@NonNull XmlModalLayoutDefinition xmlDefinition, @NonNull String id) {
        this(xmlDefinition, id, new DefaultVariable[0]);
    }

    /**
     * Constructor for an XML modal template definition.
     *
     * @param xmlDefinition  Parsed XML modal definition.
     * @param id             Resolved modal id.
     * @param defaultVars    Default variables configured on the XML modal executor.
     * @since 1.0.0-beta.2
     */
    protected TemplateModal(@NonNull XmlModalLayoutDefinition xmlDefinition, @NonNull String id, @NonNull DefaultVariable @NonNull [] defaultVars) {
        this.definition = null;
        this.xmlDefinition = xmlDefinition;
        this.xmlAnnotationDefaultVars = toXmlDefaultVars(defaultVars);
        this.id = id;
    }

    /**
     * Converts annotation default variables into XML default variables.
     *
     * @param defaultVars The annotation default variables.
     * @return The XML default variables.
     * @since 1.0.0-beta.2
     */
    private static @NonNull XmlDefaultVariable @NonNull [] toXmlDefaultVars(@NonNull DefaultVariable @NonNull [] defaultVars) {
        XmlDefaultVariable[] result = new XmlDefaultVariable[defaultVars.length];
        for (int i = 0; i < defaultVars.length; i++) {
            result[i] = new XmlDefaultVariable(defaultVars[i].variable(), defaultVars[i].value());
        }
        return result;
    }

    /**
     * Generates an AdvancedModal based on the template and the specified locale.
     *
     * @param locale The locale to use for generating the AdvancedModal.
     * @return The generated AdvancedModal.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedModal advancedModal(@NonNull DiscordLocale locale) {
        return new AdvancedModal(this, locale);
    }

    /**
     * Generates an AdvancedModal based on the template and the English US locale.
     *
     * @return The generated AdvancedModal.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedModal advancedModal() {
        return new AdvancedModal(this, DiscordLocale.ENGLISH_US);
    }
}
