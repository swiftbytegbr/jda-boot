package de.swiftbyte.jdaboot.interaction.modal.model;

import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Immutable definition for one XML modal layout.
 *
 * @param id             The layout ID.
 * @param sourcePath     The source XML resource path.
 * @param modalId        The referenced modal ID, or {@code null}.
 * @param modalClassName The referenced modal class name, or {@code null}.
 * @param title          The modal title.
 * @param defaultVars    The default variables.
 * @param labels         The modal labels.
 * @since 1.0.0-beta.2
 */
public record XmlModalLayoutDefinition(
        @NonNull String id,
        @NonNull String sourcePath,
        @Nullable String modalId,
        @Nullable String modalClassName,
        @NonNull String title,
        @NonNull XmlDefaultVariable @NonNull [] defaultVars,
        @NonNull List<XmlModalNodes.LabelNode> labels
) {
}
