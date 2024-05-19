package de.swiftbyte.jdaboot.annotation.interactions.command;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Command annotation is used to create a command.
 * It must belong to a public class that implements the SlashCommand, UserContextCommand, or MessageContextCommand interface.
 * It includes properties to specify the name, description, type, enabledFor, guildOnly, isGlobal, options, subcommandGroups, subcommands of the command.
 *
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SlashCommandDefinition {

    /**
     * The name of the command in discord.
     *
     * @return The name of the command.
     * @since alpha.4
     */
    String name();

    /**
     * The description of the command in discord.
     *
     * @return The description of the command.
     * @since alpha.4
     */
    String description() default "DESCRIPTION MISSING";

    /**
     * The type of the command.
     *
     * @return The type of the command.
     * @since alpha.4
     */
    Type type();

    /**
     * The default needed Permission of the command.
     *
     * @return The default needed Permission of the command.
     * @since alpha.4
     */
    Permission enabledFor() default Permission.UNKNOWN;

    /**
     * Specifies whether the command should only be used on servers and not in private messages.
     *
     * @return True if the command should only be used on servers, false otherwise.
     * @since alpha.4
     */
    boolean guildOnly() default false;

    /**
     * Specifies whether the command should be global.
     *
     * @return True if the command should be global, false otherwise.
     * @since alpha.4
     */
    boolean isGlobal() default true;

    /**
     * The options that the command should have.
     *
     * @return The options that the command should have.
     * @since alpha.4
     */
    CommandOption[] options() default {};

    /**
     * The subcommand groups that the command should have.
     *
     * @return The subcommand groups that the command should have.
     * @since alpha.4
     */
    SubcommandGroup[] subcommandGroups() default {};

    /**
     * The subcommands that the command should have.
     *
     * @return The subcommands that the command should have.
     * @since alpha.4
     */
    Subcommand[] subcommands() default {};

    /**
     * The different types of commands discord have.
     *
     * @since alpha.4
     */
    enum Type {
        /**
         * A normal slash command.
         *
         * @since alpha.4
         */
        SLASH,

        /**
         * A command executed on a user context menu.
         *
         * @since alpha.4
         */
        USER,

        /**
         * A command executed on a message context menu.
         *
         * @since alpha.4
         */
        MESSAGE
    }
}