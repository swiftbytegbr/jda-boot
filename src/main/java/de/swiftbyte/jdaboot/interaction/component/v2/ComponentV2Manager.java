package de.swiftbyte.jdaboot.interaction.component.v2;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import de.swiftbyte.jdaboot.annotation.interaction.component.ComponentByPath;
import de.swiftbyte.jdaboot.exceptions.ConfigurationException;
import de.swiftbyte.jdaboot.exceptions.ElementNotFoundException;
import de.swiftbyte.jdaboot.exceptions.ElementRegistrationException;
import de.swiftbyte.jdaboot.interaction.component.v2.model.ComponentV2LayoutDefinition;
import lombok.CustomLog;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.normalizeResourcePath;

/**
 * Manages Component V2 layouts loaded from XML resources and supports field injection.
 *
 * @since 1.0.0-beta.2
 */
@CustomLog
public class ComponentV2Manager {

    private static final @NonNull String COMPONENTS_DIR = "components/";
    private static final @NonNull String INLINE_XML_SOURCE = "inline Component V2 XML";
    private static final @NonNull String XML_STREAM_SOURCE = "Component V2 XML stream";

    private final @NonNull HashMap<@NonNull String, @NonNull Map<@NonNull String, @NonNull ComponentV2LayoutDefinition>> xmlFileCache = new HashMap<>();

    private final @NonNull HashMap<@NonNull String, @NonNull ComponentV2LayoutDefinition> layoutsById = new HashMap<>();

    private final @NonNull Class<?> mainClass;

    /**
     * Creates a manager and injects all discovered Component V2 XML templates.
     *
     * @param mainClass The application main class.
     * @since 1.0.0-beta.2
     */
    public ComponentV2Manager(@NonNull Class<?> mainClass) {

        this.mainClass = mainClass;

        Reflections reflections = new Reflections(mainClass.getPackageName(), Scanners.FieldsAnnotated);

        reflections.getFieldsAnnotatedWith(ComponentByPath.class).forEach(field -> {
            if (!TemplateComponentV2.class.isAssignableFrom(field.getType())) {
                throw new ElementRegistrationException("Fields annotated with @ComponentByPath must be of type TemplateComponentV2", field);
            }

            ComponentByPath annotation = field.getAnnotation(ComponentByPath.class);
            String xmlPath = normalizeResourcePath(annotation.value(), COMPONENTS_DIR);
            String layoutId = annotation.layoutId();

            TemplateComponentV2 component = resolveComponentForField(field, xmlPath, layoutId, annotation.defaultVars());
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, component);

            log.info("Registered component v2 field {}.{} from {}", field.getDeclaringClass().getName(), field.getName(), xmlPath);
        });
    }

    /**
     * Retrieves a Component V2 layout template by its global id.
     * Global ids are composed from the normalized XML path and layout id.
     * Example: {@code components/example.xml#main}.
     *
     * @param id The global layout id.
     * @return The template or null when not found.
     * @since 1.0.0-beta.2
     */
    public @Nullable TemplateComponentV2 getComponent(@NonNull String id) {
        ComponentV2LayoutDefinition definition = layoutsById.get(id);
        if (definition == null) {
            return null;
        }
        return new TemplateComponentV2(definition);
    }

    /**
     * Retrieves a Component V2 layout template by XML file path and layout id.
     *
     * @param path     XML path.
     * @param layoutId Layout id.
     * @return Template or null when not found.
     * @since 1.0.0-beta.2
     */
    public @Nullable TemplateComponentV2 getComponentByPath(@NonNull String path, @NonNull String layoutId) {
        Map<String, ComponentV2LayoutDefinition> layouts = getOrLoadFile(normalizeResourcePath(path, COMPONENTS_DIR));
        ComponentV2LayoutDefinition definition = layouts.get(layoutId);
        if (definition == null) {
            return null;
        }
        return new TemplateComponentV2(definition);
    }

    /**
     * Retrieves a Component V2 layout template by XML file path.
     * The XML must define exactly one layout.
     *
     * @param path XML path.
     * @return Template or null when file has no layout.
     * @since 1.0.0-beta.2
     */
    public @Nullable TemplateComponentV2 getComponentByPath(@NonNull String path) {
        Map<String, ComponentV2LayoutDefinition> layouts = getOrLoadFile(normalizeResourcePath(path, COMPONENTS_DIR));
        if (layouts.size() != 1) {
            return null;
        }
        return new TemplateComponentV2(layouts.values().iterator().next());
    }

    /**
     * Loads a Component V2 template from an XML string.
     * The XML document must contain exactly one layout.
     *
     * @param xml The Component V2 XML document.
     * @return The parsed component template.
     * @throws ConfigurationException If the XML is invalid or does not contain exactly one layout.
     * @since 1.0.0-beta.2
     */
    public @NonNull TemplateComponentV2 loadComponent(@NonNull String xml) {
        return loadComponent(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                null,
                INLINE_XML_SOURCE
        );
    }

    /**
     * Loads a selected Component V2 template from an XML string.
     *
     * @param xml      The Component V2 XML document.
     * @param layoutId The ID of the layout to return.
     * @return The parsed component template.
     * @throws ConfigurationException If the XML is invalid or the layout does not exist.
     * @since 1.0.0-beta.2
     */
    public @NonNull TemplateComponentV2 loadComponent(@NonNull String xml, @NonNull String layoutId) {
        return loadComponent(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                layoutId,
                INLINE_XML_SOURCE
        );
    }

    /**
     * Loads a Component V2 template from an XML stream.
     * The XML document must contain exactly one layout. This method does not close the stream.
     *
     * @param xmlStream The Component V2 XML stream.
     * @return The parsed component template.
     * @throws ConfigurationException If the XML is invalid or does not contain exactly one layout.
     * @since 1.0.0-beta.2
     */
    public @NonNull TemplateComponentV2 loadComponent(@NonNull InputStream xmlStream) {
        return loadComponent(xmlStream, null, XML_STREAM_SOURCE);
    }

    /**
     * Loads a selected Component V2 template from an XML stream.
     * This method does not close the stream.
     *
     * @param xmlStream The Component V2 XML stream.
     * @param layoutId  The ID of the layout to return.
     * @return The parsed component template.
     * @throws ConfigurationException If the XML is invalid or the layout does not exist.
     * @since 1.0.0-beta.2
     */
    public @NonNull TemplateComponentV2 loadComponent(@NonNull InputStream xmlStream,
                                                       @NonNull String layoutId) {
        return loadComponent(xmlStream, layoutId, XML_STREAM_SOURCE);
    }

    /**
     * Lists all known global layout ids.
     *
     * @return Global layout ids.
     * @since 1.0.0-beta.2
     */
    public @NonNull List<@NonNull String> getLayoutIds() {
        return List.copyOf(layoutsById.keySet());
    }

    /**
     * Resolves the XML component template requested by an annotated field.
     *
     * @param field    The field receiving the template.
     * @param xmlPath  The normalized XML resource path.
     * @param layoutId The requested layout ID, or an empty string for a single-layout file.
     * @param defaultVars The default variables configured on the injection annotation.
     * @return The resolved component template.
     * @since 1.0.0-beta.2
     */
    private @NonNull TemplateComponentV2 resolveComponentForField(@NonNull Field field, @NonNull String xmlPath,
                                                                  @NonNull String layoutId,
                                                                  @NonNull DefaultVariable @NonNull [] defaultVars) {
        Map<String, ComponentV2LayoutDefinition> layouts = getOrLoadFile(xmlPath);

        if (layoutId.isBlank()) {
            if (layouts.size() != 1) {
                throw new ElementRegistrationException(
                        String.format("Component XML '%s' contains %d layouts. Please set layoutId in @ComponentByPath", xmlPath, layouts.size()),
                        field
                );
            }
            ComponentV2LayoutDefinition definition = layouts.values().iterator().next();
            return new TemplateComponentV2(definition, defaultVars);
        }

        ComponentV2LayoutDefinition definition = layouts.get(layoutId);
        if (definition == null) {
            throw new ElementNotFoundException("Could not find component layout", layoutId, field);
        }

        return new TemplateComponentV2(definition, defaultVars);
    }

    /**
     * Returns cached layouts or loads them from an XML resource.
     *
     * @param xmlPath The normalized XML resource path.
     * @return The layouts indexed by layout ID.
     * @since 1.0.0-beta.2
     */
    private @NonNull Map<@NonNull String, @NonNull ComponentV2LayoutDefinition> getOrLoadFile(@NonNull String xmlPath) {
        if (!xmlFileCache.containsKey(xmlPath)) {
            Map<String, ComponentV2LayoutDefinition> parsed = ComponentV2XmlLoader.load(mainClass, xmlPath);
            if (parsed.isEmpty()) {
                throw new ConfigurationException("No layouts found in Component V2 XML", xmlPath);
            }

            for (Map.Entry<String, ComponentV2LayoutDefinition> entry : parsed.entrySet()) {
                String globalLayoutId = globalLayoutId(xmlPath, entry.getKey());
                if (layoutsById.containsKey(globalLayoutId)) {
                    throw new ConfigurationException(
                            String.format("Duplicate Component V2 global layout id: %s", globalLayoutId),
                            xmlPath
                    );
                }
                layoutsById.put(globalLayoutId, entry.getValue());
            }

            xmlFileCache.put(xmlPath, parsed);
        }

        return xmlFileCache.get(xmlPath);
    }

    /**
     * Parses an XML source and selects one component layout.
     *
     * @param xmlStream       The XML input stream.
     * @param layoutId        The requested layout ID, or {@code null} when exactly one layout is expected.
     * @param sourceReference A description of the XML source used in error messages.
     * @return The selected component template.
     * @since 1.0.0-beta.2
     */
    private @NonNull TemplateComponentV2 loadComponent(@NonNull InputStream xmlStream,
                                                        @Nullable String layoutId,
                                                        @NonNull String sourceReference) {
        Map<String, ComponentV2LayoutDefinition> layouts = ComponentV2XmlLoader.load(xmlStream, sourceReference);

        if (layoutId == null) {
            if (layouts.size() != 1) {
                throw new ConfigurationException(
                        String.format("Component V2 XML must contain exactly one layout but contains %d", layouts.size()),
                        sourceReference
                );
            }
            return new TemplateComponentV2(layouts.values().iterator().next());
        }

        ComponentV2LayoutDefinition definition = layouts.get(layoutId);
        if (definition == null) {
            throw new ConfigurationException(
                    String.format("Could not find Component V2 layout id '%s'", layoutId),
                    sourceReference
            );
        }
        return new TemplateComponentV2(definition);
    }

    /**
     * Builds the global layout id used for cross-file lookups.
     *
     * @param xmlPath  The normalized XML resource path.
     * @param layoutId The layout id inside the XML file.
     * @return The global layout id.
     * @since 1.0.0-beta.2
     */
    private static @NonNull String globalLayoutId(@NonNull String xmlPath, @NonNull String layoutId) {
        return xmlPath + "#" + layoutId;
    }

}
