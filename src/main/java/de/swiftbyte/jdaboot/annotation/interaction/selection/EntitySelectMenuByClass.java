package de.swiftbyte.jdaboot.annotation.interaction.selection;

import de.swiftbyte.jdaboot.interaction.selection.EntitySelectMenuExecutor;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to associate a field with a specific {@link EntitySelectMenuExecutor} class.
 * This annotation is used to dynamically link an entity select menu interaction to its handling logic
 * defined in a class that extends {@link EntitySelectMenuExecutor}.
 * <p>
 * By annotating a field with {@code EntitySelectMenuByClass}, the system can automatically
 * route entity select menu events to the specified executor for processing.
 * This allows for a clean separation of entity select menu definitions and their behavior.
 *
 * @see EntitySelectMenuExecutor
 * @since 1.0.0-alpha.11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EntitySelectMenuByClass {

    /**
     * Specifies the class of the {@link EntitySelectMenuExecutor} that should be invoked
     * when the associated entity select menu is interacted with.
     *
     * @return The executor class used to handle entity select menu interactions.
     */
    @NonNull Class<? extends EntitySelectMenuExecutor> value();

}
