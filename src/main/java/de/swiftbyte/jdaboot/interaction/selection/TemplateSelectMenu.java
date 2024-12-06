package de.swiftbyte.jdaboot.interaction.selection;

import de.swiftbyte.jdaboot.annotation.interaction.selection.EntitySelectMenuDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.selection.StringSelectMenuDefinition;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * The TemplateSelectMenu class is responsible for generating advanced select menus based on a provided template.
 * It uses the SelectMenuById or SelectMenuByClass annotation
 * to define the template and the AdvancedSelectMenu class to generate the final select menu.
 *
 * @since 1.0.0-alpha.11
 */
public class TemplateSelectMenu {

    @Getter(AccessLevel.PACKAGE)
    private final EntitySelectMenuDefinition entityDefinition;

    @Getter(AccessLevel.PACKAGE)
    private final StringSelectMenuDefinition stringDefinition;

    @Getter(AccessLevel.PACKAGE)
    private final String id;

    /**
     * Constructor for TemplateSelectMenu. Initializes the template with the specified EntitySelectMenuDefinition annotation.
     *
     * @param selectMenuDefinition The EntitySelectMenuDefinition annotation to use as a template.
     * @since 1.0.0-alpha.11
     */
    protected TemplateSelectMenu(EntitySelectMenuDefinition selectMenuDefinition, String id) {
        this.entityDefinition = selectMenuDefinition;
        this.stringDefinition = null;
        this.id = id;
    }

    /**
     * Constructor for TemplateSelectMenu. Initializes the template with the specified StringSelectMenuDefinition annotation.
     *
     * @param selectMenuDefinition The StringSelectMenuDefinition annotation to use as a template.
     * @since 1.0.0-alpha.11
     */
    protected TemplateSelectMenu(StringSelectMenuDefinition selectMenuDefinition, String id) {
        this.stringDefinition = selectMenuDefinition;
        this.entityDefinition = null;
        this.id = id;
    }

    /**
     * Generates an AdvancedSelectMenu based on the template and the specified locale.
     *
     * @param locale The locale to use for generating the AdvancedSelectMenu.
     * @return The generated AdvancedSelectMenu.
     * @since 1.0.0-alpha.11
     */
    public AdvancedSelectMenu advancedSelectMenu(DiscordLocale locale) {
        return new AdvancedSelectMenu(this, locale);
    }

    /**
     * Generates an AdvancedSelectMenu based on the template and the English US locale.
     *
     * @return The generated AdvancedSelectMenu.
     * @since 1.0.0-alpha.11
     */
    public AdvancedSelectMenu advancedSelectMenu() {
        return new AdvancedSelectMenu(this, DiscordLocale.ENGLISH_US);
    }
}
