package de.swiftbyte.jdaboot.embeds;

import de.swiftbyte.jdaboot.annotation.embed.Embed;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * The TemplateEmbed class is responsible for generating advanced embeds based on a provided template.
 * It uses the Embed annotation to define the template and the AdvancedEmbed class to generate the final embed.
 *
 * @since alpha.4
 */
public class TemplateEmbed {

    @Getter(AccessLevel.PACKAGE)
    private final Embed embed;

    /**
     * Constructor for TemplateEmbed. Initializes the template with the specified Embed annotation.
     *
     * @param embed The Embed annotation to use as a template.
     * @since alpha.4
     */
    protected TemplateEmbed(Embed embed) {
        this.embed = embed;
    }

    /**
     * Generates an AdvancedEmbed based on the template and the specified locale.
     *
     * @param locale The locale to use for generating the AdvancedEmbed.
     * @return The generated AdvancedEmbed.
     * @since alpha.4
     */
    public AdvancedEmbed advancedEmbed(DiscordLocale locale) {
        return new AdvancedEmbed(this, locale);
    }

    /**
     * Generates an AdvancedEmbed based on the template and the English US locale.
     *
     * @return The generated AdvancedEmbed.
     * @since alpha.4
     */
    public AdvancedEmbed advancedEmbed() {
        return new AdvancedEmbed(this, DiscordLocale.ENGLISH_US);
    }
}
