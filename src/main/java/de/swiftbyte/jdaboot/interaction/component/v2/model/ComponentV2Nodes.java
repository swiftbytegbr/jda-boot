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

    /**
     * Prevents instantiation of this utility class.
     *
     * @since 1.0.0-beta.2
     */
    private ComponentV2Nodes() {
        // utility
    }

    /**
     * Marker interface for Component V2 XML nodes.
     *
     * @since 1.0.0-beta.2
     */
    public interface Node {
    }

    /**
     * Marker interface for message top-level nodes.
     *
     * @since 1.0.0-beta.2
     */
    public interface MessageTopLevelNode extends Node {
    }

    /**
     * Marker interface for container child nodes.
     *
     * @since 1.0.0-beta.2
     */
    public interface ContainerChildNode extends Node {
    }

    /**
     * Marker interface for section content nodes.
     *
     * @since 1.0.0-beta.2
     */
    public interface SectionContentNode extends Node {
    }

    /**
     * Marker interface for section accessory nodes.
     *
     * @since 1.0.0-beta.2
     */
    public interface SectionAccessoryNode extends Node {
    }

    /**
     * Marker interface for action row child nodes.
     *
     * @since 1.0.0-beta.2
     */
    public interface ActionRowChildNode extends Node {
    }

    /**
     * Defines a text display component.
     *
     * @param content The displayed text.
     * @since 1.0.0-beta.2
     */
    public record TextDisplayNode(@NonNull String content)
            implements MessageTopLevelNode, ContainerChildNode, SectionContentNode {
    }

    /**
     * Defines a separator component.
     *
     * @param divider Whether the divider is visible.
     * @param spacing The separator spacing.
     * @since 1.0.0-beta.2
     */
    public record SeparatorNode(boolean divider, Separator.Spacing spacing)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    /**
     * Defines an action row.
     *
     * @param children The action row children.
     * @since 1.0.0-beta.2
     */
    public record ActionRowNode(@NonNull List<@NonNull ActionRowChildNode> children)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    /**
     * Defines a container component.
     *
     * @param children    The container children.
     * @param accentColor The optional accent color.
     * @param spoiler     Whether the container is a spoiler.
     * @param disabled    Whether the container is disabled.
     * @since 1.0.0-beta.2
     */
    public record ContainerNode(@NonNull List<@NonNull ContainerChildNode> children, @Nullable Integer accentColor,
                                boolean spoiler, boolean disabled)
            implements MessageTopLevelNode {
    }

    /**
     * Defines a section component.
     *
     * @param content   The section content.
     * @param accessory The section accessory.
     * @param disabled  Whether the section is disabled.
     * @since 1.0.0-beta.2
     */
    public record SectionNode(@NonNull List<@NonNull SectionContentNode> content,
                              @NonNull SectionAccessoryNode accessory,
                              boolean disabled)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    /**
     * Defines a file display component.
     *
     * @param fileName The referenced attachment file name.
     * @param spoiler  Whether the file is a spoiler.
     * @since 1.0.0-beta.2
     */
    public record FileDisplayNode(@NonNull String fileName, boolean spoiler)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    /**
     * Defines a media gallery component.
     *
     * @param items The gallery items.
     * @since 1.0.0-beta.2
     */
    public record MediaGalleryNode(@NonNull List<@NonNull MediaGalleryItemNode> items)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    /**
     * Defines a media gallery item.
     *
     * @param url         The media URL.
     * @param description The optional description.
     * @param spoiler     Whether the item is a spoiler.
     * @since 1.0.0-beta.2
     */
    public record MediaGalleryItemNode(@NonNull String url, @NonNull String description, boolean spoiler) {
    }

    /**
     * Defines a button reference.
     *
     * @param id        The referenced button ID.
     * @param className The referenced button executor class.
     * @since 1.0.0-beta.2
     */
    public record ButtonRefNode(@Nullable String id,
                                @Nullable String className) implements ActionRowChildNode, SectionAccessoryNode {
    }

    /**
     * Defines a string select reference.
     *
     * @param id        The referenced select menu ID.
     * @param className The referenced select menu executor class.
     * @since 1.0.0-beta.2
     */
    public record StringSelectRefNode(@Nullable String id, @Nullable String className) implements ActionRowChildNode {
    }

    /**
     * Defines an entity select reference.
     *
     * @param id        The referenced select menu ID.
     * @param className The referenced select menu executor class.
     * @since 1.0.0-beta.2
     */
    public record EntitySelectRefNode(@Nullable String id, @Nullable String className) implements ActionRowChildNode {
    }

    /**
     * Defines a thumbnail component.
     *
     * @param url         The thumbnail URL.
     * @param description The optional description.
     * @param spoiler     Whether the thumbnail is a spoiler.
     * @since 1.0.0-beta.2
     */
    public record ThumbnailNode(@NonNull String url, @NonNull String description, boolean spoiler)
            implements SectionAccessoryNode {
    }
}
