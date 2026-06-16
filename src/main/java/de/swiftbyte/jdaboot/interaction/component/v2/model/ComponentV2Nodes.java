package de.swiftbyte.jdaboot.interaction.component.v2.model;

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
     * Marker interface for button nodes supported in action rows and section accessories.
     *
     * @since 1.0.0-beta.2
     */
    public interface ButtonNode extends ActionRowChildNode, SectionAccessoryNode {
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
     * @param divider The variable-capable divider value.
     * @param spacing The variable-capable separator spacing.
     * @since 1.0.0-beta.2
     */
    public record SeparatorNode(@NonNull String divider, @NonNull String spacing)
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
     * @param accentColor The variable-capable optional accent color.
     * @param spoiler     The variable-capable spoiler value.
     * @param disabled    The variable-capable disabled value.
     * @since 1.0.0-beta.2
     */
    public record ContainerNode(@NonNull List<@NonNull ContainerChildNode> children,
                                @NonNull String accentColor,
                                @NonNull String spoiler,
                                @NonNull String disabled)
            implements MessageTopLevelNode {
    }

    /**
     * Defines a section component.
     *
     * @param content   The section content.
     * @param accessory The section accessory.
     * @param disabled  The variable-capable disabled value.
     * @since 1.0.0-beta.2
     */
    public record SectionNode(@NonNull List<@NonNull SectionContentNode> content,
                              @NonNull SectionAccessoryNode accessory,
                              @NonNull String disabled)
            implements MessageTopLevelNode, ContainerChildNode {
    }

    /**
     * Defines a file display component.
     *
     * @param fileName The referenced attachment file name.
     * @param spoiler  The variable-capable spoiler value.
     * @since 1.0.0-beta.2
     */
    public record FileDisplayNode(@NonNull String fileName, @NonNull String spoiler)
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
     * @param spoiler     The variable-capable spoiler value.
     * @since 1.0.0-beta.2
     */
    public record MediaGalleryItemNode(@NonNull String url,
                                       @NonNull String description,
                                       @NonNull String spoiler) {
    }

    /**
     * Defines a button reference.
     *
     * @param id        The referenced button ID.
     * @param className The referenced button executor class.
     * @since 1.0.0-beta.2
     */
    public record ButtonRefNode(@Nullable String id,
                                @Nullable String className) implements ButtonNode {
    }

    /**
     * Defines a link button.
     *
     * @param url      The variable-capable target URL.
     * @param label    The variable-capable button label.
     * @param emoji    The variable-capable optional emoji.
     * @param disabled The variable-capable disabled value.
     * @since 1.0.0-beta.2
     */
    public record LinkButtonNode(@NonNull String url,
                                 @NonNull String label,
                                 @NonNull String emoji,
                                 @NonNull String disabled) implements ButtonNode {
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
     * @param spoiler     The variable-capable spoiler value.
     * @since 1.0.0-beta.2
     */
    public record ThumbnailNode(@NonNull String url,
                                @NonNull String description,
                                @NonNull String spoiler)
            implements SectionAccessoryNode {
    }
}
