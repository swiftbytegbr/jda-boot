package de.swiftbyte.jdaboot.annotation.embed;

/**
 * The EmbedField annotation is used to specify a field in an embed.
 * It includes properties to specify the title, description, and whether the field is inline.
 *
 * @since alpha.4
 */
public @interface EmbedField {

    /**
     * The title of the field.
     *
     * @return The title of the field.
     * @since alpha.4
     */
    String title() default "";

    /**
     * The description of the field.
     *
     * @return The description of the field.
     * @since alpha.4
     */
    String description() default "";

    /**
     * Specifies whether the field is inline.
     *
     * @return True if the field is inline, false otherwise.
     * @since alpha.4
     */
    boolean inline() default false;

}