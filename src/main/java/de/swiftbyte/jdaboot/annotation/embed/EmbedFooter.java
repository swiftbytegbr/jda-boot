package de.swiftbyte.jdaboot.annotation.embed;

import org.jspecify.annotations.NonNull;

/**
 * The EmbedFooter annotation is used to specify the footer of an embed.
 * It includes properties to specify the text and icon URL of the footer.
 *
 * @since alpha.4
 */
public @interface EmbedFooter {

    /**
     * The text of the footer.
     *
     * @return The text of the footer.
     * @since alpha.4
     */
    @NonNull String text() default "";

    /**
     * The icon URL of the footer.
     *
     * @return The icon URL of the footer.
     * @since alpha.4
     */
    @NonNull String iconUrl() default "";

}