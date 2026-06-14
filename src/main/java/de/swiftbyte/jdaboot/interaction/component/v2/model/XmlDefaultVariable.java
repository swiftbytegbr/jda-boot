package de.swiftbyte.jdaboot.interaction.component.v2.model;

import org.jspecify.annotations.NonNull;

/**
 * Runtime model for XML default variables used by the Component V2 loader.
 *
 * @param key   The variable key.
 * @param value The default value.
 * @since 1.0.0-beta.2
 */
public record XmlDefaultVariable(@NonNull String key, @NonNull String value) {
}
