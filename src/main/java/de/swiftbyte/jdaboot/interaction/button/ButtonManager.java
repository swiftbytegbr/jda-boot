package de.swiftbyte.jdaboot.interaction.button;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonByClass;
import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonById;
import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonDefinition;
import de.swiftbyte.jdaboot.exceptions.ElementNotFoundException;
import de.swiftbyte.jdaboot.exceptions.ElementRegistrationException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

/**
 * The ButtonManager class extends ListenerAdapter and is responsible for managing bot buttons in the application.
 * It maintains a map of button IDs to ButtonExecutor instances, and handles button interaction events.
 *
 * @since alpha.4
 */
@Slf4j
public class ButtonManager extends ListenerAdapter {

    /**
     * The map of button IDs to ButtonExecutor instances.
     */
    private @NonNull HashMap<@NonNull String, @NonNull ButtonExecutor> buttonExecutableList = new HashMap<>();

    /**
     * The map of classes to button IDs.
     */
    private @NonNull HashMap<@NonNull Class<?>, @NonNull String> classList = new HashMap<>();


    /**
     * Constructor for ButtonManager. Initializes the manager with the specified JDA instance and main class.
     * It uses reflection to find classes annotated with @ButtonDefinition and creates instances of those classes.
     *
     * @param mainClass The main class of your project.
     * @since alpha.4
     */
    public ButtonManager(@NonNull Class<?> mainClass, @NonNull ShardManager shardManager) {
        Reflections reflections = new Reflections(mainClass.getPackageName(), Scanners.FieldsAnnotated, Scanners.TypesAnnotated);

        reflections.getTypesAnnotatedWith(ButtonDefinition.class).forEach(clazz -> {

            ButtonDefinition annotation = clazz.getAnnotation(ButtonDefinition.class);

            String id = annotation.id().isEmpty() ? UUID.randomUUID().toString() : annotation.id();
            if (id.contains(";")) {
                throw new ElementRegistrationException("Button ID cannot contain semicolons!", clazz);
            }
            if (id.length() >= 60) {
                throw new ElementRegistrationException("Button ID cannot be longer than 60 characters!", clazz);
            }

            if (ButtonExecutor.class.isAssignableFrom(clazz)) {
                ButtonExecutor cmd = (ButtonExecutor) JDABootObjectManager.getOrInitialiseObject(clazz);

                buttonExecutableList.put(id, cmd);
                classList.put(clazz, id);

                log.info("Registered button {}", clazz.getName());
            }
        });

        reflections.getFieldsAnnotatedWith(ButtonById.class).forEach(field -> {
            checkTemplateButtonFieldType(field, ButtonById.class.getSimpleName());
            ButtonById annotation = field.getAnnotation(ButtonById.class);
            TemplateButton button = getButton(annotation.value());
            if (button == null) {
                throw new ElementNotFoundException("Could not find button", annotation.value(), field);
            }
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, button);
        });
        reflections.getFieldsAnnotatedWith(ButtonByClass.class).forEach(field -> {
            checkTemplateButtonFieldType(field, ButtonByClass.class.getSimpleName());
            TemplateButton button = getButton(field.getAnnotation(ButtonByClass.class).value());
            if (button == null) {
                throw new ElementNotFoundException("Could not find button", field);
            }
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, button);
        });

        shardManager.addEventListener(this);
    }

    /**
     * Retrieves the Button instance with the specified ID.
     *
     * @param id The ID of the button.
     * @return The TemplateButton instance.
     * @since alpha.4
     */
    public @Nullable TemplateButton getButton(@NonNull String id) {
        ButtonExecutor executor = buttonExecutableList.get(id);
        if (executor == null) {
            return null;
        }
        ButtonDefinition definition = executor.getClass().getAnnotation(ButtonDefinition.class);
        return new TemplateButton(definition, id);
    }

    /**
     * Retrieves the Button instance associated with the specified class.
     *
     * @param clazz The class associated with the button.
     * @return The TemplateButton instance.
     * @since alpha.4
     */
    public <T extends ButtonExecutor> @Nullable TemplateButton getButton(@NonNull Class<T> clazz) {
        String id = classList.get(clazz);
        return getButton(id);
    }

    /**
     * Handles button interaction events. When a button is clicked, this method finds the corresponding
     * ButtonExecutor instance and delegates the event to it.
     *
     * @param event The button interaction event.
     * @since alpha.4
     */
    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {

        String[] idParts = event.getComponentId().split(";");

        if (buttonExecutableList.containsKey(idParts[0])) {
            buttonExecutableList.get(idParts[0]).onButtonClick(event, idParts.length == 2 ? Objects.requireNonNullElse(AdvancedButton.getVariablesFromId(idParts[1]), new HashMap<>()) : new HashMap<>());
        }
    }

    private void checkTemplateButtonFieldType(@NonNull Field field, @NonNull String annotationName) {
        if (!TemplateButton.class.isAssignableFrom(field.getType())) {
            throw new ElementRegistrationException(
                    String.format("Fields annotated with @%s must be of type TemplateButton", annotationName),
                    field
            );
        }
    }
}
