package de.swiftbyte.jdaboot.interaction.selection;

import de.swiftbyte.jdaboot.annotation.interaction.selection.EntitySelectMenuDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.selection.StringSelectMenuDefinition;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The TemplateSelectMenu class is responsible for generating advanced select menus based on a provided template.
 * It uses the SelectMenuById or SelectMenuByClass annotation
 * to define the template and the AdvancedSelectMenu class to generate the final select menu.
 *
 * @since 1.0.0-alpha.11
 */
public class TemplateSelectMenu {

    @Getter(AccessLevel.PACKAGE)
    private final @Nullable EntitySelectMenuDefinition entityDefinition;

    @Getter(AccessLevel.PACKAGE)
    private final @Nullable StringSelectMenuDefinition stringDefinition;

    @Getter(AccessLevel.PACKAGE)
    private final @NonNull String id;

    /**
     * Constructor for TemplateSelectMenu. Initializes the template with the specified EntitySelectMenuDefinition annotation.
     *
     * @param selectMenuDefinition The EntitySelectMenuDefinition annotation to use as a template.
     * @since 1.0.0-alpha.11
     */
    protected TemplateSelectMenu(@NonNull EntitySelectMenuDefinition selectMenuDefinition, @NonNull String id) {
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
    protected TemplateSelectMenu(@NonNull StringSelectMenuDefinition selectMenuDefinition, @NonNull String id) {
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
    public @NonNull AdvancedSelectMenu advancedSelectMenu(@NonNull DiscordLocale locale) {
        return new AdvancedSelectMenu(this, locale);
    }

    /**
     * Generates an AdvancedSelectMenu based on the template and the English US locale.
     *
     * @return The generated AdvancedSelectMenu.
     * @since 1.0.0-alpha.11
     */
    public @NonNull AdvancedSelectMenu advancedSelectMenu() {
        return new AdvancedSelectMenu(this, DiscordLocale.ENGLISH_US);
    }
}
