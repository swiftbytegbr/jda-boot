package de.swiftbyte.jdaboot.annotation.interaction.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a string select option for a select menu.
 * This annotation can be used to specify the label, value, description, emoji, and default status of the option.
 *
 * @since 1.0.0-alpha.11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_PARAMETER)
public @interface StringSelectOption {

    /**
     * The label of the select option.
     *
     * @return The label of the select option.
     * @since 1.0.0-alpha.11
     */
    String label();

    /**
     * The value of the select option.
     *
     * @return The value of the select option.
     * @since 1.0.0-alpha.11
     */
    String value();

    /**
     * The description of the select option.
     *
     * @return The description of the select option.
     * @since 1.0.0-alpha.11
     */
    String description() default "";

    /**
     * The emoji associated with the select option.
     *
     * @return The emoji of the select option.
     * @since 1.0.0-alpha.11
     */
    String emoji() default "";

    /**
     * Indicates whether the select option is the default option.
     *
     * @return True if the option is the default, false otherwise.
     * @since 1.0.0-alpha.11
     */
    boolean isDefault() default false;

}
