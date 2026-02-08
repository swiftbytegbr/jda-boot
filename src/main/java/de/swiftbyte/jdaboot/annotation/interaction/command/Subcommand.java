package de.swiftbyte.jdaboot.annotation.interaction.command;

import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Subcommand annotation is used to specify a subcommand in a command.
 * It includes properties to specify the name, description, and options of the subcommand.
 *
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_PARAMETER)
public @interface Subcommand {

    /**
     * The name of the subcommand.
     *
     * @return The name of the subcommand.
     * @since alpha.4
     */
    @NonNull String name();

    /**
     * The description of the subcommand.
     *
     * @return The description of the subcommand.
     * @since alpha.4
     */
    @NonNull String description();

    /**
     * The options of the subcommand.
     *
     * @return The options of the subcommand.
     * @since alpha.4
     */
    @NonNull CommandOption @NonNull [] options() default {};
}