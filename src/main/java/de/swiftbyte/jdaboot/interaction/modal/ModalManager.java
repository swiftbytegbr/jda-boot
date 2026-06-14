package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalByClass;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalById;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalByPath;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalDefinition;
import de.swiftbyte.jdaboot.exceptions.ConfigurationException;
import de.swiftbyte.jdaboot.exceptions.ElementNotFoundException;
import de.swiftbyte.jdaboot.exceptions.ElementRegistrationException;
import de.swiftbyte.jdaboot.exceptions.ObjectInitializationException;
import de.swiftbyte.jdaboot.interaction.modal.model.XmlModalLayoutDefinition;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The ModalManager class extends ListenerAdapter and is responsible for managing bot modals in the application.
 * It maintains a map of modal IDs to ModalExecutor instances, and handles modal interaction events.
 *
 * @since 1.0.0-alpha.7
 */
@Slf4j
public class ModalManager extends ListenerAdapter {

    private static final @NonNull String MODALS_DIR = "modals/";

    /**
     * The map of modal IDs to ModalExecutor instances.
     */
    private @NonNull HashMap<@NonNull String, @NonNull ModalExecutor> modalExecutableList = new HashMap<>();

    /**
     * The map of classes to modal IDs.
     */
    private HashMap<@NonNull Class<?>, @NonNull String> classList = new HashMap<>();

    private final @NonNull Class<?> mainClass;

    private final @NonNull HashMap<@NonNull String, @NonNull Map<@NonNull String, @NonNull XmlModalLayoutDefinition>> xmlFileCache = new HashMap<>();


    /**
     * Constructor for ModalManager. Initializes the manager with the specified JDA instance and main class.
     * It uses reflection to find classes annotated with @ModalDefinition and creates instances of those classes.
     *
     * @param jda       The JDA instance to use for modal handling.
     * @param mainClass The main class of your project.
     * @since 1.0.0-alpha.7
     */
    public ModalManager(@NonNull Class<?> mainClass, @NonNull ShardManager shardManager) {
        this.mainClass = mainClass;
        Reflections reflections = new Reflections(mainClass.getPackageName(), Scanners.FieldsAnnotated, Scanners.TypesAnnotated);

        reflections.getTypesAnnotatedWith(ModalDefinition.class).forEach(clazz -> {

            ModalDefinition annotation = clazz.getAnnotation(ModalDefinition.class);

            String id = annotation.id().isEmpty() ? UUID.randomUUID().toString() : annotation.id();

            if (id.contains(";")) {
                throw new ElementRegistrationException("Modal ID cannot contain semicolons!", clazz);
            }

            if (id.length() >= 60) {
                throw new ElementRegistrationException("Modal ID cannot be longer than 60 characters!", clazz);
            }

            if (ModalExecutor.class.isAssignableFrom(clazz)) {
                ModalExecutor cmd = (ModalExecutor) JDABootObjectManager.getOrInitialiseObject(clazz);

                modalExecutableList.put(id, cmd);
                classList.put(clazz, id);

                log.info("Registered modal {}", clazz.getName());
            }
        });

        reflections.getFieldsAnnotatedWith(ModalById.class).forEach(field -> {
            checkTemplateModalFieldType(field, ModalById.class.getSimpleName());
            ModalById annotation = field.getAnnotation(ModalById.class);
            TemplateModal modal = getModal(annotation.value());
            if (modal == null) {
                throw new ElementNotFoundException("Could not found modal", annotation.value(), field);
            }
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, modal);
        });
        reflections.getFieldsAnnotatedWith(ModalByClass.class).forEach(field -> {
            checkTemplateModalFieldType(field, ModalByClass.class.getSimpleName());
            TemplateModal modal = getModal(field.getAnnotation(ModalByClass.class).value());
            if (modal == null) {
                throw new ElementNotFoundException("Could not found modal", field);
            }
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, modal);
        });
        reflections.getFieldsAnnotatedWith(ModalByPath.class).forEach(field -> {
            if (!TemplateModal.class.isAssignableFrom(field.getType())) {
                throw new ElementRegistrationException("Fields annotated with @ModalByPath must be of type TemplateModal", field);
            }

            ModalByPath annotation = field.getAnnotation(ModalByPath.class);
            String xmlPath = normalizePath(annotation.value());
            TemplateModal modal = resolveXmlModalForField(field, xmlPath, annotation.layoutId());
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, modal);
            log.info("Registered XML modal field {}.{} from {}", field.getDeclaringClass().getName(), field.getName(), xmlPath);
        });

        shardManager.addEventListener(this);
    }

    /**
     * Retrieves the TemplateModal instance with the specified ID.
     *
     * @param id The ID of the modal.
     * @return The TemplateModal instance.
     * @since 1.0.0-alpha.7
     */
    public @Nullable TemplateModal getModal(@NonNull String id) {
        ModalExecutor executor = modalExecutableList.get(id);
        if (executor == null) {
            return null;
        }
        ModalDefinition definition = executor.getClass().getAnnotation(ModalDefinition.class);
        return new TemplateModal(definition, id);
    }

    /**
     * Retrieves the TemplateModal instance associated with the specified class.
     *
     * @param clazz The class associated with the modal.
     * @return The TemplateModal instance.
     * @since 1.0.0-alpha.7
     */
    public <T extends ModalExecutor> @Nullable TemplateModal getModal(@NonNull Class<T> clazz) {
        String id = classList.get(clazz);
        return getModal(id);
    }

    /**
     * Retrieves an XML modal template by XML file path and layout id.
     *
     * @param path     XML path.
     * @param layoutId Layout id.
     * @return Template or null when the layout id is unknown.
     * @since 1.0.0-beta.2
     */
    public @Nullable TemplateModal getModalByPath(@NonNull String path, @NonNull String layoutId) {
        Map<String, XmlModalLayoutDefinition> layouts = getOrLoadXmlFile(normalizePath(path));
        XmlModalLayoutDefinition definition = layouts.get(layoutId);
        if (definition == null) {
            return null;
        }
        return toTemplate(definition);
    }

    /**
     * Retrieves an XML modal template by XML file path.
     * The XML must define exactly one layout.
     *
     * @param path XML path.
     * @return Template or null when no single layout is resolvable.
     * @since 1.0.0-beta.2
     */
    public @Nullable TemplateModal getModalByPath(@NonNull String path) {
        Map<String, XmlModalLayoutDefinition> layouts = getOrLoadXmlFile(normalizePath(path));
        if (layouts.size() != 1) {
            return null;
        }
        return toTemplate(layouts.values().iterator().next());
    }

    /**
     * Handles modal interaction events. When a modal is submitted, this method finds the corresponding
     * ModalExecutor instance and delegates the event to it.
     *
     * @param event The modal interaction event.
     * @since 1.0.0-alpha.7
     */
    @Override
    public void onModalInteraction(@NonNull ModalInteractionEvent event) {

        String[] idParts = event.getModalId().split(";");

        if (modalExecutableList.containsKey(idParts[0])) {
            modalExecutableList.get(idParts[0]).onModalSubmit(event, idParts.length == 2 ? Objects.requireNonNullElse(AdvancedModal.getVariablesFromId(idParts[1]), new HashMap<>()) : new HashMap<>());
        }
    }

    private @NonNull TemplateModal resolveXmlModalForField(@NonNull Field field, @NonNull String xmlPath,
                                                           @NonNull String layoutId) {
        if (layoutId.isBlank()) {
            TemplateModal modal = getModalByPath(xmlPath);
            if (modal == null) {
                throw new ElementRegistrationException(
                        String.format("Modal XML '%s' must contain exactly one layout or set layoutId in @ModalByPath", xmlPath),
                        field
                );
            }
            return modal;
        }

        TemplateModal modal = getModalByPath(xmlPath, layoutId);
        if (modal == null) {
            throw new ElementNotFoundException("Could not find XML modal layout", layoutId, field);
        }
        return modal;
    }

    private void checkTemplateModalFieldType(@NonNull Field field, @NonNull String annotationName) {
        if (!TemplateModal.class.isAssignableFrom(field.getType())) {
            throw new ElementRegistrationException(
                    String.format("Fields annotated with @%s must be of type TemplateModal", annotationName),
                    field
            );
        }
    }

    private @NonNull Map<@NonNull String, @NonNull XmlModalLayoutDefinition> getOrLoadXmlFile(@NonNull String xmlPath) {
        if (!xmlFileCache.containsKey(xmlPath)) {
            Map<String, XmlModalLayoutDefinition> parsed = ModalXmlLoader.load(mainClass, xmlPath);
            if (parsed.isEmpty()) {
                throw new ConfigurationException("No modal layouts found in XML", xmlPath);
            }
            xmlFileCache.put(xmlPath, parsed);
        }
        return xmlFileCache.get(xmlPath);
    }

    private @NonNull TemplateModal toTemplate(@NonNull XmlModalLayoutDefinition definition) {
        String source = definition.sourcePath() + ", layout: " + definition.id();
        String modalId = definition.modalId();
        if (modalId != null) {
            if (!modalExecutableList.containsKey(modalId)) {
                throw new ConfigurationException(
                        String.format("Could not find modal id '%s' referenced by XML layout '%s'", modalId, definition.id()),
                        definition.sourcePath(),
                        "/layout[@id='" + definition.id() + "']"
                );
            }
            return new TemplateModal(definition, modalId);
        }

        String modalClassName = Objects.requireNonNull(definition.modalClassName());
        try {
            Class<?> rawClass = Class.forName(modalClassName, true, mainClass.getClassLoader());
            if (!ModalExecutor.class.isAssignableFrom(rawClass)) {
                throw new ObjectInitializationException(
                        String.format("Referenced modal class '%s' does not implement %s", modalClassName, ModalExecutor.class.getName()),
                        rawClass,
                        source
                );
            }

            String id = classList.get(rawClass);
            if (id == null || !modalExecutableList.containsKey(id)) {
                throw new ConfigurationException(
                        String.format("Could not find registered modal for class '%s' referenced by XML layout '%s'", modalClassName, definition.id()),
                        definition.sourcePath(),
                        "/layout[@id='" + definition.id() + "']"
                );
            }

            return new TemplateModal(definition, id);
        } catch (ClassNotFoundException e) {
            throw new ObjectInitializationException("Could not load modal class: " + modalClassName, source, e);
        }
    }

    private @NonNull String normalizePath(@NonNull String path) {
        String normalized = path.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.startsWith(MODALS_DIR)) {
            normalized = MODALS_DIR + normalized;
        }
        return normalized;
    }
}
