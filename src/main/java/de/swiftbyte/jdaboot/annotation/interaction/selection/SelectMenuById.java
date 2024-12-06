package de.swiftbyte.jdaboot.annotation.interaction.selection;

import de.swiftbyte.jdaboot.interaction.selection.StringSelectMenuExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to associate a field with a specific string select menu by its ID.
 * This annotation is used to dynamically link a string select menu interaction to its handling logic
 * defined in a class that implements the {@link StringSelectMenuExecutor} interface.
 * <p>
 * By annotating a field with {@code SelectMenuById}, the system can automatically
 * route string select menu events to the specified executor for processing.
 * This allows for a clean separation of string select menu definitions and their behavior.
 *
 * @see StringSelectMenuExecutor
 * @since 1.0.0-alpha.11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SelectMenuById {

    /**
     * Specifies the ID of the string select menu that should be linked to the executor.
     *
     * @return The ID of the string select menu.
     */
    String value();

}