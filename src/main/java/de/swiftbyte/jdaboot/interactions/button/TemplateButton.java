package de.swiftbyte.jdaboot.interactions.button;

import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.annotation.interactions.button.ButtonDefinition;
import de.swiftbyte.jdaboot.embeds.AdvancedEmbed;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class TemplateButton {

    @Getter(AccessLevel.PACKAGE)
    private final ButtonDefinition definition;

    @Getter(AccessLevel.PACKAGE)
    private final String id;

    /**
     * Constructor for TemplateButton. Initializes the template with the specified ButtonDefinition annotation.
     *
     * @param buttonDefinition The ButtonDefinition annotation to use as a template.
     * @since 1.0.0-alpha.6
     */
    protected TemplateButton(ButtonDefinition buttonDefinition, String id) {
        this.definition = buttonDefinition;
        this.id = id;
    }

    /**
     * Generates an AdvancedButton based on the template and the specified locale.
     *
     * @param locale The locale to use for generating the AdvancedButton.
     * @return The generated AdvancedButton.
     * @since 1.0.0-alpha.6
     */
    public AdvancedButton generateAdvancedButton(DiscordLocale locale) {
        return new AdvancedButton(this, locale);
    }

    /**
     * Generates an AdvancedButton based on the template and the English US locale.
     *
     * @return The generated AdvancedButton.
     * @since 1.0.0-alpha.6
     */
    public AdvancedButton generateAdvancedButton() {
        return new AdvancedButton(this, DiscordLocale.ENGLISH_US);
    }
}
