package de.swiftbyte.jdaboot.annotation.interactions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Button annotation is used to mark a class as a bot button and provide metadata about the button.
 * It includes properties such as the button's label, type, id, emoji, and url.
 *
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ButtonDefinition {

    /**
     * The label of the button.
     *
     * @return The label of the button.
     * @since alpha.4
     */
    String label();

    /**
     * The type of the button.
     *
     * @return The type of the button.
     * @since alpha.4
     */
    Type type();

    /**
     * The id of the button.
     *
     * @return The id of the button.
     * @since alpha.4
     */
    String id() default "";

    /**
     * The emoji of the button.
     *
     * @return The emoji of the button.
     * @since alpha.4
     */
    String emoji() default "";

    /**
     * The url of the button.
     *
     * @return The url of the button.
     * @since alpha.4
     */
    String url() default "";

    /**
     * The Type enum defines the types of buttons that can be created.
     *
     * @since alpha.4
     */
    enum Type {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        LINK
    }
}
