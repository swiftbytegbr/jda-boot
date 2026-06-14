package de.swiftbyte.jdaboot.interaction.component.v2;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import de.swiftbyte.jdaboot.exceptions.ElementBuildException;
import de.swiftbyte.jdaboot.exceptions.ElementNotFoundException;
import de.swiftbyte.jdaboot.exceptions.ObjectInitializationException;
import de.swiftbyte.jdaboot.exceptions.StillInitializingException;
import de.swiftbyte.jdaboot.interaction.button.AdvancedButton;
import de.swiftbyte.jdaboot.interaction.button.ButtonExecutor;
import de.swiftbyte.jdaboot.interaction.button.ButtonManager;
import de.swiftbyte.jdaboot.interaction.button.TemplateButton;
import de.swiftbyte.jdaboot.interaction.component.v2.model.ComponentV2Nodes;
import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import de.swiftbyte.jdaboot.interaction.selection.AdvancedSelectMenu;
import de.swiftbyte.jdaboot.interaction.selection.EntitySelectMenuExecutor;
import de.swiftbyte.jdaboot.interaction.selection.SelectMenuManager;
import de.swiftbyte.jdaboot.interaction.selection.StringSelectMenuExecutor;
import de.swiftbyte.jdaboot.interaction.selection.TemplateSelectMenu;
import de.swiftbyte.jdaboot.variables.VariableProcessor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent;
import net.dv8tion.jda.api.components.section.SectionContentComponent;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds JDA Component V2 trees from XML templates with runtime variable support.
 *
 * @since 1.0.0-beta.2
 */
public class AdvancedComponentV2 {

    private final @NonNull TemplateComponentV2 template;

    @Getter
    @Setter
    private @NonNull DiscordLocale locale;

    private final @NonNull HashMap<@NonNull String, @NonNull String> variables = new HashMap<>();

    /**
     * Creates a Component V2 builder.
     *
     * @param template The source template.
     * @param locale   The locale used for variable processing.
     * @since 1.0.0-beta.2
     */
    protected AdvancedComponentV2(@NonNull TemplateComponentV2 template, @NonNull DiscordLocale locale) {
        this.template = template;
        this.locale = locale;
    }

    /**
     * Sets a variable used for component rendering.
     *
     * @param key   The variable key.
     * @param value The variable value.
     * @return This builder.
     * @since 1.0.0-beta.2
     */
    public @NonNull AdvancedComponentV2 setVariable(@NonNull String key, @NonNull String value) {
        variables.put(key, value);
        return this;
    }

    /**
     * Appends multiple variables used for component rendering.
     *
     * @param variables The variables to append.
     * @return The AdvancedComponentV2 instance for chaining.
     * @since 1.0.0-beta.2
     */
    public @NonNull AdvancedComponentV2 setVariables(@NonNull Map<@NonNull String, @NonNull String> variables) {
        this.variables.putAll(variables);
        return this;
    }

    /**
     * Builds the configured message components.
     *
     * @return The built top-level components.
     * @since 1.0.0-beta.2
     */
    public @NonNull List<@NonNull MessageTopLevelComponent> build() {
        try {
            List<MessageTopLevelComponent> components = new ArrayList<>();
            for (ComponentV2Nodes.MessageTopLevelNode node : template.getDefinition().components()) {
                components.add(buildMessageComponent(node));
            }
            return components;
        } catch (Exception e) {
            throw new ElementBuildException("Failed to build component layout", sourceReference(), e);
        }
    }

    /**
     * Builds a message top-level component.
     *
     * @param node The source XML node.
     * @return The built component.
     * @since 1.0.0-beta.2
     */
    private @NonNull MessageTopLevelComponent buildMessageComponent(ComponentV2Nodes.MessageTopLevelNode node) {
        if (node instanceof ComponentV2Nodes.TextDisplayNode textNode) {
            return TextDisplay.of(processVar(textNode.content()));
        }
        if (node instanceof ComponentV2Nodes.SeparatorNode separatorNode) {
            return Separator.create(separatorNode.divider(), separatorNode.spacing());
        }
        if (node instanceof ComponentV2Nodes.FileDisplayNode fileDisplayNode) {
            return buildFileDisplay(fileDisplayNode);
        }
        if (node instanceof ComponentV2Nodes.MediaGalleryNode mediaGalleryNode) {
            return buildMediaGallery(mediaGalleryNode);
        }
        if (node instanceof ComponentV2Nodes.ActionRowNode actionRowNode) {
            return buildActionRow(actionRowNode);
        }
        if (node instanceof ComponentV2Nodes.ContainerNode containerNode) {
            return buildContainer(containerNode);
        }
        if (node instanceof ComponentV2Nodes.SectionNode sectionNode) {
            return buildSection(sectionNode);
        }

        throw new ObjectInitializationException("Unsupported message node type: " + node.getClass().getName(), node.getClass(), sourceReference());
    }

    /**
     * Builds an action row.
     *
     * @param node The source XML node.
     * @return The built action row.
     * @since 1.0.0-beta.2
     */
    private @NonNull ActionRow buildActionRow(ComponentV2Nodes.ActionRowNode node) {
        List<ActionRowChildComponent> children = new ArrayList<>();
        for (ComponentV2Nodes.ActionRowChildNode childNode : node.children()) {
            children.add(buildActionRowChild(childNode));
        }
        return ActionRow.of(children);
    }

    /**
     * Builds an action row child component.
     *
     * @param node The source XML node.
     * @return The built child component.
     * @since 1.0.0-beta.2
     */
    private @NonNull ActionRowChildComponent buildActionRowChild(ComponentV2Nodes.ActionRowChildNode node) {
        if (node instanceof ComponentV2Nodes.ButtonRefNode buttonRef) {
            return buildButton(buttonRef);
        }
        if (node instanceof ComponentV2Nodes.StringSelectRefNode stringRef) {
            return buildStringSelect(stringRef);
        }
        if (node instanceof ComponentV2Nodes.EntitySelectRefNode entityRef) {
            return buildEntitySelect(entityRef);
        }

        throw new ObjectInitializationException("Unsupported action-row child type: " + node.getClass().getName(), node.getClass(), sourceReference());
    }

    /**
     * Builds a container.
     *
     * @param node The source XML node.
     * @return The built container.
     * @since 1.0.0-beta.2
     */
    private @NonNull Container buildContainer(ComponentV2Nodes.ContainerNode node) {
        List<ContainerChildComponent> children = new ArrayList<>();
        for (ComponentV2Nodes.ContainerChildNode childNode : node.children()) {
            children.add(buildContainerChild(childNode));
        }

        Container container = Container.of(children);
        if (node.accentColor() != null) {
            container = container.withAccentColor(node.accentColor());
        }
        container = container.withSpoiler(node.spoiler());
        container = container.withDisabled(node.disabled());
        return container;
    }

    /**
     * Builds a container child component.
     *
     * @param node The source XML node.
     * @return The built child component.
     * @since 1.0.0-beta.2
     */
    private @NonNull ContainerChildComponent buildContainerChild(ComponentV2Nodes.ContainerChildNode node) {
        if (node instanceof ComponentV2Nodes.TextDisplayNode textNode) {
            return TextDisplay.of(processVar(textNode.content()));
        }
        if (node instanceof ComponentV2Nodes.SeparatorNode separatorNode) {
            return Separator.create(separatorNode.divider(), separatorNode.spacing());
        }
        if (node instanceof ComponentV2Nodes.FileDisplayNode fileDisplayNode) {
            return buildFileDisplay(fileDisplayNode);
        }
        if (node instanceof ComponentV2Nodes.MediaGalleryNode mediaGalleryNode) {
            return buildMediaGallery(mediaGalleryNode);
        }
        if (node instanceof ComponentV2Nodes.ActionRowNode actionRowNode) {
            return buildActionRow(actionRowNode);
        }
        if (node instanceof ComponentV2Nodes.SectionNode sectionNode) {
            return buildSection(sectionNode);
        }

        throw new ObjectInitializationException("Unsupported container child type: " + node.getClass().getName(), node.getClass(), sourceReference());
    }

    /**
     * Builds a section.
     *
     * @param node The source XML node.
     * @return The built section.
     * @since 1.0.0-beta.2
     */
    private @NonNull Section buildSection(ComponentV2Nodes.SectionNode node) {
        List<SectionContentComponent> content = new ArrayList<>();
        for (ComponentV2Nodes.SectionContentNode contentNode : node.content()) {
            content.add(buildSectionContent(contentNode));
        }

        Section section = Section.of(buildSectionAccessory(node.accessory()), content);
        return section.withDisabled(node.disabled());
    }

    /**
     * Builds a section content component.
     *
     * @param node The source XML node.
     * @return The built content component.
     * @since 1.0.0-beta.2
     */
    private @NonNull SectionContentComponent buildSectionContent(ComponentV2Nodes.SectionContentNode node) {
        if (node instanceof ComponentV2Nodes.TextDisplayNode textNode) {
            return TextDisplay.of(processVar(textNode.content()));
        }

        throw new ObjectInitializationException("Unsupported section content type: " + node.getClass().getName(), node.getClass(), sourceReference());
    }

    /**
     * Builds a section accessory component.
     *
     * @param node The source XML node.
     * @return The built accessory component.
     * @since 1.0.0-beta.2
     */
    private @NonNull SectionAccessoryComponent buildSectionAccessory(ComponentV2Nodes.SectionAccessoryNode node) {
        if (node instanceof ComponentV2Nodes.ButtonRefNode buttonRef) {
            return buildButton(buttonRef);
        }
        if (node instanceof ComponentV2Nodes.ThumbnailNode thumbnailNode) {
            Thumbnail thumbnail = Thumbnail.fromUrl(processVar(thumbnailNode.url()));
            if (!thumbnailNode.description().isBlank()) {
                thumbnail = thumbnail.withDescription(processVar(thumbnailNode.description()));
            }
            return thumbnail.withSpoiler(thumbnailNode.spoiler());
        }

        throw new ObjectInitializationException("Unsupported section accessory type: " + node.getClass().getName(), node.getClass(), sourceReference());
    }

    /**
     * Builds a file display component.
     *
     * @param node The source XML node.
     * @return The built file display.
     * @since 1.0.0-beta.2
     */
    private @NonNull FileDisplay buildFileDisplay(ComponentV2Nodes.FileDisplayNode node) {
        return FileDisplay.fromFileName(processVar(node.fileName())).withSpoiler(node.spoiler());
    }

    /**
     * Builds a media gallery.
     *
     * @param node The source XML node.
     * @return The built media gallery.
     * @since 1.0.0-beta.2
     */
    private @NonNull MediaGallery buildMediaGallery(ComponentV2Nodes.MediaGalleryNode node) {
        List<MediaGalleryItem> items = new ArrayList<>();

        for (ComponentV2Nodes.MediaGalleryItemNode itemNode : node.items()) {
            MediaGalleryItem item = MediaGalleryItem.fromUrl(processVar(itemNode.url()));
            if (!itemNode.description().isBlank()) {
                item = item.withDescription(processVar(itemNode.description()));
            }
            item = item.withSpoiler(itemNode.spoiler());
            items.add(item);
        }

        return MediaGallery.of(items);
    }

    /**
     * Resolves and builds a referenced button.
     *
     * @param ref The button reference.
     * @return The built button.
     * @since 1.0.0-beta.2
     */
    private @NonNull Button buildButton(ComponentV2Nodes.ButtonRefNode ref) {
        ButtonManager buttonManager = JDABootConfigurationManager.getButtonManager();
        if (buttonManager == null) {
            throw new StillInitializingException();
        }

        TemplateButton templateButton;
        if (ref.id() != null && !ref.id().isBlank()) {
            templateButton = buttonManager.getButton(processVar(ref.id()));
        } else if (ref.className() != null && !ref.className().isBlank()) {
            Class<? extends ButtonExecutor> clazz = loadAndValidateClass(processVar(ref.className()), ButtonExecutor.class);
            templateButton = buttonManager.getButton(clazz);
        } else {
            throw new ObjectInitializationException("button-ref must define id or class", ref.getClass(), sourceReference());
        }

        if (templateButton == null) {
            throw new ElementNotFoundException("Could not find button in Component V2 layout (XML: " + sourceReference() + ")", ref.id() != null ? processVar(ref.id()) : processVar(ref.className()));
        }

        AdvancedButton advancedButton = templateButton.advancedButton(locale);
        applyVariables(advancedButton::setVariable);
        return advancedButton.build();
    }

    /**
     * Resolves and builds a referenced string select menu.
     *
     * @param ref The select menu reference.
     * @return The built string select menu.
     * @since 1.0.0-beta.2
     */
    private @NonNull StringSelectMenu buildStringSelect(ComponentV2Nodes.StringSelectRefNode ref) {
        SelectMenuManager selectMenuManager = JDABootConfigurationManager.getSelectMenuManager();
        if (selectMenuManager == null) {
            throw new StillInitializingException();
        }

        TemplateSelectMenu templateSelectMenu;
        if (ref.id() != null && !ref.id().isBlank()) {
            templateSelectMenu = selectMenuManager.getSelectMenu(processVar(ref.id()));
        } else if (ref.className() != null && !ref.className().isBlank()) {
            Class<? extends StringSelectMenuExecutor> clazz = loadAndValidateClass(processVar(ref.className()), StringSelectMenuExecutor.class);
            templateSelectMenu = selectMenuManager.getStringSelectMenu(clazz);
        } else {
            throw new ObjectInitializationException("string-select-ref must define id or class", ref.getClass(), sourceReference());
        }

        if (templateSelectMenu == null) {
            throw new ElementNotFoundException("Could not find select menu in Component V2 layout (XML: " + sourceReference() + ")", ref.id() != null ? processVar(ref.id()) : processVar(ref.className()));
        }

        AdvancedSelectMenu advancedSelectMenu = templateSelectMenu.advancedSelectMenu(locale);
        applyVariables(advancedSelectMenu::setVariable);

        SelectMenu builtMenu = advancedSelectMenu.build();
        if (builtMenu instanceof StringSelectMenu stringSelectMenu) {
            return stringSelectMenu;
        }
        throw new ObjectInitializationException("Select menu is not a string select menu", builtMenu.getClass(), sourceReference());
    }

    /**
     * Resolves and builds a referenced entity select menu.
     *
     * @param ref The select menu reference.
     * @return The built entity select menu.
     * @since 1.0.0-beta.2
     */
    private @NonNull EntitySelectMenu buildEntitySelect(ComponentV2Nodes.EntitySelectRefNode ref) {
        SelectMenuManager selectMenuManager = JDABootConfigurationManager.getSelectMenuManager();
        if (selectMenuManager == null) {
            throw new StillInitializingException();
        }

        TemplateSelectMenu templateSelectMenu;
        if (ref.id() != null && !ref.id().isBlank()) {
            templateSelectMenu = selectMenuManager.getSelectMenu(processVar(ref.id()));
        } else if (ref.className() != null && !ref.className().isBlank()) {
            Class<? extends EntitySelectMenuExecutor> clazz = loadAndValidateClass(processVar(ref.className()), EntitySelectMenuExecutor.class);
            templateSelectMenu = selectMenuManager.getEntitySelectMenu(clazz);
        } else {
            throw new ObjectInitializationException("entity-select-ref must define id or class", ref.getClass(), sourceReference());
        }

        if (templateSelectMenu == null) {
            throw new ElementNotFoundException("Could not find select menu in Component V2 layout (XML: " + sourceReference() + ")", ref.id() != null ? processVar(ref.id()) : processVar(ref.className()));
        }

        AdvancedSelectMenu advancedSelectMenu = templateSelectMenu.advancedSelectMenu(locale);
        applyVariables(advancedSelectMenu::setVariable);

        SelectMenu builtMenu = advancedSelectMenu.build();
        if (builtMenu instanceof EntitySelectMenu entitySelectMenu) {
            return entitySelectMenu;
        }
        throw new ObjectInitializationException("Select menu is not an entity select menu", builtMenu.getClass(), sourceReference());
    }

    /**
     * Loads a class and verifies that it implements the required type.
     *
     * @param className  The class name.
     * @param targetType The required target type.
     * @param <T>        The required type.
     * @return The validated class.
     * @since 1.0.0-beta.2
     */
    @SuppressWarnings("unchecked")
    private <T> @NonNull Class<? extends T> loadAndValidateClass(@NonNull String className, @NonNull Class<T> targetType) {
        try {
            Class<?> rawClass = Class.forName(className);
            if (!targetType.isAssignableFrom(rawClass)) {
                throw new ObjectInitializationException(String.format(
                        "Class '%s' is not assignable to %s",
                        className, targetType.getName()
                ), rawClass, sourceReference());
            }
            return (Class<? extends T>) rawClass;
        } catch (ClassNotFoundException e) {
            throw new ObjectInitializationException("Could not load class: " + className, sourceReference(), e);
        }
    }

    /**
     * Applies default and runtime variables to another advanced builder.
     *
     * @param receiver The variable receiver.
     * @since 1.0.0-beta.2
     */
    private void applyVariables(@NonNull VariableReceiver receiver) {
        for (XmlDefaultVariable defaultVar : template.getDefinition().defaultVars()) {
            receiver.accept(defaultVar.key(), defaultVar.value());
        }
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            receiver.accept(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Processes variables in an XML value.
     *
     * @param old The configured value.
     * @return The processed value.
     * @since 1.0.0-beta.2
     */
    private @NonNull String processVar(@NonNull String old) {
        return VariableProcessor.processVariable(locale, old, variables, template.getDefinition().defaultVars());
    }

    /**
     * Returns the source location used for error reporting.
     *
     * @return The XML source reference.
     * @since 1.0.0-beta.2
     */
    private @NonNull String sourceReference() {
        return template.getDefinition().sourcePath() + ", layout: " + template.getDefinition().id();
    }

    /**
     * Receives variables applied to referenced templates.
     *
     * @since 1.0.0-beta.2
     */
    @FunctionalInterface
    private interface VariableReceiver {
        /**
         * Accepts a variable.
         *
         * @param key   The variable key.
         * @param value The variable value.
         * @since 1.0.0-beta.2
         */
        void accept(@NonNull String key, @NonNull String value);
    }
}
