package de.swiftbyte.jdaboot.interaction.component.v2;

import de.swiftbyte.jdaboot.exceptions.ConfigurationException;
import de.swiftbyte.jdaboot.interaction.component.v2.model.ComponentV2LayoutDefinition;
import de.swiftbyte.jdaboot.interaction.component.v2.model.ComponentV2Nodes;
import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import de.swiftbyte.jdaboot.xml.XmlLoaderSupport;
import lombok.CustomLog;
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

import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.childElements;
import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.nodeName;
import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.optionalAttribute;
import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.parseRefTarget;
import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.requiredAttribute;

/**
 * Loads Component V2 layouts from an XML resource and validates against the bundled XSD.
 *
 * @since 1.0.0-beta.2
 */
@CustomLog
public final class ComponentV2XmlLoader {

    private static final @NonNull String XSD_RESOURCE = "de/swiftbyte/jdaboot/schema/components-v2.xsd";
    private static final @NonNull Schema SCHEMA = XmlLoaderSupport.loadBundledSchema(
            XSD_RESOURCE,
            ComponentV2XmlLoader.class.getClassLoader()
    );

    /**
     * Prevents instantiation of this utility class.
     *
     * @since 1.0.0-beta.2
     */
    private ComponentV2XmlLoader() {
        // utility
    }

    /**
     * Loads all Component V2 layouts from an XML classpath resource.
     *
     * @param mainClass    The application class whose class loader resolves the resource.
     * @param resourcePath The classpath path of the XML resource.
     * @return The parsed layouts indexed by layout ID.
     * @since 1.0.0-beta.2
     */
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

            Map<String, ComponentV2LayoutDefinition> definitions = load(xmlStream, resourcePath);
            log.info("Loaded {} Component V2 layout(s) from {}", definitions.size(), resourcePath);
            return definitions;
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load Component V2 XML", resourcePath, e);
        }
    }

    /**
     * Loads all Component V2 layouts from an XML stream.
     * The caller remains responsible for closing the stream.
     *
     * @param xmlStream       The XML input stream.
     * @param sourceReference A description of the XML source used in error messages.
     * @return The parsed layouts indexed by layout ID.
     * @since 1.0.0-beta.2
     */
    static @NonNull Map<@NonNull String, @NonNull ComponentV2LayoutDefinition> load(
            @NonNull InputStream xmlStream,
            @NonNull String sourceReference) {
        try {
            Document document = XmlLoaderSupport.parseDocument(xmlStream, SCHEMA);
            Element root = document.getDocumentElement();
            if (!"components-v2".equals(nodeName(root))) {
                throw new ConfigurationException(String.format(
                        "Invalid root element '%s'. Expected 'components-v2'",
                        nodeName(root)), sourceReference);
            }

            HashMap<String, ComponentV2LayoutDefinition> definitions = new HashMap<>();
            for (Element layoutElement : childElements(root)) {
                if (!"layout".equals(nodeName(layoutElement))) {
                    throw new ConfigurationException(String.format(
                            "Unsupported top-level element <%s>. Only <layout> is allowed",
                            nodeName(layoutElement)), sourceReference);
                }

                ComponentV2LayoutDefinition definition = parseLayout(layoutElement, sourceReference);
                if (definitions.containsKey(definition.id())) {
                    throw new ConfigurationException(String.format(
                            "Duplicate Component V2 layout id '%s'",
                            definition.id()), sourceReference);
                }
                definitions.put(definition.id(), definition);
            }

            return Collections.unmodifiableMap(definitions);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load Component V2 XML", sourceReference, e);
        }
    }

    /**
     * Parses a Component V2 layout element.
     *
     * @param layoutElement The layout element.
     * @param resourcePath  The XML resource path used for error reporting.
     * @return The parsed layout definition.
     * @since 1.0.0-beta.2
     */
    private static @NonNull ComponentV2LayoutDefinition parseLayout(@NonNull Element layoutElement,
                                                                    @NonNull String resourcePath) {
        String id = requiredAttribute(layoutElement, "id", resourcePath);

        List<XmlDefaultVariable> defaultVars = new ArrayList<>();
        List<ComponentV2Nodes.MessageTopLevelNode> components = new ArrayList<>();
        boolean hasDefaultVariablesBlock = false;
        boolean hasComponentNodes = false;

        for (Element child : childElements(layoutElement)) {
            String nodeName = nodeName(child);

            if ("default-variables".equals(nodeName)) {
                if (hasDefaultVariablesBlock) {
                    throw new ConfigurationException("<layout> can contain <default-variables> only once", resourcePath);
                }
                if (hasComponentNodes) {
                    throw new ConfigurationException("<default-variables> must be defined before component nodes", resourcePath);
                }

                defaultVars.addAll(parseDefaultVariables(child, resourcePath));
                hasDefaultVariablesBlock = true;
                continue;
            }

            hasComponentNodes = true;
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

    /**
     * Parses Component V2 default variables.
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
     * Parses a message top-level component.
     *
     * @param element      The component element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed message node.
     * @since 1.0.0-beta.2
     */
    private static ComponentV2Nodes.MessageTopLevelNode parseMessageNode(@NonNull Element element,
                                                                         @NonNull String resourcePath) {
        String nodeName = nodeName(element);
        return switch (nodeName) {
            case "text-display" -> new ComponentV2Nodes.TextDisplayNode(
                    requiredAttribute(element, "content", resourcePath)
            );
            case "separator" -> new ComponentV2Nodes.SeparatorNode(
                    optionalAttribute(element, "divider", "true"),
                    optionalAttribute(element, "spacing", "SMALL")
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

    /**
     * Parses an action row component.
     *
     * @param element      The action row element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed action row node.
     * @since 1.0.0-beta.2
     */
    private static ComponentV2Nodes.ActionRowNode parseActionRowNode(@NonNull Element element,
                                                                     @NonNull String resourcePath) {
        List<ComponentV2Nodes.ActionRowChildNode> children = new ArrayList<>();
        int buttonCount = 0;
        int stringSelectCount = 0;
        int entitySelectCount = 0;

        for (Element child : childElements(element)) {
            String childName = nodeName(child);
            switch (childName) {
                case "button-ref", "link-button" -> {
                    children.add(parseButtonNode(child, resourcePath));
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
                        "Unsupported <action-row> child <%s>. Allowed: button-ref, link-button, string-select-ref, entity-select-ref",
                        childName), resourcePath);
            }
        }

        if (children.isEmpty()) {
            throw new ConfigurationException("<action-row> must contain at least one child component", resourcePath);
        }

        if (buttonCount > 0) {
            if (stringSelectCount > 0 || entitySelectCount > 0) {
                throw new ConfigurationException("<action-row> cannot mix buttons with select refs", resourcePath);
            }
            if (buttonCount > 5) {
                throw new ConfigurationException("<action-row> can contain at most 5 buttons", resourcePath);
            }
        }

        boolean hasExactlyOneStringSelect = buttonCount == 0 && stringSelectCount == 1 && entitySelectCount == 0 && children.size() == 1;
        boolean hasExactlyOneEntitySelect = buttonCount == 0 && entitySelectCount == 1 && stringSelectCount == 0 && children.size() == 1;
        boolean hasValidButtons = buttonCount >= 1 && buttonCount <= 5 && stringSelectCount == 0 && entitySelectCount == 0;

        if (!hasValidButtons && !hasExactlyOneStringSelect && !hasExactlyOneEntitySelect) {
            throw new ConfigurationException("<action-row> must be either 1-5 buttons OR exactly one string-select-ref OR exactly one entity-select-ref", resourcePath);
        }

        return new ComponentV2Nodes.ActionRowNode(List.copyOf(children));
    }

    /**
     * Parses a supported button node.
     *
     * @param element      The button element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed button node.
     * @since 1.0.0-beta.2
     */
    private static ComponentV2Nodes.ButtonNode parseButtonNode(@NonNull Element element,
                                                               @NonNull String resourcePath) {
        return switch (nodeName(element)) {
            case "button-ref" -> parseButtonRefNode(element, resourcePath);
            case "link-button" -> parseLinkButtonNode(element, resourcePath);
            default -> throw new ConfigurationException(
                    "Unsupported button element <" + nodeName(element) + ">",
                    resourcePath
            );
        };
    }

    /**
     * Parses a button reference.
     *
     * @param element      The button reference element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed button reference.
     * @since 1.0.0-beta.2
     */
    private static ComponentV2Nodes.ButtonRefNode parseButtonRefNode(@NonNull Element element, @NonNull String resourcePath) {
        XmlLoaderSupport.XmlRefTarget target = parseRefTarget(element, resourcePath);
        return new ComponentV2Nodes.ButtonRefNode(target.id(), target.className());
    }

    /**
     * Parses a link button.
     *
     * @param element      The link button element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed link button.
     * @since 1.0.0-beta.2
     */
    private static ComponentV2Nodes.LinkButtonNode parseLinkButtonNode(@NonNull Element element,
                                                                       @NonNull String resourcePath) {
        return new ComponentV2Nodes.LinkButtonNode(
                requiredAttribute(element, "url", resourcePath),
                requiredAttribute(element, "label", resourcePath),
                optionalAttribute(element, "emoji"),
                optionalAttribute(element, "disabled", "false")
        );
    }

    /**
     * Parses a string select reference.
     *
     * @param element      The string select reference element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed string select reference.
     * @since 1.0.0-beta.2
     */
    private static ComponentV2Nodes.StringSelectRefNode parseStringSelectRefNode(@NonNull Element element,
                                                                                 @NonNull String resourcePath) {
        XmlLoaderSupport.XmlRefTarget target = parseRefTarget(element, resourcePath);
        return new ComponentV2Nodes.StringSelectRefNode(target.id(), target.className());
    }

    /**
     * Parses an entity select reference.
     *
     * @param element      The entity select reference element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed entity select reference.
     * @since 1.0.0-beta.2
     */
    private static ComponentV2Nodes.EntitySelectRefNode parseEntitySelectRefNode(@NonNull Element element,
                                                                                 @NonNull String resourcePath) {
        XmlLoaderSupport.XmlRefTarget target = parseRefTarget(element, resourcePath);
        return new ComponentV2Nodes.EntitySelectRefNode(target.id(), target.className());
    }

    /**
     * Parses a container component.
     *
     * @param element      The container element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed container node.
     * @since 1.0.0-beta.2
     */
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
                optionalAttribute(element, "accent-color"),
                optionalAttribute(element, "spoiler", "false"),
                optionalAttribute(element, "disabled", "false")
        );
    }

    /**
     * Parses a container child component.
     *
     * @param element      The child element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed container child node.
     * @since 1.0.0-beta.2
     */
    private static ComponentV2Nodes.ContainerChildNode parseContainerChildNode(@NonNull Element element,
                                                                               @NonNull String resourcePath) {
        String nodeName = nodeName(element);
        return switch (nodeName) {
            case "text-display" -> new ComponentV2Nodes.TextDisplayNode(
                    requiredAttribute(element, "content", resourcePath)
            );
            case "separator" -> new ComponentV2Nodes.SeparatorNode(
                    optionalAttribute(element, "divider", "true"),
                    optionalAttribute(element, "spacing", "SMALL")
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

    /**
     * Parses a file display component.
     *
     * @param element      The file display element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed file display node.
     * @since 1.0.0-beta.2
     */
    private static ComponentV2Nodes.FileDisplayNode parseFileDisplayNode(@NonNull Element element,
                                                                         @NonNull String resourcePath) {
        return new ComponentV2Nodes.FileDisplayNode(
                requiredAttribute(element, "file-name", resourcePath),
                optionalAttribute(element, "spoiler", "false")
        );
    }

    /**
     * Parses a media gallery component.
     *
     * @param element      The media gallery element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed media gallery node.
     * @since 1.0.0-beta.2
     */
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
                    optionalAttribute(child, "spoiler", "false")
            ));
        }

        if (items.isEmpty()) {
            throw new ConfigurationException(String.format(
                    "<%s> must contain at least one <item>",
                    nodeName(element)), resourcePath);
        }

        return new ComponentV2Nodes.MediaGalleryNode(List.copyOf(items));
    }

    /**
     * Parses a section component.
     *
     * @param element      The section element.
     * @param resourcePath The XML resource path used for error reporting.
     * @return The parsed section node.
     * @since 1.0.0-beta.2
     */
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
                optionalAttribute(element, "disabled", "false")
        );
    }

    /**
     * Parses the content components of a section.
     *
     * @param contentElement The section content element.
     * @param resourcePath   The XML resource path used for error reporting.
     * @return The parsed section content nodes.
     * @since 1.0.0-beta.2
     */
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

    /**
     * Parses the accessory component of a section.
     *
     * @param accessoryElement The section accessory element.
     * @param resourcePath     The XML resource path used for error reporting.
     * @return The parsed section accessory node.
     * @since 1.0.0-beta.2
     */
    private static ComponentV2Nodes.SectionAccessoryNode parseSectionAccessory(@NonNull Element accessoryElement,
                                                                               @NonNull String resourcePath) {
        List<Element> children = childElements(accessoryElement);
        if (children.size() != 1) {
            throw new ConfigurationException("<accessory> must contain exactly one child element", resourcePath);
        }

        Element child = children.get(0);
        String nodeName = nodeName(child);
        return switch (nodeName) {
            case "button-ref", "link-button" -> parseButtonNode(child, resourcePath);
            case "thumbnail" -> new ComponentV2Nodes.ThumbnailNode(
                    requiredAttribute(child, "url", resourcePath),
                    optionalAttribute(child, "description"),
                    optionalAttribute(child, "spoiler", "false")
            );
            default -> throw new ConfigurationException(String.format(
                    "Unsupported <accessory> child <%s>. Allowed: button-ref, link-button, thumbnail",
                    nodeName), resourcePath);
        };
    }

}
