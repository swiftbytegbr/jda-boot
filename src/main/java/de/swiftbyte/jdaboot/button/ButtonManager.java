package de.swiftbyte.jdaboot.button;

import de.swiftbyte.jdaboot.JDABootConfigurationManager;
import de.swiftbyte.jdaboot.annotation.Button;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.UUID;

/**
 * The ButtonManager class extends ListenerAdapter and is responsible for managing bot buttons in the application.
 * It maintains a map of button IDs to BotButton instances, and handles button interaction events.
 *
 * @since alpha.4
 */
@Slf4j
public class ButtonManager extends ListenerAdapter {

    /**
     * The map of button IDs to BotButton instances.
     */
    private HashMap<String, BotButton> buttonExecutableList = new HashMap<>();

    /**
     * The map of classes to button IDs.
     */
    private HashMap<Class, String> classList = new HashMap<>();

    /**
     * The map of button IDs to Button instances.
     */
    private HashMap<String, net.dv8tion.jda.api.interactions.components.buttons.Button> buttonList = new HashMap<>();

    /**
     * Constructor for ButtonManager. Initializes the manager with the specified JDA instance and main class.
     * It uses reflection to find classes annotated with @Button and creates instances of those classes.
     *
     * @param jda       The JDA instance to use for button handling.
     * @param mainClass The main class of your project.
     * @since alpha.4
     */
    public ButtonManager(JDA jda, Class<?> mainClass) {
        Reflections reflections = new Reflections(mainClass.getPackageName().split("\\.")[0]);

        reflections.getTypesAnnotatedWith(Button.class).forEach(clazz -> {

            try {
                Button annotation = clazz.getAnnotation(Button.class);

                String id = annotation.id().isEmpty() ? UUID.randomUUID().toString() : annotation.id();

                if (BotButton.class.isAssignableFrom(clazz)) {
                    Constructor<?> constructor = clazz.getConstructor();
                    BotButton cmd = (BotButton) constructor.newInstance();

                    buttonExecutableList.put(id, cmd);
                    classList.put(clazz, id);
                    buttonList.put(id, createButton(annotation, id));

                }
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                log.error("Error while creating button", e);
            }
        });

        jda.addEventListener(this);
    }

    /**
     * Retrieves the Button instance with the specified ID.
     *
     * @param id The ID of the button.
     * @return The Button instance.
     * @since alpha.4
     */
    public static net.dv8tion.jda.api.interactions.components.buttons.Button getButton(String id) {
        return JDABootConfigurationManager.getButtonManager().buttonList.get(id);
    }

    /**
     * Retrieves the Button instance associated with the specified class.
     *
     * @param clazz The class associated with the button.
     * @return The Button instance.
     * @since alpha.4
     */
    public static <T> net.dv8tion.jda.api.interactions.components.buttons.Button getButton(Class<T> clazz) {
        return JDABootConfigurationManager.getButtonManager().buttonList.get(JDABootConfigurationManager.getButtonManager().classList.get(clazz));
    }

    /**
     * Creates a Button based on the provided Button annotation and ID.
     *
     * @param button The Button annotation to use for creating the Button.
     * @param id     The ID of the button.
     * @return The created Button.
     * @since alpha.4
     */
    private net.dv8tion.jda.api.interactions.components.buttons.Button createButton(Button button, String id) {

        net.dv8tion.jda.api.interactions.components.buttons.Button btn = switch (button.type()) {
            case PRIMARY -> net.dv8tion.jda.api.interactions.components.buttons.Button.primary(id, button.label());
            case DANGER -> net.dv8tion.jda.api.interactions.components.buttons.Button.danger(id, button.label());
            case SUCCESS -> net.dv8tion.jda.api.interactions.components.buttons.Button.success(id, button.label());
            case SECONDARY -> net.dv8tion.jda.api.interactions.components.buttons.Button.secondary(id, button.label());
            case LINK -> net.dv8tion.jda.api.interactions.components.buttons.Button.link(button.url(), button.label());
        };

        if (!button.emoji().isEmpty()) btn = btn.withEmoji(Emoji.fromUnicode(button.emoji()));

        return btn;
    }

    /**
     * Handles button interaction events. When a button is clicked, this method finds the corresponding
     * BotButton instance and delegates the event to it.
     *
     * @param event The button interaction event.
     * @since alpha.4
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (buttonExecutableList.containsKey(event.getComponentId())) {
            buttonExecutableList.get(event.getComponentId()).onButtonClick(event);
        }
    }
}
