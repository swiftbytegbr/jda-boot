package de.swiftbyte.jdaboot.annotation.interaction.modal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ModalRow annotation is used to define a modal row and provide metadata about the input.
 *
 * @since 1.0.0-alpha.7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_PARAMETER)
public @interface ModalRow {

    /**
     * The id of the input.
     *
     * @return The id of the input.
     * @since 1.0.0-alpha.7
     */
    String id();

    /**
     * The label of the input.
     *
     * @return The label of the input.
     * @since 1.0.0-alpha.7
     */
    String label();

    /**
     * The style of the input.
     *
     * @return The style of the input.
     * @since 1.0.0-alpha.7
     */
    InputStyle inputStyle();

    /**
     * The placeholder of the input.
     *
     * @return The placeholder of the input.
     * @since 1.0.0-alpha.7
     */
    String placeholder() default "";

    /**
     * The default value of the input.
     *
     * @return The default value of the input.
     * @since 1.0.0-alpha.7
     */
    String defaultValue() default "";

    /**
     * The minimum length of the input.
     *
     * @return The minimum length of the input.
     * @since 1.0.0-alpha.7
     */
    int minLength() default 0;

    /**
     * The maximum length of the input.
     *
     * @return The maximum length of the input.
     * @since 1.0.0-alpha.7
     */
    int maxLength() default 0;

    /**
     * Specifies whether the input is required.
     *
     * @return True if the input is required, false otherwise.
     * @since 1.0.0-alpha.7
     */
    boolean required() default false;

    /**
     * The InputStyle enum defines the styles of inputs that can be created.
     *
     * @since 1.0.0-alpha.7
     */
    enum InputStyle {
        SHORT,
        PARAGRAPH
    }
}
