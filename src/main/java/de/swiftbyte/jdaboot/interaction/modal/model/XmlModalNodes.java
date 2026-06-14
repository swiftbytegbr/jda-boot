package de.swiftbyte.jdaboot.interaction.modal.model;

import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * XML modal node definitions used by the runtime modal builder.
 *
 * @since 1.0.0-beta.2
 */
public final class XmlModalNodes {

    private XmlModalNodes() {
        // utility
    }

    public sealed interface LabelChildNode permits StringInputNode, FileInputNode, StringSelectNode, EntitySelectNode,
            CheckboxNode, CheckboxGroupNode, RadioGroupNode {
    }

    public record LabelNode(
            @NonNull String text,
            @NonNull String description,
            @NonNull LabelChildNode child
    ) {
    }

    public record StringInputNode(
            @NonNull String id,
            @NonNull TextInputStyle style,
            @NonNull String placeholder,
            boolean required,
            int maxLength,
            int minLength,
            @NonNull String defaultValue
    ) implements LabelChildNode {
    }

    public record FileInputNode(
            @NonNull String id,
            boolean required,
            int maxValues,
            int minValues
    ) implements LabelChildNode {
    }

    public record StringSelectOptionNode(
            @NonNull String label,
            @NonNull String value,
            @NonNull String description,
            boolean defaultOption
    ) {
    }

    public record StringSelectNode(
            @NonNull String id,
            @NonNull String placeholder,
            boolean required,
            int maxValues,
            int minValues,
            boolean disabled,
            @NonNull List<@NonNull StringSelectOptionNode> options
    ) implements LabelChildNode {
    }

    public record EntitySelectNode(
            @NonNull String id,
            @NonNull String placeholder,
            boolean required,
            int maxValues,
            int minValues,
            boolean disabled,
            @NonNull List<EntitySelectMenu.SelectTarget> targets,
            @NonNull List<ChannelType> channelTypes
    ) implements LabelChildNode {
    }

    public record CheckboxNode(
            @NonNull String id,
            boolean defaultValue
    ) implements LabelChildNode {
    }

    public record GroupOptionNode(
            @NonNull String label,
            @NonNull String value,
            @NonNull String description,
            boolean defaultOption
    ) {
    }

    public record CheckboxGroupNode(
            @NonNull String id,
            boolean required,
            int minValues,
            int maxValues,
            @NonNull List<@NonNull GroupOptionNode> options
    ) implements LabelChildNode {
    }

    public record RadioGroupNode(
            @NonNull String id,
            boolean required,
            @NonNull List<@NonNull GroupOptionNode> options
    ) implements LabelChildNode {
    }
}
