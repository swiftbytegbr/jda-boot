package de.jonafaust.jdaboot.annotation.command;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    String name();

    String description() default "DESCRIPTION MISSING";

    Type type();

    Permission enabledFor() default Permission.UNKNOWN;

    boolean guildOnly() default false;

    CommandOption[] options() default {};

    SubcommandGroup[] subcommandGroups() default {};

    Subcommand[] subcommands() default {};

    enum Type {
        SLASH,
        USER,
        MESSAGE
    }
}
