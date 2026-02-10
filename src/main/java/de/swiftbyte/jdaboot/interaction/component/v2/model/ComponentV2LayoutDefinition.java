package de.swiftbyte.jdaboot.interaction.component.v2.model;

import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Immutable definition for one Component V2 layout loaded from XML.
 *
 * @since 1.0.0-beta.2
 */
public record ComponentV2LayoutDefinition(
        @NonNull String id,
        @NonNull String sourcePath,
        @NonNull XmlDefaultVariable @NonNull [] defaultVars,
        @NonNull List<ComponentV2Nodes.MessageTopLevelNode> components
) {
}
