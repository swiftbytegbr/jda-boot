package de.swiftbyte.jdaboot.interaction.component.v2;

import de.swiftbyte.jdaboot.interaction.component.v2.model.ComponentV2LayoutDefinition;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;

/**
 * Template object for generating Component V2 payloads.
 *
 * @since 1.0.0-beta.2
 */
public class TemplateComponentV2 {

    private final @NonNull ComponentV2LayoutDefinition definition;

    /**
     * Constructor for a Component V2 template.
     *
     * @param definition The parsed layout definition.
     * @since 1.0.0-beta.2
     */
    protected TemplateComponentV2(@NonNull ComponentV2LayoutDefinition definition) {
        this.definition = definition;
    }

    @NonNull ComponentV2LayoutDefinition getDefinition() {
        return definition;
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
