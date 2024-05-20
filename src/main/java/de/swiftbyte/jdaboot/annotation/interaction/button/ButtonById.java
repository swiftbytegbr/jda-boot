package de.swiftbyte.jdaboot.annotation.interaction.button;

import de.swiftbyte.jdaboot.interaction.button.ButtonExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to identify a button by its unique identifier.
 * This annotation is used to link a field directly to a button based on its ID, facilitating
 * the association between a button in the user interface and its corresponding handling logic.
 * <p>
 * Applying this annotation to a field allows the system to automatically route button click
 * events to the associated logic, based on the button's ID. This mechanism supports a clean
 * and straightforward way to manage button interactions within the application.
 *
 * @see ButtonExecutor
 * @since 1.0.0-alpha.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ButtonById {

    /**
     * Specifies the unique identifier of the button.
     * This ID is used to match the button in the user interface with its handling logic.
     *
     * @return The unique ID of the button.
     */
    String value();

}
