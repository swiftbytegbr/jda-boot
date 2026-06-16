package de.swiftbyte.jdaboot.interaction.component.v2;

import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import de.swiftbyte.jdaboot.interaction.component.v2.model.ComponentV2LayoutDefinition;
import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;

/**
 * Template object for generating Component V2 payloads.
 *
 * @since 1.0.0-beta.2
 */
public class TemplateComponentV2 {

    private final @NonNull ComponentV2LayoutDefinition definition;
    private final @NonNull XmlDefaultVariable @NonNull [] annotationDefaultVars;

    /**
     * Constructor for a Component V2 template.
     *
     * @param definition The parsed layout definition.
     * @since 1.0.0-beta.2
     */
    protected TemplateComponentV2(@NonNull ComponentV2LayoutDefinition definition) {
        this(definition, new DefaultVariable[0]);
    }

    /**
     * Constructor for a Component V2 template.
     *
     * @param definition  The parsed layout definition.
     * @param defaultVars The default variables configured on the injection annotation.
     * @since 1.0.0-beta.2
     */
    protected TemplateComponentV2(@NonNull ComponentV2LayoutDefinition definition, @NonNull DefaultVariable @NonNull [] defaultVars) {
        this.definition = definition;
        this.annotationDefaultVars = toXmlDefaultVars(defaultVars);
    }

    @NonNull ComponentV2LayoutDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns default variables configured on the injection annotation.
     *
     * @return The annotation default variables.
     * @since 1.0.0-beta.2
     */
    @NonNull XmlDefaultVariable @NonNull [] getAnnotationDefaultVars() {
        return annotationDefaultVars;
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
     * Creates an advanced builder with the given locale.
     *
     * @param locale The locale used for variable/translation processing.
     * @return Advanced builder.
     * @since 1.0.0-beta.2
     */
    public @NonNull AdvancedComponentV2 advancedComponent(@NonNull DiscordLocale locale) {
        return new AdvancedComponentV2(this, locale);
    }

    /**
     * Creates an advanced builder using English (US) locale.
     *
     * @return Advanced builder.
     * @since 1.0.0-beta.2
     */
    public @NonNull AdvancedComponentV2 advancedComponent() {
        return new AdvancedComponentV2(this, DiscordLocale.ENGLISH_US);
    }
}
