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

    /**
     * Prevents instantiation of this utility class.
     *
     * @since 1.0.0-beta.2
     */
    private XmlModalNodes() {
        // utility
    }

    /**
     * Marker interface for components that can be used as label children.
     *
     * @since 1.0.0-beta.2
     */
    public sealed interface LabelChildNode permits StringInputNode, FileInputNode, StringSelectNode, EntitySelectNode,
            CheckboxNode, CheckboxGroupNode, RadioGroupNode {
    }

    /**
     * Defines a modal label and its child component.
     *
     * @param text        The label text.
     * @param description The optional label description.
     * @param child       The child component.
     * @since 1.0.0-beta.2
     */
    public record LabelNode(
            @NonNull String text,
            @NonNull String description,
            @NonNull LabelChildNode child
    ) {
    }

    /**
     * Defines a text input component.
     *
     * @param id           The custom component ID.
     * @param style        The text input style.
     * @param placeholder  The optional placeholder.
     * @param required     Whether the input is required.
     * @param maxLength    The maximum input length.
     * @param minLength    The minimum input length.
     * @param defaultValue The optional default value.
     * @since 1.0.0-beta.2
     */
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

    /**
     * Defines a file input component.
     *
     * @param id        The custom component ID.
     * @param required  Whether the input is required.
     * @param maxValues The maximum number of files.
     * @param minValues The minimum number of files.
     * @since 1.0.0-beta.2
     */
    public record FileInputNode(
            @NonNull String id,
            boolean required,
            int maxValues,
            int minValues
    ) implements LabelChildNode {
    }

    /**
     * Defines an option of a string select component.
     *
     * @param label         The displayed option label.
     * @param value         The submitted option value.
     * @param description   The optional option description.
     * @param defaultOption Whether the option is selected by default.
     * @since 1.0.0-beta.2
     */
    public record StringSelectOptionNode(
            @NonNull String label,
            @NonNull String value,
            @NonNull String description,
            boolean defaultOption
    ) {
    }

    /**
     * Defines a string select component.
     *
     * @param id          The custom component ID.
     * @param placeholder The optional placeholder.
     * @param required    Whether the input is required.
     * @param maxValues   The maximum number of selected values.
     * @param minValues   The minimum number of selected values.
     * @param disabled    Whether the component is disabled.
     * @param options     The available options.
     * @since 1.0.0-beta.2
     */
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

    /**
     * Defines an entity select component.
     *
     * @param id           The custom component ID.
     * @param placeholder  The optional placeholder.
     * @param required     Whether the input is required.
     * @param maxValues    The maximum number of selected values.
     * @param minValues    The minimum number of selected values.
     * @param disabled     Whether the component is disabled.
     * @param targets      The selectable entity targets.
     * @param channelTypes The allowed channel types.
     * @since 1.0.0-beta.2
     */
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

    /**
     * Defines a checkbox component.
     *
     * @param id           The custom component ID.
     * @param defaultValue Whether the checkbox is selected by default.
     * @since 1.0.0-beta.2
     */
    public record CheckboxNode(
            @NonNull String id,
            boolean defaultValue
    ) implements LabelChildNode {
    }

    /**
     * Defines an option of a checkbox or radio group.
     *
     * @param label         The displayed option label.
     * @param value         The submitted option value.
     * @param description   The optional option description.
     * @param defaultOption Whether the option is selected by default.
     * @since 1.0.0-beta.2
     */
    public record GroupOptionNode(
            @NonNull String label,
            @NonNull String value,
            @NonNull String description,
            boolean defaultOption
    ) {
    }

    /**
     * Defines a checkbox group component.
     *
     * @param id        The custom component ID.
     * @param required  Whether the configured selection range is required.
     * @param minValues The minimum number of selected values, or {@code -1} when unset.
     * @param maxValues The maximum number of selected values, or {@code -1} when unset.
     * @param options   The available options.
     * @since 1.0.0-beta.2
     */
    public record CheckboxGroupNode(
            @NonNull String id,
            boolean required,
            int minValues,
            int maxValues,
            @NonNull List<@NonNull GroupOptionNode> options
    ) implements LabelChildNode {
    }

    /**
     * Defines a radio group component.
     *
     * @param id       The custom component ID.
     * @param required Whether an option must be selected.
     * @param options  The available options.
     * @since 1.0.0-beta.2
     */
    public record RadioGroupNode(
            @NonNull String id,
            boolean required,
            @NonNull List<@NonNull GroupOptionNode> options
    ) implements LabelChildNode {
    }
}
