package de.swiftbyte.jdaboot.annotation.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to create a command.
 * It must belong to a public class that implements the {@link de.swiftbyte.jdaboot.command.SlashCommand},
 * {@link de.swiftbyte.jdaboot.command.UserContextCommand} or {@link de.swiftbyte.jdaboot.command.MessageContextCommand} interface.
 *
 * @since alpha.3.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    /**
     * The name of the command in discord.
     */
    String name();

    /**
     * The description of the command in discord.
     */
    String description() default "DESCRIPTION MISSING";

    /**
     * The {@link Type} of the command.
     */
    Type type();

    /**
     * The default needed {@link Permission} of the command.
     */
    Permission enabledFor() default Permission.UNKNOWN;

    /**
     * True if the command should only be used on servers and not in private messages, otherwise false.
     */
    boolean guildOnly() default false;

    /**
     * True if the command should be global, false if it should be added manually to a server.
     */
    boolean isGlobal() default true;

    /**
     * The {@link CommandOption}s that the command should have.
     */
    CommandOption[] options() default {};

    /**
     * The {@link SubcommandGroup}s that the command should have.
     */
    SubcommandGroup[] subcommandGroups() default {};

    /**
     * The {@link Subcommand}s that the command should have.
     */
    Subcommand[] subcommands() default {};

    /**
     * The different types of commands discord have.
     */
    enum Type {
        /**
         * A normal slash command.
         */
        SLASH,

        /**
         * A command executed on a user context menu
         */
        USER,

        /**
         * A command executed on a message context menu
         */
        MESSAGE
    }
}
