package de.swiftbyte.jdaboot.annotation.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ConsoleCommand annotation is used to mark a class as a console command.
 * It includes properties to specify the name and aliases of the command.
 * The class marked with this annotation should implement the ConsoleCommandExecutor interface.
 *
 * @see de.swiftbyte.jdaboot.cli.ConsoleCommandExecutor
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConsoleCommandDefinition {

    /**
     * The name of the console command.
     *
     * @return The name of the console command.
     * @since alpha.4
     */
    String name();

    /**
     * The aliases of the console command.
     * These are alternative names that can be used to invoke the command.
     *
     * @return The aliases of the console command.
     * @since alpha.4
     */
    String[] aliases() default {};
}
