package de.swiftbyte.jdaboot.annotation.interaction.modal;

import de.swiftbyte.jdaboot.interaction.modal.ModalExecutor;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers a {@link ModalExecutor} for XML modal layouts.
 *
 * @since 1.0.0-beta.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XmlModalDefinition {

    /**
     * The optional modal ID used by XML layouts with {@code modal-id}.
     * When empty, the modal can still be referenced with {@code modal-class}.
     *
     * @return The modal ID, or an empty string for class-only references.
     * @since 1.0.0-beta.2
     */
    @NonNull String id() default "";
}
