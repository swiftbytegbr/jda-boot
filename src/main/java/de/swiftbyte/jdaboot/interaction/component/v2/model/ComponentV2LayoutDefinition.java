package de.swiftbyte.jdaboot.interaction.component.v2.model;

import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Immutable definition for one Component V2 layout loaded from XML.
 *
 * @param id          The layout ID.
 * @param sourcePath  The source XML resource path.
 * @param defaultVars The default variables.
 * @param components  The top-level components.
 * @since 1.0.0-beta.2
 */
public record ComponentV2LayoutDefinition(
        @NonNull String id,
        @NonNull String sourcePath,
        @NonNull XmlDefaultVariable @NonNull [] defaultVars,
        @NonNull List<ComponentV2Nodes.MessageTopLevelNode> components
) {
}
