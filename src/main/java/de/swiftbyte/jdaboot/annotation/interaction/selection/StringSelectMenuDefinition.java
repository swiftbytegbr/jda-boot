package de.swiftbyte.jdaboot.annotation.interaction.selection;

import de.swiftbyte.jdaboot.annotation.DefaultVariable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The StringSelectMenuDefinition annotation is used to define a string select menu and provide metadata about the menu.
 *
 * @since 1.0.0-alpha.11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StringSelectMenuDefinition {

    /**
     * The id of the select menu.
     *
     * @return The id of the select menu.
     * @since 1.0.0-alpha.11
     */
    String id() default "";

    /**
     * The placeholder text for the select menu.
     *
     * @return The placeholder text.
     * @since 1.0.0-alpha.11
     */
    String placeholder() default "";

    /**
     * The minimum number of options that can be selected.
     *
     * @return The minimum number of options.
     * @since 1.0.0-alpha.11
     */
    int minOptions() default 1;

    /**
     * The maximum number of options that can be selected.
     *
     * @return The maximum number of options.
     * @since 1.0.0-alpha.11
     */
    int maxOptions() default 1;

    /**
     * Indicates whether the select menu is disabled.
     *
     * @return True if the select menu is disabled, false otherwise.
     * @since 1.0.0-alpha.11
     */
    boolean isDisabled() default false;

    /**
     * The options available in the select menu.
     *
     * @return An array of StringSelectOption.
     * @since 1.0.0-alpha.11
     */
    StringSelectOption[] options();

    /**
     * The default variables for the select menu.
     *
     * @return An array of DefaultVariable.
     * @since 1.0.0-alpha.11
     */
    DefaultVariable[] defaultVars() default {};
}
