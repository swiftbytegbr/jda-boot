package de.swiftbyte.jdaboot.annotation;

/**
 * The DefaultVariable annotation is used to specify a default variable that can be used in the application.
 * It includes properties to specify the variable's name and value.
 *
 * @since alpha.4
 */
public @interface DefaultVariable {

    /**
     * The name of the variable.
     *
     * @return The name of the variable.
     * @since alpha.4
     */
    String variable();

    /**
     * The value of the variable.
     *
     * @return The value of the variable.
     * @since alpha.4
     */
    String value();

}
