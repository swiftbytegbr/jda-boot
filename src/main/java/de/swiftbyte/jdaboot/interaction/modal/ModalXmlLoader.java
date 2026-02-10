package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.exceptions.ConfigurationException;
import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import de.swiftbyte.jdaboot.interaction.modal.model.XmlModalLayoutDefinition;
import de.swiftbyte.jdaboot.interaction.modal.model.XmlModalNodes;
import lombok.CustomLog;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.jspecify.annotations.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads modal layouts from an XML resource and validates against the bundled XSD.
 *
 * @since 1.0.0-beta.2
 */
@CustomLog
public final class ModalXmlLoader {

    private static final @NonNull String XSD_RESOURCE = "de/swiftbyte/jdaboot/schema/modals.xsd";

    private ModalXmlLoader() {
        // utility
    }

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

            Document document = parseDocument(xmlStream);
            Element root = document.getDocumentElement();
            if (!"modals".equals(nodeName(root))) {
                throw new ConfigurationException(String.format(
                        "Invalid root element '%s'. Expected 'modals'",
                        nodeName(root)), resourcePath);
            }

            HashMap<String, XmlModalLayoutDefinition> definitions = new HashMap<>();
            for (Element layoutElement : childElements(root)) {
                if (!"layout".equals(nodeName(layoutElement))) {
                    throw new ConfigurationException(String.format(
                            "Unsupported top-level element <%s>. Only <layout> is allowed",
                            nodeName(layoutElement)), resourcePath);
                }

                XmlModalLayoutDefinition definition = parseLayout(layoutElement, resourcePath);
                if (definitions.containsKey(definition.id())) {
                    throw new ConfigurationException(String.format(
                            "Duplicate modal layout id '%s'",
                            definition.id()), resourcePath);
                }
                definitions.put(definition.id(), definition);
            }

            log.info("Loaded {} modal layout(s) from {}", definitions.size(), resourcePath);
            return Collections.unmodifiableMap(definitions);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load modal XML", resourcePath, e);
        }
    }

    private static @NonNull Document parseDocument(@NonNull InputStream xmlStream)
            throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setSchema(loadSchema());

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlStream);
    }

    private static @NonNull Schema loadSchema() {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try (InputStream xsdStream = ModalXmlLoader.class.getClassLoader().getResourceAsStream(XSD_RESOURCE)) {
            if (xsdStream == null) {
                throw new ConfigurationException("Missing bundled modal XSD", XSD_RESOURCE);
            }
            return schemaFactory.newSchema(new javax.xml.transform.stream.StreamSource(xsdStream));
        } catch (SAXException | IOException e) {
            throw new ConfigurationException("Failed to load modal XSD", XSD_RESOURCE, e);
        }
    }

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

    private static @NonNull List<@NonNull XmlDefaultVariable> parseDefaultVariables(@NonNull Element element,
                                                                                    @NonNull String resourcePath) {
        List<XmlDefaultVariable> defaultVars = new ArrayList<>();
        for (Element child : childElements(element)) {
            String nodeName = nodeName(child);
            if (!"variable".equals(nodeName)) {
                throw new ConfigurationException(String.format(
                        "Unsupported <%s> child <%s>. Only <variable> is allowed",
                        nodeName(element), nodeName), resourcePath);
            }

            String key = requiredAttribute(child, "key", resourcePath);
            String value = requiredAttribute(child, "value", resourcePath);
            defaultVars.add(new XmlDefaultVariable(key, value));
        }

        if (defaultVars.isEmpty()) {
            throw new ConfigurationException("<default-variables> must contain at least one <variable>", resourcePath);
        }

        return defaultVars;
    }

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
            default -> throw new ConfigurationException(String.format(
                    "Unsupported <label> child <%s>. Allowed: string-input, file-input, string-select, entity-select",
                    nodeName
            ), resourcePath);
        };

        return new XmlModalNodes.LabelNode(text, description, childNode);
    }

    private static XmlModalNodes.StringInputNode parseStringInputNode(@NonNull Element element,
                                                                      @NonNull String resourcePath) {
        String id = requiredAttribute(element, "id", resourcePath);
        String styleValue = optionalAttribute(element, "style");
        String placeholder = optionalAttribute(element, "placeholder");
        boolean required = booleanAttribute(element, "required");
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

    private static XmlModalNodes.FileInputNode parseFileInputNode(@NonNull Element element,
                                                                  @NonNull String resourcePath) {
        String id = requiredAttribute(element, "id", resourcePath);
        boolean required = booleanAttribute(element, "required");
        int minValues = intAttribute(element, "min-values", required ? 1 : 0, resourcePath);
        int maxValues = intAttribute(element, "max-values", 1, resourcePath);

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

        return new XmlModalNodes.FileInputNode(id, required, maxValues, minValues);
    }

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
                    booleanAttribute(child, "default")
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

    private static @NonNull SelectSettings parseSelectSettings(@NonNull Element element, @NonNull String resourcePath) {
        String id = requiredAttribute(element, "id", resourcePath);
        String placeholder = optionalAttribute(element, "placeholder");
        boolean required = booleanAttribute(element, "required");
        int minValues = intAttribute(element, "min-values", required ? 1 : 0, resourcePath);
        int maxValues = intAttribute(element, "max-values", 1, resourcePath);
        boolean disabled = booleanAttribute(element, "disabled");

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

    private static int intAttribute(@NonNull Element element, @NonNull String attributeName, int fallback,
                                    @NonNull String resourcePath) {
        String value = optionalAttribute(element, attributeName);
        if (value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ConfigurationException(String.format(
                    "Invalid integer value '%s' for attribute '%s' on <%s>",
                    value, attributeName, nodeName(element)
            ), resourcePath, e);
        }
    }

    private static boolean booleanAttribute(@NonNull Element element, @NonNull String attributeName) {
        String value = optionalAttribute(element, attributeName);
        if (value.isBlank()) {
            return false;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static @NonNull String requiredAttribute(@NonNull Element element, @NonNull String attributeName,
                                                     @NonNull String resourcePath) {
        String value = element.getAttribute(attributeName);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(String.format(
                    "Missing required attribute '%s' on <%s>",
                    attributeName, nodeName(element)), resourcePath);
        }
        return value;
    }

    private static @NonNull String optionalAttribute(@NonNull Element element, @NonNull String attributeName) {
        String value = element.getAttribute(attributeName);
        return value == null || value.isBlank() ? "" : value;
    }

    private static @NonNull List<@NonNull Element> childElements(@NonNull Element parent) {
        List<Element> elements = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element element) {
                elements.add(element);
            }
        }
        return elements;
    }

    private static @NonNull String nodeName(@NonNull Element element) {
        return element.getLocalName() != null ? element.getLocalName() : element.getTagName();
    }

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
