package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.exceptions.ConfigurationException;
import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import de.swiftbyte.jdaboot.interaction.modal.model.XmlModalLayoutDefinition;
import de.swiftbyte.jdaboot.interaction.modal.model.XmlModalNodes;
import de.swiftbyte.jdaboot.xml.XmlLoaderSupport;
import lombok.CustomLog;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.FileType;
import org.jspecify.annotations.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.validation.Schema;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.booleanAttribute;
import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.childElements;
import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.intAttribute;
import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.nodeName;
import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.optionalAttribute;
import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.requiredAttribute;

/**
 * Loads modal layouts from an XML resource and validates against the bundled XSD.
 *
 * @since 1.0.0-beta.2
 */
@CustomLog
public final class ModalXmlLoader {

    private static final @NonNull String XSD_RESOURCE = "de/swiftbyte/jdaboot/schema/modals.xsd";
    private static final @NonNull Schema SCHEMA = XmlLoaderSupport.loadBundledSchema(
            XSD_RESOURCE,
            ModalXmlLoader.class.getClassLoader()
    );

    /**
     * Prevents instantiation of this utility class.
     *
     * @since 1.0.0-beta.2
     */
    private ModalXmlLoader() {
        // utility
    }

    /**
     * Loads all modal layouts from an XML classpath resource.
     *
     * @param mainClass    The application class whose class loader resolves the resource.
     * @param resourcePath The classpath path of the XML resource.
     * @return The parsed modal layouts indexed by layout ID.
     * @since 1.0.0-beta.2
     */
    public static @NonNull Map<@NonNull String, @NonNull XmlModalLayoutDefinition> load(@NonNull Class<?> mainClass,
                                                                                        @NonNull String resourcePath) {
        if (resourcePath.isBlank()) {
            return Map.of();
        }

        try (InputStream xmlStream = mainClass.getClassLoader().getResourceAsStream(resourcePath)) {
            if (xmlStream == null) {
                log.debug("No modal XML found at '{}'. Skipping XML modal load.", resourcePath);
                return Map.of();
            }

            Map<String, XmlModalLayoutDefinition> definitions = load(xmlStream, resourcePath);
            log.info("Loaded {} modal layout(s) from {}", definitions.size(), resourcePath);
            return definitions;
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load modal XML", resourcePath, e);
        }
    }

    /**
     * Loads all modal layouts from an XML stream.
     * The caller remains responsible for closing the stream.
     *
     * @param xmlStream       The XML input stream.
     * @param sourceReference A description of the XML source used in error messages.
     * @return The parsed modal layouts indexed by layout ID.
     * @since 1.0.0-beta.2
     */
    static @NonNull Map<@NonNull String, @NonNull XmlModalLayoutDefinition> load(
            @NonNull InputStream xmlStream,
            @NonNull String sourceReference) {
        try {
            Document document = XmlLoaderSupport.parseDocument(xmlStream, SCHEMA);
            Element root = document.getDocumentElement();
            if (!"modals".equals(nodeName(root))) {
                throw new ConfigurationException(String.format(
                        "Invalid root element '%s'. Expected 'modals'",
                        nodeName(root)), sourceReference);
            }

            HashMap<String, XmlModalLayoutDefinition> definitions = new HashMap<>();
            for (Element layoutElement : childElements(root)) {
                if (!"layout".equals(nodeName(layoutElement))) {
                    throw new ConfigurationException(String.format(
                            "Unsupported top-level element <%s>. Only <layout> is allowed",
                            nodeName(layoutElement)), sourceReference);
                }

                XmlModalLayoutDefinition definition = parseLayout(layoutElement, sourceReference);
                if (definitions.containsKey(definition.id())) {
                    throw new ConfigurationException(String.format(
                            "Duplicate modal layout id '%s'",
                            definition.id()), sourceReference);
                }
                definitions.put(definition.id(), definition);
            }

            return Collections.unmodifiableMap(definitions);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load modal XML", sourceReference, e);
        }
    }

    /**
     * Parses a modal layout element.
     *
     * @param layoutElement The layout element.
     * @param resourcePath  The XML resource path used for error reporting.
     * @return The parsed modal layout definition.
     * @since 1.0.0-beta.2
     */
    private static @NonNull XmlModalLayoutDefinition parseLayout(@NonNull Element layoutElement,
                                                                 @NonNull String resourcePath) {
        String id = requiredAttribute(layoutElement, "id", resourcePath);
        String title = requiredAttribute(layoutElement, "title", resourcePath);
        String modalId = optionalAttribute(layoutElement, "modal-id");
        String modalClassName = optionalAttribute(layoutElement, "modal-class");

        boolean hasModalId = !modalId.isBlank();
        boolean hasModalClass = !modalClassName.isBlank();
        if (hasModalId == hasModalClass) {
            throw new ConfigurationException(
                    String.format("Layout '%s' must define exactly one of attributes 'modal-id' or 'modal-class'", id),
                    resourcePath
            );
        }

        List<XmlDefaultVariable> defaultVars = new ArrayList<>();
        List<XmlModalNodes.LabelNode> labels = new ArrayList<>();
        boolean hasDefaultVariablesBlock = false;
        boolean hasLabels = false;

        for (Element child : childElements(layoutElement)) {
            String nodeName = nodeName(child);
            if ("default-variables".equals(nodeName)) {
                if (hasDefaultVariablesBlock) {
                    throw new ConfigurationException("<layout> can contain <default-variables> only once", resourcePath);
                }
                if (hasLabels) {
                    throw new ConfigurationException("<default-variables> must be defined before <label> nodes", resourcePath);
                }
                defaultVars.addAll(parseDefaultVariables(child, resourcePath));
                hasDefaultVariablesBlock = true;
                continue;
            }

            if ("label".equals(nodeName)) {
                hasLabels = true;
                labels.add(parseLabelNode(child, resourcePath));
                continue;
            }

            throw new ConfigurationException(String.format(
                    "Unsupported <layout> child <%s>. Allowed: default-variables, label",
                    nodeName
            ), resourcePath);
        }

        if (labels.isEmpty()) {
            throw new ConfigurationException(String.format(
                    "Layout '%s' must contain at least one <label>",
                    id
            ), resourcePath);
        }

        if (labels.size() > 5) {
            throw new ConfigurationException(String.format(
                    "Layout '%s' can contain at most 5 labels",
                    id
            ), resourcePath);
        }

        return new XmlModalLayoutDefinition(
                id,
                resourcePath,
                hasModalId ? modalId : null,
                hasModalClass ? modalClassName : null,
                title,
                defaultVars.toArray(new XmlDefaultVariable[0]),
                List.copyOf(labels)
        );
    }

    /**
     * Parses modal default variables.
     *
     * @param element      The default variables element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed default variables.
     * @since 1.0.0-beta.2
     */
    private static @NonNull List<@NonNull XmlDefaultVariable> parseDefaultVariables(@NonNull Element element,
                                                                                    @NonNull String resourcePath) {
        List<XmlDefaultVariable> defaultVars = new ArrayList<>();
        for (XmlLoaderSupport.XmlVariableDefinition variableDefinition : XmlLoaderSupport.parseDefaultVariables(element, resourcePath)) {
            defaultVars.add(new XmlDefaultVariable(variableDefinition.key(), variableDefinition.value()));
        }
        return defaultVars;
    }

    /**
     * Parses a modal label and its child component.
     *
     * @param element      The label element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed label node.
     * @since 1.0.0-beta.2
     */
    private static XmlModalNodes.LabelNode parseLabelNode(@NonNull Element element,
                                                          @NonNull String resourcePath) {
        String text = requiredAttribute(element, "text", resourcePath);
        String description = optionalAttribute(element, "description");
        List<Element> children = childElements(element);

        if (children.size() != 1) {
            throw new ConfigurationException("<label> must contain exactly one child input element", resourcePath);
        }

        Element child = children.get(0);
        String nodeName = nodeName(child);
        XmlModalNodes.LabelChildNode childNode = switch (nodeName) {
            case "string-input" -> parseStringInputNode(child, resourcePath);
            case "file-input" -> parseFileInputNode(child, resourcePath);
            case "string-select" -> parseStringSelectNode(child, resourcePath);
            case "entity-select" -> parseEntitySelectNode(child, resourcePath);
            case "checkbox" -> parseCheckboxNode(child, resourcePath);
            case "checkbox-group" -> parseCheckboxGroupNode(child, resourcePath);
            case "radio-group" -> parseRadioGroupNode(child, resourcePath);
            default -> throw new ConfigurationException(String.format(
                    "Unsupported <label> child <%s>. Allowed: string-input, file-input, string-select, entity-select, checkbox, checkbox-group, radio-group",
                    nodeName
            ), resourcePath);
        };

        return new XmlModalNodes.LabelNode(text, description, childNode);
    }

    /**
     * Parses a string input component.
     *
     * @param element      The string input element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed string input node.
     * @since 1.0.0-beta.2
     */
    private static XmlModalNodes.StringInputNode parseStringInputNode(@NonNull Element element,
                                                                      @NonNull String resourcePath) {
        String id = requiredAttribute(element, "id", resourcePath);
        String styleValue = optionalAttribute(element, "style");
        String placeholder = optionalAttribute(element, "placeholder");
        boolean required = booleanAttribute(element, "required", false);
        int maxLength = intAttribute(element, "max-length", 0, resourcePath);
        int minLength = intAttribute(element, "min-length", 0, resourcePath);
        String defaultValue = optionalAttribute(element, "value");

        if (minLength < 0) {
            throw new ConfigurationException("<string-input> min-length cannot be negative", resourcePath);
        }
        if (maxLength < 0) {
            throw new ConfigurationException("<string-input> max-length cannot be negative", resourcePath);
        }
        if (maxLength > 0 && minLength > maxLength) {
            throw new ConfigurationException("<string-input> min-length cannot be greater than max-length", resourcePath);
        }

        TextInputStyle style;
        if (styleValue.isBlank()) {
            style = TextInputStyle.SHORT;
        } else {
            try {
                style = TextInputStyle.valueOf(styleValue.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ConfigurationException(String.format(
                        "Invalid string-input style '%s'",
                        styleValue
                ), resourcePath, e);
            }
        }

        if (style == TextInputStyle.UNKNOWN) {
            throw new ConfigurationException("TextInputStyle.UNKNOWN is not allowed in XML modal string-input", resourcePath);
        }

        return new XmlModalNodes.StringInputNode(
                id,
                style,
                placeholder,
                required,
                maxLength,
                minLength,
                defaultValue
        );
    }

    /**
     * Parses a file input component.
     *
     * @param element      The file input element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed file input node.
     * @since 1.0.0-beta.2
     */
    private static XmlModalNodes.FileInputNode parseFileInputNode(@NonNull Element element,
                                                                  @NonNull String resourcePath) {
        String id = requiredAttribute(element, "id", resourcePath);
        boolean required = booleanAttribute(element, "required", false);
        int minValues = intAttribute(element, "min-values", required ? 1 : 0, resourcePath);
        int maxValues = intAttribute(element, "max-values", 1, resourcePath);
        List<FileType> fileTypes = new ArrayList<>();

        for (Element child : childElements(element)) {
            String childName = nodeName(child);
            if (!"file-type".equals(childName)) {
                throw new ConfigurationException(String.format(
                        "Unsupported <file-input> child <%s>. Only <file-type> is allowed",
                        childName
                ), resourcePath);
            }

            String value = requiredAttribute(child, "value", resourcePath);
            try {
                fileTypes.add(switch (value) {
                    case "image" -> FileType.IMAGE;
                    case "video" -> FileType.VIDEO;
                    case "audio" -> FileType.AUDIO;
                    default -> FileType.ofExtension(value);
                });
            } catch (IllegalArgumentException e) {
                throw new ConfigurationException(String.format(
                        "Invalid file type '%s' in <file-input>",
                        value
                ), resourcePath, e);
            }
        }

        if (minValues < 0) {
            throw new ConfigurationException("<file-input> min-values cannot be negative", resourcePath);
        }
        if (maxValues <= 0) {
            throw new ConfigurationException("<file-input> max-values must be greater than 0", resourcePath);
        }
        if (required && minValues == 0) {
            throw new ConfigurationException("<file-input> min-values must be at least 1 when required=true", resourcePath);
        }
        if (minValues > maxValues) {
            throw new ConfigurationException("<file-input> min-values cannot be greater than max-values", resourcePath);
        }

        return new XmlModalNodes.FileInputNode(id, required, maxValues, minValues, List.copyOf(fileTypes));
    }

    /**
     * Parses a string select component.
     *
     * @param element      The string select element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed string select node.
     * @since 1.0.0-beta.2
     */
    private static XmlModalNodes.StringSelectNode parseStringSelectNode(@NonNull Element element,
                                                                        @NonNull String resourcePath) {
        SelectSettings settings = parseSelectSettings(element, resourcePath);
        List<XmlModalNodes.StringSelectOptionNode> options = new ArrayList<>();

        for (Element child : childElements(element)) {
            String childName = nodeName(child);
            if (!"option".equals(childName)) {
                throw new ConfigurationException(String.format(
                        "Unsupported <string-select> child <%s>. Only <option> is allowed",
                        childName
                ), resourcePath);
            }

            options.add(new XmlModalNodes.StringSelectOptionNode(
                    requiredAttribute(child, "label", resourcePath),
                    requiredAttribute(child, "value", resourcePath),
                    optionalAttribute(child, "description"),
                    booleanAttribute(child, "default", false)
            ));
        }

        if (options.isEmpty()) {
            throw new ConfigurationException("<string-select> must contain at least one <option>", resourcePath);
        }

        return new XmlModalNodes.StringSelectNode(
                settings.id(),
                settings.placeholder(),
                settings.required(),
                settings.maxValues(),
                settings.minValues(),
                settings.disabled(),
                List.copyOf(options)
        );
    }

    /**
     * Parses an entity select component.
     *
     * @param element      The entity select element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed entity select node.
     * @since 1.0.0-beta.2
     */
    private static XmlModalNodes.EntitySelectNode parseEntitySelectNode(@NonNull Element element,
                                                                        @NonNull String resourcePath) {
        SelectSettings settings = parseSelectSettings(element, resourcePath);
        Set<EntitySelectMenu.SelectTarget> targets = java.util.EnumSet.noneOf(EntitySelectMenu.SelectTarget.class);
        Set<ChannelType> channelTypes = java.util.EnumSet.noneOf(ChannelType.class);

        for (Element child : childElements(element)) {
            String childName = nodeName(child);
            switch (childName) {
                case "target" -> {
                    String targetValue = requiredAttribute(child, "type", resourcePath);
                    try {
                        targets.add(EntitySelectMenu.SelectTarget.valueOf(targetValue.trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        throw new ConfigurationException(
                                String.format("Invalid entity select target '%s'", targetValue),
                                resourcePath,
                                e
                        );
                    }
                }
                case "channel-type" -> {
                    String typeValue = requiredAttribute(child, "value", resourcePath);
                    try {
                        channelTypes.add(ChannelType.valueOf(typeValue.trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        throw new ConfigurationException(
                                String.format("Invalid channel type '%s' in <entity-select>", typeValue),
                                resourcePath,
                                e
                        );
                    }
                }
                default -> throw new ConfigurationException(String.format(
                        "Unsupported <entity-select> child <%s>. Allowed: target, channel-type",
                        childName
                ), resourcePath);
            }
        }

        if (targets.isEmpty()) {
            throw new ConfigurationException("<entity-select> must contain at least one <target>", resourcePath);
        }

        if (!targets.contains(EntitySelectMenu.SelectTarget.CHANNEL) && !channelTypes.isEmpty()) {
            throw new ConfigurationException("<entity-select> channel-type is only allowed when target CHANNEL is configured", resourcePath);
        }

        return new XmlModalNodes.EntitySelectNode(
                settings.id(),
                settings.placeholder(),
                settings.required(),
                settings.maxValues(),
                settings.minValues(),
                settings.disabled(),
                List.copyOf(targets),
                List.copyOf(channelTypes)
        );
    }

    /**
     * Parses a checkbox component.
     *
     * @param element      The checkbox element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed checkbox node.
     * @since 1.0.0-beta.2
     */
    private static XmlModalNodes.CheckboxNode parseCheckboxNode(@NonNull Element element,
                                                                @NonNull String resourcePath) {
        return new XmlModalNodes.CheckboxNode(
                requiredAttribute(element, "id", resourcePath),
                booleanAttribute(element, "default", false)
        );
    }

    /**
     * Parses a checkbox group component.
     *
     * @param element      The checkbox group element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed checkbox group node.
     * @since 1.0.0-beta.2
     */
    private static XmlModalNodes.CheckboxGroupNode parseCheckboxGroupNode(@NonNull Element element,
                                                                          @NonNull String resourcePath) {
        String id = requiredAttribute(element, "id", resourcePath);
        boolean required = booleanAttribute(element, "required", true);
        int minValues = intAttribute(element, "min-values", -1, resourcePath);
        int maxValues = intAttribute(element, "max-values", -1, resourcePath);
        List<XmlModalNodes.GroupOptionNode> options = parseGroupOptions(element, resourcePath);

        if (minValues < -1 || minValues > 10) {
            throw new ConfigurationException("<checkbox-group> min-values must be between 0 and 10", resourcePath);
        }
        if (maxValues != -1 && (maxValues <= 0 || maxValues > 10)) {
            throw new ConfigurationException("<checkbox-group> max-values must be between 1 and 10", resourcePath);
        }
        if (minValues != -1 && maxValues != -1 && minValues > maxValues) {
            throw new ConfigurationException("<checkbox-group> min-values cannot be greater than max-values", resourcePath);
        }
        if (required && minValues == 0) {
            throw new ConfigurationException("<checkbox-group> min-values cannot be 0 when required=true", resourcePath);
        }

        long defaultOptions = options.stream().filter(XmlModalNodes.GroupOptionNode::defaultOption).count();
        if (defaultOptions > 0 && minValues != -1 && defaultOptions < minValues) {
            throw new ConfigurationException("<checkbox-group> has fewer default options than min-values", resourcePath);
        }
        if (defaultOptions > 0 && maxValues != -1 && defaultOptions > maxValues) {
            throw new ConfigurationException("<checkbox-group> has more default options than max-values", resourcePath);
        }

        return new XmlModalNodes.CheckboxGroupNode(id, required, minValues, maxValues, List.copyOf(options));
    }

    /**
     * Parses a radio group component.
     *
     * @param element      The radio group element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed radio group node.
     * @since 1.0.0-beta.2
     */
    private static XmlModalNodes.RadioGroupNode parseRadioGroupNode(@NonNull Element element,
                                                                    @NonNull String resourcePath) {
        String id = requiredAttribute(element, "id", resourcePath);
        boolean required = booleanAttribute(element, "required", true);
        List<XmlModalNodes.GroupOptionNode> options = parseGroupOptions(element, resourcePath);

        if (options.size() < 2) {
            throw new ConfigurationException("<radio-group> must contain at least two <option> elements", resourcePath);
        }
        long defaultOptions = options.stream().filter(XmlModalNodes.GroupOptionNode::defaultOption).count();
        if (defaultOptions > 1) {
            throw new ConfigurationException("<radio-group> can contain at most one default option", resourcePath);
        }

        return new XmlModalNodes.RadioGroupNode(id, required, List.copyOf(options));
    }

    /**
     * Parses the options of a checkbox or radio group.
     *
     * @param element      The group element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed group options.
     * @since 1.0.0-beta.2
     */
    private static @NonNull List<XmlModalNodes.GroupOptionNode> parseGroupOptions(
            @NonNull Element element,
            @NonNull String resourcePath) {
        List<XmlModalNodes.GroupOptionNode> options = new ArrayList<>();
        for (Element child : childElements(element)) {
            String childName = nodeName(child);
            if (!"option".equals(childName)) {
                throw new ConfigurationException(String.format(
                        "Unsupported <%s> child <%s>. Only <option> is allowed",
                        nodeName(element), childName
                ), resourcePath);
            }

            options.add(new XmlModalNodes.GroupOptionNode(
                    requiredAttribute(child, "label", resourcePath),
                    requiredAttribute(child, "value", resourcePath),
                    optionalAttribute(child, "description"),
                    booleanAttribute(child, "default", false)
            ));
        }

        if (options.isEmpty()) {
            throw new ConfigurationException(String.format(
                    "<%s> must contain at least one <option>",
                    nodeName(element)
            ), resourcePath);
        }
        if (options.size() > 10) {
            throw new ConfigurationException(String.format(
                    "<%s> can contain at most 10 <option> elements",
                    nodeName(element)
            ), resourcePath);
        }
        return options;
    }

    /**
     * Parses settings shared by string and entity select components.
     *
     * @param element      The select element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed select settings.
     * @since 1.0.0-beta.2
     */
    private static @NonNull SelectSettings parseSelectSettings(@NonNull Element element, @NonNull String resourcePath) {
        String id = requiredAttribute(element, "id", resourcePath);
        String placeholder = optionalAttribute(element, "placeholder");
        boolean required = booleanAttribute(element, "required", false);
        int minValues = intAttribute(element, "min-values", required ? 1 : 0, resourcePath);
        int maxValues = intAttribute(element, "max-values", 1, resourcePath);
        boolean disabled = booleanAttribute(element, "disabled", false);

        if (minValues < 0) {
            throw new ConfigurationException(String.format("<%s> min-values cannot be negative", nodeName(element)), resourcePath);
        }
        if (maxValues <= 0) {
            throw new ConfigurationException(String.format("<%s> max-values must be greater than 0", nodeName(element)), resourcePath);
        }
        if (required && minValues == 0) {
            throw new ConfigurationException(String.format("<%s> min-values must be at least 1 when required=true", nodeName(element)), resourcePath);
        }
        if (minValues > maxValues) {
            throw new ConfigurationException(String.format("<%s> min-values cannot be greater than max-values", nodeName(element)), resourcePath);
        }

        return new SelectSettings(id, placeholder, required, minValues, maxValues, disabled);
    }

    /**
     * Contains settings shared by string and entity select components.
     *
     * @param id          The custom component ID.
     * @param placeholder The optional placeholder.
     * @param required    Whether the component is required.
     * @param minValues   The minimum number of selected values.
     * @param maxValues   The maximum number of selected values.
     * @param disabled    Whether the component is disabled.
     * @since 1.0.0-beta.2
     */
    private record SelectSettings(
            @NonNull String id,
            @NonNull String placeholder,
            boolean required,
            int minValues,
            int maxValues,
            boolean disabled
    ) {
    }
}
