package de.swiftbyte.jdaboot.annotation.interaction.selection;

import de.swiftbyte.jdaboot.interaction.selection.StringSelectMenuExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to associate a field with a specific {@link StringSelectMenuExecutor} class.
 * This annotation is used to dynamically link a string select menu interaction to its handling logic
 * defined in a class that implements the {@link StringSelectMenuExecutor} interface.
 * <p>
 * By annotating a field with {@code StringSelectMenuByClass}, the system can automatically
 * route string select menu events to the specified executor for processing.
 * This allows for a clean separation of string select menu definitions and their behavior.
 *
 * @see StringSelectMenuExecutor
 * @since 1.0.0-alpha.11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StringSelectMenuByClass {

    /**
     * Specifies the class of the {@link StringSelectMenuExecutor} that should be invoked
     * when the associated string select menu is interacted with.
     *
     * @return The class implementing {@link StringSelectMenuExecutor} to handle string select menu interactions.
     */
    Class<? extends StringSelectMenuExecutor> value();

}
