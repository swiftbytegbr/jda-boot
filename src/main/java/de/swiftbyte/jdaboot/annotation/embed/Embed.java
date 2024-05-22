package de.swiftbyte.jdaboot.annotation.embed;

import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import de.swiftbyte.jdaboot.embed.EmbedColor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Embed annotation is used to specify the properties of an embed.
 * It includes properties to specify the URL, title, description, color, hex color, thumbnail URL, author, footer, image URL, fields, and default variables of the embed.
 *
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Embed {

    /**
     * The ID of the embed.
     *
     * @return The ID of the embed.
     * @since 1.0.0.alpha.5
     */
    String id() default "";

    /**
     * The ID of the embed that this embed is based on.
     *
     * @return The ID of the embed that this embed is based on.
     * @since 1.0.0.alpha.5
     */
    String basedOn() default "";

    /**
     * The URL of the embed.
     *
     * @return The URL of the embed.
     * @since alpha.4
     */
    String url() default "";

    /**
     * The title of the embed.
     *
     * @return The title of the embed.
     * @since alpha.4
     */
    String title() default "";

    /**
     * The description of the embed.
     *
     * @return The description of the embed.
     * @since alpha.4
     */
    String description() default "";

    /**
     * The color of the embed.
     *
     * @return The color of the embed.
     * @since alpha.4
     */
    EmbedColor color() default EmbedColor.NOT_DEFINED;

    /**
     * The hex color of the embed.
     *
     * @return The hex color of the embed.
     * @since alpha.4
     */
    String hexColor() default "";

    /**
     * The thumbnail URL of the embed.
     *
     * @return The thumbnail URL of the embed.
     * @since alpha.4
     */
    String thumbnailUrl() default "";

    /**
     * The author of the embed.
     *
     * @return The author of the embed.
     * @since alpha.4
     */
    EmbedAuthor author() default @EmbedAuthor();

    /**
     * The footer of the embed.
     *
     * @return The footer of the embed.
     * @since alpha.4
     */
    EmbedFooter footer() default @EmbedFooter();

    /**
     * The image URL of the embed.
     *
     * @return The image URL of the embed.
     * @since alpha.4
     */
    String imageUrl() default "";

    /**
     * The fields of the embed.
     *
     * @return The fields of the embed.
     * @since alpha.4
     */
    EmbedField[] fields() default {};

    /**
     * The default variables of the embed.
     *
     * @return The default variables of the embed.
     * @since alpha.4
     */
    DefaultVariable[] defaultVars() default {};

}