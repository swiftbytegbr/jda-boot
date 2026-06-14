package de.swiftbyte.jdaboot.xml;

import de.swiftbyte.jdaboot.exceptions.ConfigurationException;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared XML parsing helpers for interaction XML loaders.
 *
 * @since 1.0.0-beta.2
 */
public final class XmlLoaderSupport {

    private XmlLoaderSupport() {
        // utility
    }

    /**
     * Normalizes a classpath resource path relative to the given base directory.
     * Classpath paths always use forward slashes and do not start with a slash.
     *
     * @param path          The configured resource path.
     * @param baseDirectory The base directory for relative paths.
     * @return The normalized classpath resource path.
     * @since 1.0.0-beta.2
     */
    public static @NonNull String normalizeResourcePath(@NonNull String path, @NonNull String baseDirectory) {
        String normalizedPath = path.trim().replace('\\', '/');
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        String normalizedBaseDirectory = baseDirectory.trim().replace('\\', '/');
        while (normalizedBaseDirectory.startsWith("/")) {
            normalizedBaseDirectory = normalizedBaseDirectory.substring(1);
        }
        if (!normalizedBaseDirectory.endsWith("/")) {
            normalizedBaseDirectory += "/";
        }

        if (!normalizedPath.startsWith(normalizedBaseDirectory)) {
            normalizedPath = normalizedBaseDirectory + normalizedPath;
        }
        return normalizedPath;
    }

    public static @NonNull Schema loadBundledSchema(@NonNull String schemaResource, @NonNull ClassLoader classLoader) {
        URL schemaUrl = classLoader.getResource(schemaResource);
        if (schemaUrl == null) {
            throw new ConfigurationException("Missing bundled XSD", schemaResource);
        }

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            return schemaFactory.newSchema(schemaUrl);
        } catch (SAXException e) {
            throw new ConfigurationException("Failed to load XSD", schemaResource, e);
        }
    }

    public static @NonNull Document parseDocument(@NonNull InputStream xmlStream,
                                                  @NonNull Schema schema)
            throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setSchema(schema);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlStream);
    }

    public static @NonNull List<@NonNull Element> childElements(@NonNull Element parent) {
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

    public static @NonNull String nodeName(@NonNull Element element) {
        return element.getLocalName() != null ? element.getLocalName() : element.getTagName();
    }

    public static @NonNull String requiredAttribute(@NonNull Element element,
                                                    @NonNull String attributeName,
                                                    @NonNull String resourcePath) {
        String value = element.getAttribute(attributeName);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(String.format(
                    "Missing required attribute '%s' on <%s>",
                    attributeName, nodeName(element)), resourcePath);
        }
        return value;
    }

    public static @NonNull String optionalAttribute(@NonNull Element element,
                                                    @NonNull String attributeName) {
        String value = element.getAttribute(attributeName);
        return value == null || value.isBlank() ? "" : value;
    }

    public static boolean booleanAttribute(@NonNull Element element,
                                           @NonNull String attributeName,
                                           boolean fallback) {
        String value = optionalAttribute(element, attributeName);
        if (value.isBlank()) {
            return fallback;
        }
        return Boolean.parseBoolean(value.trim());
    }

    public static int intAttribute(@NonNull Element element,
                                   @NonNull String attributeName,
                                   int fallback,
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

    public static @NonNull List<@NonNull XmlVariableDefinition> parseDefaultVariables(@NonNull Element element,
                                                                                      @NonNull String resourcePath) {
        List<XmlVariableDefinition> variables = new ArrayList<>();
        for (Element child : childElements(element)) {
            String childName = nodeName(child);
            if (!"variable".equals(childName)) {
                throw new ConfigurationException(String.format(
                        "Unsupported <%s> child <%s>. Only <variable> is allowed",
                        nodeName(element), childName), resourcePath);
            }

            String key = requiredAttribute(child, "key", resourcePath);
            String value = requiredAttribute(child, "value", resourcePath);
            variables.add(new XmlVariableDefinition(key, value));
        }

        if (variables.isEmpty()) {
            throw new ConfigurationException("<default-variables> must contain at least one <variable>", resourcePath);
        }

        return variables;
    }

    public static @NonNull XmlRefTarget parseRefTarget(@NonNull Element element,
                                                       @NonNull String resourcePath) {
        String id = optionalAttribute(element, "id");
        String className = optionalAttribute(element, "class");

        boolean hasId = !id.isBlank();
        boolean hasClass = !className.isBlank();
        if (hasId == hasClass) {
            throw new ConfigurationException(String.format(
                    "<%s> must define exactly one of attributes 'id' or 'class'",
                    nodeName(element)), resourcePath);
        }

        return new XmlRefTarget(hasId ? id : null, hasClass ? className : null);
    }

    public record XmlVariableDefinition(@NonNull String key, @NonNull String value) {
    }

    public record XmlRefTarget(@Nullable String id, @Nullable String className) {
    }
}
