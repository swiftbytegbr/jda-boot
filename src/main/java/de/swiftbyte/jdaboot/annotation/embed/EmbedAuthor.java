package de.swiftbyte.jdaboot.annotation.embed;

import org.jspecify.annotations.NonNull;

/**
 * The EmbedAuthor annotation is used to specify the author of an embed.
 * It includes properties to specify the name, URL, and icon URL of the author.
 *
 * @since alpha.4
 */
public @interface EmbedAuthor {

    /**
     * The name of the author.
     *
     * @return The name of the author.
     * @since alpha.4
     */
    @NonNull String name() default "";

    /**
     * The URL of the author.
     *
     * @return The URL of the author.
     * @since alpha.4
     */
    @NonNull String url() default "";

    /**
     * The icon URL of the author.
     *
     * @return The icon URL of the author.
     * @since alpha.4
     */
    @NonNull String iconUrl() default "";

}