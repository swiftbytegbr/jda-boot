package de.swiftbyte.jdaboot.annotation.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_PARAMETER)
public @interface SubcommandGroup {

    String name();

    String description();

    Subcommand[] subcommands() default {};
}
