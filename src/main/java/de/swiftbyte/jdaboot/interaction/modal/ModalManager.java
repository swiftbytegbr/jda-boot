package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalByClass;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalById;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalByPath;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.modal.XmlModalDefinition;
import de.swiftbyte.jdaboot.exceptions.ConfigurationException;
import de.swiftbyte.jdaboot.exceptions.ElementNotFoundException;
import de.swiftbyte.jdaboot.exceptions.ElementRegistrationException;
import de.swiftbyte.jdaboot.exceptions.ObjectInitializationException;
import de.swiftbyte.jdaboot.interaction.modal.model.XmlModalLayoutDefinition;
import lombok.extern.slf4j.Slf4j;
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

import static de.swiftbyte.jdaboot.xml.XmlLoaderSupport.normalizeResourcePath;

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
     * Discovers modal definitions, injects templates, and registers this listener with every shard.
     *
     * @param mainClass    The main class of your project.
     * @param shardManager The shard manager used for modal interactions.
     * @since 1.0.0-alpha.7
     */
    public ModalManager(@NonNull Class<?> mainClass, @NonNull ShardManager shardManager) {
        this.mainClass = mainClass;
        Reflections reflections = new Reflections(mainClass.getPackageName(), Scanners.FieldsAnnotated, Scanners.TypesAnnotated);

        reflections.getTypesAnnotatedWith(ModalDefinition.class).forEach(clazz -> {

            ModalDefinition annotation = clazz.getAnnotation(ModalDefinition.class);

            String id = annotation.id().isEmpty() ? UUID.randomUUID().toString() : annotation.id();

            registerModalExecutor(clazz, id, "modal");
        });

        reflections.getTypesAnnotatedWith(XmlModalDefinition.class).forEach(clazz -> {
            XmlModalDefinition annotation = clazz.getAnnotation(XmlModalDefinition.class);
            String id = annotation.id().isEmpty() ? UUID.randomUUID().toString() : annotation.id();
            registerModalExecutor(clazz, id, "XML modal");
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
            String xmlPath = normalizeResourcePath(annotation.value(), MODALS_DIR);
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
        if (definition == null) {
            return null;
        }
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
        if (id == null) {
            return null;
        }
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
        Map<String, XmlModalLayoutDefinition> layouts = getOrLoadXmlFile(normalizeResourcePath(path, MODALS_DIR));
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
        Map<String, XmlModalLayoutDefinition> layouts = getOrLoadXmlFile(normalizeResourcePath(path, MODALS_DIR));
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

    /**
     * Resolves the XML modal template requested by an annotated field.
     *
     * @param field    The field receiving the template.
     * @param xmlPath  The normalized XML resource path.
     * @param layoutId The requested layout ID, or an empty string for a single-layout file.
     * @return The resolved modal template.
     * @throws ElementRegistrationException If no unambiguous layout can be selected.
     * @throws ElementNotFoundException     If the requested layout does not exist.
     * @since 1.0.0-beta.2
     */
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

    /**
     * Verifies that an annotated field can receive a {@link TemplateModal}.
     *
     * @param field          The field to validate.
     * @param annotationName The annotation name used in an error message.
     * @throws ElementRegistrationException If the field type is incompatible.
     * @since 1.0.0-beta.2
     */
    private void checkTemplateModalFieldType(@NonNull Field field, @NonNull String annotationName) {
        if (!TemplateModal.class.isAssignableFrom(field.getType())) {
            throw new ElementRegistrationException(
                    String.format("Fields annotated with @%s must be of type TemplateModal", annotationName),
                    field
            );
        }
    }

    /**
     * Returns cached modal layouts or loads them from the given XML resource.
     *
     * @param xmlPath The normalized XML resource path.
     * @return The layouts indexed by layout ID.
     * @throws ConfigurationException If the XML file contains no modal layouts.
     * @since 1.0.0-beta.2
     */
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

    /**
     * Converts an XML layout definition into a modal template.
     *
     * @param definition The parsed XML layout definition.
     * @return The modal template.
     * @throws ConfigurationException        If the referenced modal is not registered.
     * @throws ObjectInitializationException If a referenced modal class cannot be loaded or used.
     * @since 1.0.0-beta.2
     */
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
            ModalExecutor executor = modalExecutableList.get(modalId);
            return new TemplateModal(definition, modalId, getXmlDefaultVars(executor.getClass()));
        }

        String modalClassName = Objects.requireNonNull(definition.modalClassName());
        try {
            Class<?> rawClass = Class.forName(modalClassName, true, mainClass.getClassLoader());
            if (!ModalExecutor.class.isAssignableFrom(rawClass)) {
                throw new ObjectInitializationException(
                        String.format("Referenced modal class '%s' does not extend %s", modalClassName, ModalExecutor.class.getName()),
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

            return new TemplateModal(definition, id, getXmlDefaultVars(rawClass));
        } catch (ClassNotFoundException e) {
            throw new ObjectInitializationException("Could not load modal class: " + modalClassName, source, e);
        }
    }

    /**
     * Returns XML modal default variables configured on an executor class.
     *
     * @param clazz The executor class.
     * @return The configured default variables, or an empty array.
     * @since 1.0.0-beta.2
     */
    private @NonNull DefaultVariable @NonNull [] getXmlDefaultVars(@NonNull Class<?> clazz) {
        XmlModalDefinition definition = clazz.getAnnotation(XmlModalDefinition.class);
        return definition == null ? new DefaultVariable[0] : definition.defaultVars();
    }

    /**
     * Registers a modal executor class.
     *
     * @param clazz       The executor class.
     * @param id          The resolved modal ID.
     * @param description The registration description used for logging.
     * @throws ElementRegistrationException If the class or ID is invalid.
     * @since 1.0.0-beta.2
     */
    private void registerModalExecutor(@NonNull Class<?> clazz, @NonNull String id, @NonNull String description) {
        if (id.contains(";")) {
            throw new ElementRegistrationException("Modal ID cannot contain semicolons!", clazz);
        }

        if (id.length() >= 60) {
            throw new ElementRegistrationException("Modal ID cannot be longer than 60 characters!", clazz);
        }

        if (!ModalExecutor.class.isAssignableFrom(clazz)) {
            throw new ElementRegistrationException("Modal class must extend ModalExecutor", clazz);
        }

        if (classList.containsKey(clazz)) {
            throw new ElementRegistrationException("Modal class is already registered", clazz);
        }

        if (modalExecutableList.containsKey(id)) {
            throw new ElementRegistrationException("Duplicate modal ID: " + id, clazz);
        }

        ModalExecutor executor = (ModalExecutor) JDABootObjectManager.getOrInitialiseObject(clazz);
        modalExecutableList.put(id, executor);
        classList.put(clazz, id);

        log.info("Registered {} {}", description, clazz.getName());
    }

}
