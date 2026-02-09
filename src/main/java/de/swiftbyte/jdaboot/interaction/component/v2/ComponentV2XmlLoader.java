package de.swiftbyte.jdaboot.interaction.component.v2;

import de.swiftbyte.jdaboot.exceptions.ConfigurationException;
import de.swiftbyte.jdaboot.interaction.component.v2.model.ComponentV2LayoutDefinition;
import de.swiftbyte.jdaboot.interaction.component.v2.model.ComponentV2Nodes;
import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import lombok.CustomLog;
import net.dv8tion.jda.api.components.separator.Separator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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

/**
 * Loads Component V2 layouts from an XML resource and validates against the bundled XSD.
 *
 * @since 1.0.0-beta.2
 */
@CustomLog
public final class ComponentV2XmlLoader {

    private static final @NonNull String XSD_RESOURCE = "de/swiftbyte/jdaboot/schema/components-v2.xsd";

    private ComponentV2XmlLoader() {
        // utility
    }

    public static @NonNull Map<@NonNull String, @NonNull ComponentV2LayoutDefinition> load(@NonNull Class<?> mainClass,
                                                                                             @NonNull String resourcePath) {
        if (resourcePath.isBlank()) {
            return Map.of();
        }

        try (InputStream xmlStream = mainClass.getClassLoader().getResourceAsStream(resourcePath)) {
            if (xmlStream == null) {
                log.debug("No Component V2 XML found at '{}'. Skipping XML layout load.", resourcePath);
                return Map.of();
            }

            Document document = parseDocument(xmlStream);
            Element root = document.getDocumentElement();
            if (!"components-v2".equals(nodeName(root))) {
                throw new ConfigurationException(String.format(
                        "Invalid root element '%s's. Expected 'components-v2'",
                        nodeName(root)), resourcePath);
            }

            HashMap<String, ComponentV2LayoutDefinition> definitions = new HashMap<>();
            for (Element layoutElement : childElements(root)) {
                if (!"layout".equals(nodeName(layoutElement))) {
                    throw new ConfigurationException(String.format(
                            "Unsupported top-level element <%s>. Only <layout> is allowed",
                            nodeName(layoutElement)), resourcePath);
                }

                ComponentV2LayoutDefinition definition = parseLayout(layoutElement, resourcePath);
                if (definitions.containsKey(definition.id())) {
                    throw new ConfigurationException(String.format(
                            "Duplicate Component V2 layout id '%s'",
                            definition.id()), resourcePath);
                }
                definitions.put(definition.id(), definition);
            }

            log.info("Loaded {} Component V2 layout(s) from {}", definitions.size(), resourcePath);
            return Collections.unmodifiableMap(definitions);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load Component V2 XML", resourcePath, e);
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
        try (InputStream xsdStream = ComponentV2XmlLoader.class.getClassLoader().getResourceAsStream(XSD_RESOURCE)) {
            if (xsdStream == null) {
                throw new ConfigurationException("Missing bundled Component V2 XSD", XSD_RESOURCE);
            }
            return schemaFactory.newSchema(new javax.xml.transform.stream.StreamSource(xsdStream));
        } catch (SAXException | IOException e) {
            throw new ConfigurationException("Failed to load Component V2 XSD", XSD_RESOURCE, e);
        }
    }

    private static @NonNull ComponentV2LayoutDefinition parseLayout(@NonNull Element layoutElement,
                                                                     @NonNull String resourcePath) {
        String id = requiredAttribute(layoutElement, "id", resourcePath);

        List<XmlDefaultVariable> defaultVars = new ArrayList<>();
        List<ComponentV2Nodes.MessageTopLevelNode> components = new ArrayList<>();

        for (Element child : childElements(layoutElement)) {
            String nodeName = nodeName(child);

            if ("default-var".equals(nodeName)) {
                defaultVars.add(parseDefaultVar(child, resourcePath));
                continue;
            }

            components.add(parseMessageNode(child, resourcePath));
        }

        if (components.isEmpty()) {
            throw new ConfigurationException(String.format(
                    "Layout '%s' must contain at least one component",
                    id), resourcePath);
        }

        return new ComponentV2LayoutDefinition(
                id,
                resourcePath,
                defaultVars.toArray(new XmlDefaultVariable[0]),
                List.copyOf(components)
        );
    }

    private static @NonNull XmlDefaultVariable parseDefaultVar(@NonNull Element element, @NonNull String resourcePath) {
        String variable = requiredAttribute(element, "variable", resourcePath);
        String value = requiredAttribute(element, "value", resourcePath);
        return new XmlDefaultVariable(variable, value);
    }

    private static ComponentV2Nodes.MessageTopLevelNode parseMessageNode(@NonNull Element element,
                                                                          @NonNull String resourcePath) {
        String nodeName = nodeName(element);
        return switch (nodeName) {
            case "text-display" -> new ComponentV2Nodes.TextDisplayNode(
                    requiredAttribute(element, "content", resourcePath)
            );
            case "separator" -> new ComponentV2Nodes.SeparatorNode(
                    booleanAttribute(element, "divider", true),
                    spacingAttribute(element, resourcePath)
            );
            case "file-display" -> parseFileDisplayNode(element, resourcePath);
            case "media-gallery" -> parseMediaGalleryNode(element, resourcePath);
            case "action-row" -> parseActionRowNode(element, resourcePath);
            case "container" -> parseContainerNode(element, resourcePath);
            case "section" -> parseSectionNode(element, resourcePath);
            default -> throw new ConfigurationException(String.format(
                    "Unsupported Component V2 element <%s>",
                    nodeName), resourcePath);
        };
    }

    private static ComponentV2Nodes.ActionRowNode parseActionRowNode(@NonNull Element element,
                                                                      @NonNull String resourcePath) {
        List<ComponentV2Nodes.ActionRowChildNode> children = new ArrayList<>();
        int buttonCount = 0;
        int stringSelectCount = 0;
        int entitySelectCount = 0;

        for (Element child : childElements(element)) {
            String childName = nodeName(child);
            switch (childName) {
                case "button-ref" -> {
                    children.add(parseButtonRefNode(child, resourcePath));
                    buttonCount++;
                }
                case "string-select-ref" -> {
                    children.add(parseStringSelectRefNode(child, resourcePath));
                    stringSelectCount++;
                }
                case "entity-select-ref" -> {
                    children.add(parseEntitySelectRefNode(child, resourcePath));
                    entitySelectCount++;
                }
                default -> throw new ConfigurationException(String.format(
                        "Unsupported <action-row> child <%s>. Allowed: button-ref, string-select-ref, entity-select-ref",
                        childName), resourcePath);
            }
        }

        if (children.isEmpty()) {
            throw new ConfigurationException("<action-row> must contain at least one child component", resourcePath);
        }

        if (buttonCount > 0) {
            if (stringSelectCount > 0 || entitySelectCount > 0) {
                throw new ConfigurationException("<action-row> cannot mix button-ref with select refs", resourcePath);
            }
            if (buttonCount > 5) {
                throw new ConfigurationException("<action-row> can contain at most 5 button-ref elements", resourcePath);
            }
        }

        boolean hasExactlyOneStringSelect = buttonCount == 0 && stringSelectCount == 1 && entitySelectCount == 0 && children.size() == 1;
        boolean hasExactlyOneEntitySelect = buttonCount == 0 && entitySelectCount == 1 && stringSelectCount == 0 && children.size() == 1;
        boolean hasValidButtons = buttonCount >= 1 && buttonCount <= 5 && stringSelectCount == 0 && entitySelectCount == 0;

        if (!hasValidButtons && !hasExactlyOneStringSelect && !hasExactlyOneEntitySelect) {
            throw new ConfigurationException("<action-row> must be either 1-5 button-ref OR exactly one string-select-ref OR exactly one entity-select-ref", resourcePath);
        }

        return new ComponentV2Nodes.ActionRowNode(List.copyOf(children));
    }

    private static ComponentV2Nodes.ButtonRefNode parseButtonRefNode(@NonNull Element element, @NonNull String resourcePath) {
        RefTarget target = parseRefTarget(element, resourcePath);
        return new ComponentV2Nodes.ButtonRefNode(target.id, target.className);
    }

    private static ComponentV2Nodes.StringSelectRefNode parseStringSelectRefNode(@NonNull Element element,
                                                                                  @NonNull String resourcePath) {
        RefTarget target = parseRefTarget(element, resourcePath);
        return new ComponentV2Nodes.StringSelectRefNode(target.id, target.className);
    }

    private static ComponentV2Nodes.EntitySelectRefNode parseEntitySelectRefNode(@NonNull Element element,
                                                                                  @NonNull String resourcePath) {
        RefTarget target = parseRefTarget(element, resourcePath);
        return new ComponentV2Nodes.EntitySelectRefNode(target.id, target.className);
    }

    private static ComponentV2Nodes.ContainerNode parseContainerNode(@NonNull Element element,
                                                                      @NonNull String resourcePath) {
        List<ComponentV2Nodes.ContainerChildNode> children = new ArrayList<>();
        for (Element child : childElements(element)) {
            children.add(parseContainerChildNode(child, resourcePath));
        }

        if (children.isEmpty()) {
            throw new ConfigurationException("<container> must contain at least one child component", resourcePath);
        }

        return new ComponentV2Nodes.ContainerNode(
                List.copyOf(children),
                nullableColorAttribute(element, resourcePath),
                booleanAttribute(element, "spoiler", false),
                booleanAttribute(element, "disabled", false)
        );
    }

    private static ComponentV2Nodes.ContainerChildNode parseContainerChildNode(@NonNull Element element,
                                                                                @NonNull String resourcePath) {
        String nodeName = nodeName(element);
        return switch (nodeName) {
            case "text-display" -> new ComponentV2Nodes.TextDisplayNode(
                    requiredAttribute(element, "content", resourcePath)
            );
            case "separator" -> new ComponentV2Nodes.SeparatorNode(
                    booleanAttribute(element, "divider", true),
                    spacingAttribute(element, resourcePath)
            );
            case "file-display" -> parseFileDisplayNode(element, resourcePath);
            case "media-gallery" -> parseMediaGalleryNode(element, resourcePath);
            case "action-row" -> parseActionRowNode(element, resourcePath);
            case "section" -> parseSectionNode(element, resourcePath);
            default -> throw new ConfigurationException(String.format(
                    "Unsupported <container> child <%s>. Allowed: text-display, separator, file-display, media-gallery, action-row, section",
                    nodeName), resourcePath);
        };
    }

    private static ComponentV2Nodes.FileDisplayNode parseFileDisplayNode(@NonNull Element element,
                                                                          @NonNull String resourcePath) {
        return new ComponentV2Nodes.FileDisplayNode(
                requiredAttribute(element, "file-name", resourcePath),
                booleanAttribute(element, "spoiler", false)
        );
    }

    private static ComponentV2Nodes.MediaGalleryNode parseMediaGalleryNode(@NonNull Element element,
                                                                            @NonNull String resourcePath) {
        List<ComponentV2Nodes.MediaGalleryItemNode> items = new ArrayList<>();

        for (Element child : childElements(element)) {
            String nodeName = nodeName(child);
            if (!"item".equals(nodeName)) {
                throw new ConfigurationException(String.format(
                        "Unsupported <%s> child <%s>. Only <item> is allowed",
                        nodeName(element), nodeName), resourcePath);
            }

            items.add(new ComponentV2Nodes.MediaGalleryItemNode(
                    requiredAttribute(child, "url", resourcePath),
                    optionalAttribute(child, "description"),
                    booleanAttribute(child, "spoiler", false)
            ));
        }

        if (items.isEmpty()) {
            throw new ConfigurationException(String.format(
                    "<%s> must contain at least one <item>",
                    nodeName(element)), resourcePath);
        }

        return new ComponentV2Nodes.MediaGalleryNode(List.copyOf(items));
    }

    private static ComponentV2Nodes.SectionNode parseSectionNode(@NonNull Element element,
                                                                  @NonNull String resourcePath) {
        Element contentElement = null;
        Element accessoryElement = null;

        for (Element child : childElements(element)) {
            String nodeName = nodeName(child);
            if ("content".equals(nodeName)) {
                if (contentElement != null) {
                    throw new ConfigurationException("<section> must have exactly one <content> element", resourcePath);
                }
                contentElement = child;
            } else if ("accessory".equals(nodeName)) {
                if (accessoryElement != null) {
                    throw new ConfigurationException("<section> must have exactly one <accessory> element", resourcePath);
                }
                accessoryElement = child;
            } else {
                throw new ConfigurationException(String.format(
                        "Unsupported <section> child <%s>. Allowed: content, accessory",
                        nodeName), resourcePath);
            }
        }

        if (contentElement == null || accessoryElement == null) {
            throw new ConfigurationException("<section> must contain both <content> and <accessory>", resourcePath);
        }

        List<ComponentV2Nodes.SectionContentNode> contentNodes = parseSectionContent(contentElement, resourcePath);
        ComponentV2Nodes.SectionAccessoryNode accessoryNode = parseSectionAccessory(accessoryElement, resourcePath);

        return new ComponentV2Nodes.SectionNode(
                List.copyOf(contentNodes),
                accessoryNode,
                booleanAttribute(element, "disabled", false)
        );
    }

    private static @NonNull List<ComponentV2Nodes.SectionContentNode> parseSectionContent(@NonNull Element contentElement,
                                                                                           @NonNull String resourcePath) {
        List<ComponentV2Nodes.SectionContentNode> content = new ArrayList<>();
        for (Element child : childElements(contentElement)) {
            String nodeName = nodeName(child);
            if (!"text-display".equals(nodeName)) {
                throw new ConfigurationException(String.format(
                        "Unsupported <content> child <%s>. Only <text-display> is allowed",
                        nodeName), resourcePath);
            }
            content.add(new ComponentV2Nodes.TextDisplayNode(requiredAttribute(child, "content", resourcePath)));
        }

        if (content.isEmpty()) {
            throw new ConfigurationException("<content> must contain at least one <text-display>", resourcePath);
        }
        return content;
    }

    private static ComponentV2Nodes.SectionAccessoryNode parseSectionAccessory(@NonNull Element accessoryElement,
                                                                                @NonNull String resourcePath) {
        List<Element> children = childElements(accessoryElement);
        if (children.size() != 1) {
            throw new ConfigurationException("<accessory> must contain exactly one child element", resourcePath);
        }

        Element child = children.get(0);
        String nodeName = nodeName(child);
        return switch (nodeName) {
            case "button-ref" -> parseButtonRefNode(child, resourcePath);
            case "thumbnail" -> new ComponentV2Nodes.ThumbnailNode(
                    requiredAttribute(child, "url", resourcePath),
                    optionalAttribute(child, "description"),
                    booleanAttribute(child, "spoiler", false)
            );
            default -> throw new ConfigurationException(String.format(
                    "Unsupported <accessory> child <%s>. Allowed: button-ref, thumbnail",
                    nodeName), resourcePath);
        };
    }

    private static @Nullable Integer nullableColorAttribute(@NonNull Element element, @NonNull String resourcePath) {
        String value = optionalAttribute(element, "accent-color").trim();
        if (value.isEmpty()) {
            return null;
        }

        String normalized = value;
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("0x") || normalized.startsWith("0X")) {
            normalized = normalized.substring(2);
        }

        if (normalized.matches("[0-9a-fA-F]{6}")) {
            return Integer.parseInt(normalized, 16);
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(String.format(
                    "Invalid accent-color '%s' on <%s>",
                    value, nodeName(element)
            ), resourcePath, e);
        }
    }

    private static Separator.Spacing spacingAttribute(@NonNull Element element, @NonNull String resourcePath) {
        String value = optionalAttribute(element, "spacing");
        if (value.isBlank()) {
            return Separator.Spacing.SMALL;
        }
        try {
            return Separator.Spacing.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(String.format(
                    "Invalid spacing '%s' on <%s>",
                    value, nodeName(element)
            ), resourcePath, e);
        }
    }

    private static boolean booleanAttribute(@NonNull Element element, @NonNull String attributeName, boolean fallback) {
        String value = optionalAttribute(element, attributeName);
        if (value.isBlank()) {
            return fallback;
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

    private static @NonNull RefTarget parseRefTarget(@NonNull Element element, @NonNull String resourcePath) {
        String id = optionalAttribute(element, "id");
        String className = optionalAttribute(element, "class");

        boolean hasId = !id.isBlank();
        boolean hasClass = !className.isBlank();

        if (hasId == hasClass) {
            throw new ConfigurationException(String.format(
                    "<%s> must define exactly one of attributes 'id' or 'class'",
                    nodeName(element)), resourcePath);
        }

        return new RefTarget(hasId ? id : null, hasClass ? className : null);
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

    private record RefTarget(@Nullable String id, @Nullable String className) {
    }
}
