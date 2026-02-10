package de.swiftbyte.jdaboot.interaction.component.v2.model;

import net.dv8tion.jda.api.components.separator.Separator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Node model for Component V2 XML layouts.
 *
 * @since 1.0.0-beta.2
 */
public final class ComponentV2Nodes {

    private ComponentV2Nodes() {
        // utility
    }

    public interface Node {
    }

    public interface MessageTopLevelNode extends Node {
    }

    public interface ContainerChildNode extends Node {
    }

    public interface SectionContentNode extends Node {
    }

    public interface SectionAccessoryNode extends Node {
    }

    public interface ActionRowChildNode extends Node {
    }

    public record TextDisplayNode(@NonNull String content)
            implements MessageTopLevelNode, ContainerChildNode, SectionContentNode {
    }

    public record SeparatorNode(boolean divider, Separator.Spacing spacing)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    public record ActionRowNode(@NonNull List<@NonNull ActionRowChildNode> children)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    public record ContainerNode(@NonNull List<@NonNull ContainerChildNode> children, @Nullable Integer accentColor,
                                boolean spoiler, boolean disabled)
            implements MessageTopLevelNode {
    }

    public record SectionNode(@NonNull List<@NonNull SectionContentNode> content,
                              @NonNull SectionAccessoryNode accessory,
                              boolean disabled)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    public record FileDisplayNode(@NonNull String fileName, boolean spoiler)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    public record MediaGalleryNode(@NonNull List<@NonNull MediaGalleryItemNode> items)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    public record MediaGalleryItemNode(@NonNull String url, @NonNull String description, boolean spoiler) {
    }

    public record ButtonRefNode(@Nullable String id,
                                @Nullable String className) implements ActionRowChildNode, SectionAccessoryNode {
    }

    public record StringSelectRefNode(@Nullable String id, @Nullable String className) implements ActionRowChildNode {
    }

    public record EntitySelectRefNode(@Nullable String id, @Nullable String className) implements ActionRowChildNode {
    }

    public record ThumbnailNode(@NonNull String url, @NonNull String description, boolean spoiler)
            implements SectionAccessoryNode {
    }
}
