package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalByClass;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalById;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalDefinition;
import de.swiftbyte.jdaboot.exceptions.ElementNotFoundException;
import de.swiftbyte.jdaboot.exceptions.ElementRegistrationException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.HashMap;
import java.util.UUID;

/**
 * The ModalManager class extends ListenerAdapter and is responsible for managing bot modals in the application.
 * It maintains a map of modal IDs to ModalExecutor instances, and handles modal interaction events.
 *
 * @since 1.0.0-alpha.7
 */
@Slf4j
public class ModalManager extends ListenerAdapter {

    /**
     * The map of modal IDs to ModalExecutor instances.
     */
    private @NonNull HashMap<@NonNull String, @NonNull ModalExecutor> modalExecutableList = new HashMap<>();

    /**
     * The map of classes to modal IDs.
     */
    private HashMap<@NonNull Class<?>, @NonNull String> classList = new HashMap<>();


    /**
     * Constructor for ModalManager. Initializes the manager with the specified JDA instance and main class.
     * It uses reflection to find classes annotated with @ModalDefinition and creates instances of those classes.
     *
     * @param jda       The JDA instance to use for modal handling.
     * @param mainClass The main class of your project.
     * @since 1.0.0-alpha.7
     */
    public ModalManager(@NonNull JDA jda, @NonNull Class<?> mainClass) {
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
            ModalById annotation = field.getAnnotation(ModalById.class);
            TemplateModal modal = getModal(annotation.value());
            if (modal == null) {
                throw new ElementNotFoundException("Could not found modal", annotation.value(), field);
            }
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, modal);
        });
        reflections.getFieldsAnnotatedWith(ModalByClass.class).forEach(field -> {
            TemplateModal modal = getModal(field.getAnnotation(ModalByClass.class).value());
            if (modal == null) {
                throw new ElementNotFoundException("Could not found modal", field);
            }
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, modal);
        });

        jda.addEventListener(this);
    }

    /**
     * Retrieves the TemplateModal instance with the specified ID.
     *
     * @param id The ID of the modal.
     * @return The TemplateModal instance.
     * @since 1.0.0-alpha.7
     */
    public @Nullable TemplateModal getModal(@NonNull String id) {
        ModalDefinition definition = modalExecutableList.get(id).getClass().getAnnotation(ModalDefinition.class);
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
            modalExecutableList.get(idParts[0]).onModalSubmit(event, idParts.length == 2 ? AdvancedModal.getVariablesFromId(idParts[1]) : new HashMap<>());
        }
    }
}
