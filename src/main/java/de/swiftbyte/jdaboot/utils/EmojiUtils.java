package de.swiftbyte.jdaboot.utils;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jspecify.annotations.NonNull;

/**
 * Utility methods for Discord emoji parsing.
 *
 * @since 1.0.0-beta.2
 */
public final class EmojiUtils {

    private EmojiUtils() {
        //utility
    }

    /**
     * Parses an emoji from Discord formatted custom emoji syntax, unicode, or codepoint notation.
     *
     * @param value The emoji value.
     * @return The parsed emoji.
     * @since 1.0.0-beta.2
     */
    public static @NonNull Emoji parseEmoji(@NonNull String value) {
        return Emoji.fromFormatted(value.trim());
    }
}
