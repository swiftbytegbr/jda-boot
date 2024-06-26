package de.swiftbyte.jdaboot.interaction.button;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonByClass;
import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonById;
import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonDefinition;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.HashMap;
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
    private HashMap<String, ButtonExecutor> buttonExecutableList = new HashMap<>();

    /**
     * The map of classes to button IDs.
     */
    private HashMap<Class<?>, String> classList = new HashMap<>();


    /**
     * Constructor for ButtonManager. Initializes the manager with the specified JDA instance and main class.
     * It uses reflection to find classes annotated with @ButtonDefinition and creates instances of those classes.
     *
     * @param jda       The JDA instance to use for button handling.
     * @param mainClass The main class of your project.
     * @since alpha.4
     */
    public ButtonManager(JDA jda, Class<?> mainClass) {
        Reflections reflections = new Reflections(mainClass.getPackageName(), Scanners.FieldsAnnotated, Scanners.TypesAnnotated);

        reflections.getTypesAnnotatedWith(ButtonDefinition.class).forEach(clazz -> {

            ButtonDefinition annotation = clazz.getAnnotation(ButtonDefinition.class);

            String id = annotation.id().isEmpty() ? UUID.randomUUID().toString() : annotation.id();
            if (id.contains(";")) {
                log.error("Button ID cannot contain semicolons on button '{}'", clazz.getName());
                return;
            }
            if (id.length() >= 60) {
                log.error("Button ID cannot be longer than 60 characters on button '{}'", clazz.getName());
                return;
            }

            if (ButtonExecutor.class.isAssignableFrom(clazz)) {
                ButtonExecutor cmd = (ButtonExecutor) JDABootObjectManager.getOrInitialiseObject(clazz);

                buttonExecutableList.put(id, cmd);
                classList.put(clazz, id);

                log.info("Registered button {}", clazz.getName());
            }
        });

        reflections.getFieldsAnnotatedWith(ButtonById.class).forEach(field -> {
            ButtonById annotation = field.getAnnotation(ButtonById.class);
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, getButton(annotation.value()));
        });
        reflections.getFieldsAnnotatedWith(ButtonByClass.class).forEach(field -> {
            ButtonByClass annotation = field.getAnnotation(ButtonByClass.class);
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, getButton(annotation.value()));
        });

        jda.addEventListener(this);
    }

    /**
     * Retrieves the Button instance with the specified ID.
     *
     * @param id The ID of the button.
     * @return The TemplateButton instance.
     * @since alpha.4
     */
    public TemplateButton getButton(String id) {
        ButtonDefinition definition = buttonExecutableList.get(id).getClass().getAnnotation(ButtonDefinition.class);
        return new TemplateButton(definition, id);
    }

    /**
     * Retrieves the Button instance associated with the specified class.
     *
     * @param clazz The class associated with the button.
     * @return The TemplateButton instance.
     * @since alpha.4
     */
    public <T extends ButtonExecutor> TemplateButton getButton(Class<T> clazz) {
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
    public void onButtonInteraction(ButtonInteractionEvent event) {

        String[] idParts = event.getComponentId().split(";");

        if (buttonExecutableList.containsKey(idParts[0])) {
            buttonExecutableList.get(idParts[0]).onButtonClick(event, idParts.length == 2 ? AdvancedButton.getVariablesFromId(idParts[1]) : new HashMap<>());
        }
    }
}
