package de.swiftbyte.jdaboot.interaction.component.v2;

import de.swiftbyte.jdaboot.JDABootObjectManager;
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

import java.lang.reflect.Field;
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

    private final @NonNull HashMap<@NonNull String, @NonNull Map<@NonNull String, @NonNull ComponentV2LayoutDefinition>> xmlFileCache = new HashMap<>();

    private final @NonNull HashMap<@NonNull String, @NonNull ComponentV2LayoutDefinition> layoutsById = new HashMap<>();

    private final @NonNull Class<?> mainClass;

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

            TemplateComponentV2 component = resolveComponentForField(field, xmlPath, layoutId);
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, component);

            log.info("Registered component v2 field {}.{} from {}", field.getDeclaringClass().getName(), field.getName(), xmlPath);
        });
    }

    /**
     * Retrieves a Component V2 layout template by id.
     *
     * @param id The layout id.
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
     * Lists all known layout ids.
     *
     * @return Layout ids.
     * @since 1.0.0-beta.2
     */
    public @NonNull List<@NonNull String> getLayoutIds() {
        return List.copyOf(layoutsById.keySet());
    }

    private @NonNull TemplateComponentV2 resolveComponentForField(@NonNull Field field, @NonNull String xmlPath,
                                                                  @NonNull String layoutId) {
        Map<String, ComponentV2LayoutDefinition> layouts = getOrLoadFile(xmlPath);

        if (layoutId.isBlank()) {
            if (layouts.size() != 1) {
                throw new ElementRegistrationException(
                        String.format("Component XML '%s' contains %d layouts. Please set layoutId in @ComponentByPath", xmlPath, layouts.size()),
                        field
                );
            }
            ComponentV2LayoutDefinition definition = layouts.values().iterator().next();
            return new TemplateComponentV2(definition);
        }

        ComponentV2LayoutDefinition definition = layouts.get(layoutId);
        if (definition == null) {
            throw new ElementNotFoundException("Could not find component layout", layoutId, field);
        }

        return new TemplateComponentV2(definition);
    }

    private @NonNull Map<@NonNull String, @NonNull ComponentV2LayoutDefinition> getOrLoadFile(@NonNull String xmlPath) {
        if (!xmlFileCache.containsKey(xmlPath)) {
            Map<String, ComponentV2LayoutDefinition> parsed = ComponentV2XmlLoader.load(mainClass, xmlPath);
            if (parsed.isEmpty()) {
                throw new ConfigurationException("No layouts found in Component V2 XML", xmlPath);
            }

            for (Map.Entry<String, ComponentV2LayoutDefinition> entry : parsed.entrySet()) {
                if (layoutsById.containsKey(entry.getKey())) {
                    throw new ConfigurationException(
                            String.format("Duplicate Component V2 layout id across XML files: %s", entry.getKey()),
                            xmlPath
                    );
                }
                layoutsById.put(entry.getKey(), entry.getValue());
            }

            xmlFileCache.put(xmlPath, parsed);
        }

        return xmlFileCache.get(xmlPath);
    }

}
