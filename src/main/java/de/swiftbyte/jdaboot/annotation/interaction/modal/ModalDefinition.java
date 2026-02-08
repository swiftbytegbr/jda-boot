package de.swiftbyte.jdaboot.annotation.interaction.modal;

import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ModalDefinition annotation is used to mark a class as a bot modal and provide metadata about the modal.
 *
 * @since 1.0.0-alpha.7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModalDefinition {

    /**
     * The title of the modal.
     *
     * @return The title of the modal.
     * @since 1.0.0-alpha.7
     */
    @NonNull String title();

    /**
     * The id of the modal.
     *
     * @return The id of the modal.
     * @since 1.0.0-alpha.7
     */
    @NonNull String id() default "";

    @NonNull ModalRow @NonNull [] rows() default {};

    /**
     * The default variables of the modal.
     *
     * @return The default variables of the modal.
     * @since 1.0.0-alpha.7
     */
    @NonNull DefaultVariable @NonNull [] defaultVars() default {};

}
