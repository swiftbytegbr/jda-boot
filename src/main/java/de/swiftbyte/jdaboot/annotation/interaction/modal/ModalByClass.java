package de.swiftbyte.jdaboot.annotation.interaction.modal;

import de.swiftbyte.jdaboot.interaction.modal.ModalExecutor;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to associate a field with a specific {@link ModalExecutor} class.
 * This annotation is used to dynamically link a modal interaction to its handling logic
 * defined in a class that extends {@link ModalExecutor}.
 * <p>
 * By annotating a field with {@code ModalByClass}, the system can automatically
 * route modal submit events to the specified executor for processing.
 * This allows for a clean separation of modal definitions and their behavior.
 *
 * @see ModalExecutor
 * @since 1.0.0-alpha.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModalByClass {

    /**
     * Specifies the class of the {@link ModalExecutor} that should be invoked
     * when the associated modal is interacted with.
     *
     * @return The executor class used to handle modal interactions.
     */
    @NonNull Class<? extends ModalExecutor> value();

}
