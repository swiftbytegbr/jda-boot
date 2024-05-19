package de.swiftbyte.jdaboot.annotation.interactions.button;

import de.swiftbyte.jdaboot.interactions.button.ButtonExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to associate a field with a specific {@link ButtonExecutor} class.
 * This annotation is used to dynamically link a button interaction to its handling logic
 * defined in a class that implements the {@link ButtonExecutor} interface.
 * <p>
 * By annotating a field with {@code ButtonByClass}, the system can automatically
 * route button click events to the specified executor for processing.
 * This allows for a clean separation of button definitions and their behavior.
 *
 * @see ButtonExecutor
 * @since 1.0.0-alpha.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ButtonByClass {

    /**
     * Specifies the class of the {@link ButtonExecutor} that should be invoked
     * when the associated button is interacted with.
     *
     * @return The class implementing {@link ButtonExecutor} to handle button interactions.
     */
    Class<? extends ButtonExecutor> value();

}
