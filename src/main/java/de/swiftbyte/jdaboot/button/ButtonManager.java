package de.swiftbyte.jdaboot.button;

import de.swiftbyte.jdaboot.JDABoot;
import de.swiftbyte.jdaboot.annotation.Button;
import de.swiftbyte.jdaboot.variables.Translator;
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

@Slf4j
public class ButtonManager extends ListenerAdapter {

    private HashMap<String, BotButton> buttonExecutableList = new HashMap<>();
    private HashMap<Class, String> classList = new HashMap<>();
    private HashMap<String, net.dv8tion.jda.api.interactions.components.buttons.Button> buttonList = new HashMap<>();
    private Translator translator = JDABoot.getInstance().getTranslator();

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

    public static net.dv8tion.jda.api.interactions.components.buttons.Button getButton(String id) {
        return JDABoot.getInstance().getButtonManager().buttonList.get(id);
    }

    public static <T> net.dv8tion.jda.api.interactions.components.buttons.Button getButton(Class<T> clazz) {
        return JDABoot.getInstance().getButtonManager().buttonList.get(JDABoot.getInstance().getButtonManager().classList.get(clazz));
    }

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

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (buttonExecutableList.containsKey(event.getComponentId())) {
            buttonExecutableList.get(event.getComponentId()).onButtonClick(event);
        }
    }
}
