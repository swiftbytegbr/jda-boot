package de.swiftbyte.jdaboot.annotation.interaction.command;

import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The SubcommandGroup annotation is used to specify a group of subcommands in a command.
 * It includes properties to specify the name, description, and subcommands of the group.
 *
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_PARAMETER)
public @interface SubcommandGroup {

    /**
     * The name of the subcommand group.
     *
     * @return The name of the subcommand group.
     * @since alpha.4
     */
    @NonNull String name();

    /**
     * The description of the subcommand group.
     *
     * @return The description of the subcommand group.
     * @since alpha.4
     */
    @NonNull String description();

    /**
     * The subcommands in the subcommand group.
     *
     * @return The subcommands in the subcommand group.
     * @since alpha.4
     */
    @NonNull Subcommand @NonNull [] subcommands() default {};
}