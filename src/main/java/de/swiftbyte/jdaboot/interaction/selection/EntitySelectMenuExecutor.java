package de.swiftbyte.jdaboot.interaction.selection;

import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.HashMap;

/**
 * The EntitySelectMenuExecutor interface represents an entity select menu in the application.
 * It provides a method to handle entity select menu submit events.
 *
 * @since 1.0.0-alpha.11
 */
public interface EntitySelectMenuExecutor {

    /**
     * Called when the entity select menu is submitted.
     *
     * @param event     The entity select menu interaction event.
     * @param variables The variables set in the advanced select menu, empty when fix id is used and select menu was created before a restart.
     * @since 1.0.0-alpha.11
     */
    void onSelectMenuSubmit(EntitySelectInteractionEvent event, HashMap<String, String> variables);

}
