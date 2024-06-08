package de.swiftbyte.jdaboot.annotation.interaction.modal;

import de.swiftbyte.jdaboot.interaction.modal.ModalExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to identify a modal by its unique identifier.
 * This annotation is used to link a field directly to a modal based on its ID, facilitating
 * the association between a modal in the user interface and its corresponding handling logic.
 * <p>
 * Applying this annotation to a field allows the system to automatically route modal click
 * events to the associated logic, based on the modal's ID. This mechanism supports a clean
 * and straightforward way to manage modal interactions within the application.
 *
 * @see ModalExecutor
 * @since 1.0.0-alpha.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ModalById {

    /**
     * Specifies the unique identifier of the modal.
     * This ID is used to match the modal in the user interface with its handling logic.
     *
     * @return The unique ID of the modal.
     */
    String value();

}
