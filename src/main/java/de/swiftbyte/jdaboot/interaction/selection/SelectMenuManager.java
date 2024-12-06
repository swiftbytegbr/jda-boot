package de.swiftbyte.jdaboot.interaction.selection;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.interaction.selection.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.HashMap;
import java.util.UUID;

/**
 * The SelectMenuManager class extends ListenerAdapter and is responsible for managing bot select menus in the application.
 * It maintains a map of select menu IDs to StringSelectMenuExecutor or EntitySelectMenuExecutor instances, and handles select menu interaction events.
 *
 * @since 1.0.0-alpha.11
 */
@Slf4j
public class SelectMenuManager extends ListenerAdapter {

    /**
     * The map of select menu IDs to StringSelectMenuExecutor instances.
     */
    private HashMap<String, StringSelectMenuExecutor> stringSelectMenuExecutableList = new HashMap<>();

    /**
     * The map of select menu IDs to EntitySelectMenuExecutor instances.
     */
    private HashMap<String, EntitySelectMenuExecutor> entitySelectMenuExecutableList = new HashMap<>();

    /**
     * The map of classes to select menu IDs.
     */
    private HashMap<Class<?>, String> classList = new HashMap<>();


    /**
     * Constructor for SelectMenuManager. Initializes the manager with the specified JDA instance and main class.
     * It uses reflection to find classes annotated with @StringSelectMenuDefinition or @EntitySelectMenuDefinition and creates instances of those classes.
     *
     * @param jda       The JDA instance to use for button handling.
     * @param mainClass The main class of your project.
     * @since 1.0.0-alpha.11
     */
    public SelectMenuManager(JDA jda, Class<?> mainClass) {
        Reflections reflections = new Reflections(mainClass.getPackageName(), Scanners.FieldsAnnotated, Scanners.TypesAnnotated);

        reflections.getTypesAnnotatedWith(StringSelectMenuDefinition.class).forEach(clazz -> {

            StringSelectMenuDefinition annotation = clazz.getAnnotation(StringSelectMenuDefinition.class);

            String id = annotation.id().isEmpty() ? UUID.randomUUID().toString() : annotation.id();
            if (checkId(id, clazz)) {
                return;
            }

            if (StringSelectMenuExecutor.class.isAssignableFrom(clazz)) {
                StringSelectMenuExecutor cmd = (StringSelectMenuExecutor) JDABootObjectManager.getOrInitialiseObject(clazz);

                stringSelectMenuExecutableList.put(id, cmd);
                classList.put(clazz, id);

                log.info("Registered string select menu {}", clazz.getName());
            }
        });

        reflections.getTypesAnnotatedWith(EntitySelectMenuDefinition.class).forEach(clazz -> {

            EntitySelectMenuDefinition annotation = clazz.getAnnotation(EntitySelectMenuDefinition.class);

            String id = annotation.id().isEmpty() ? UUID.randomUUID().toString() : annotation.id();
            if (checkId(id, clazz)) {
                return;
            }

            if (EntitySelectMenuExecutor.class.isAssignableFrom(clazz)) {
                EntitySelectMenuExecutor cmd = (EntitySelectMenuExecutor) JDABootObjectManager.getOrInitialiseObject(clazz);

                entitySelectMenuExecutableList.put(id, cmd);
                classList.put(clazz, id);

                log.info("Registered entity select menu {}", clazz.getName());
            }
        });

        reflections.getFieldsAnnotatedWith(SelectMenuById.class).forEach(field -> {
            SelectMenuById annotation = field.getAnnotation(SelectMenuById.class);
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, getSelectMenu(annotation.value()));
        });
        reflections.getFieldsAnnotatedWith(StringSelectMenuByClass.class).forEach(field -> {
            StringSelectMenuByClass annotation = field.getAnnotation(StringSelectMenuByClass.class);
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, getStringSelectMenu(annotation.value()));
        });
        reflections.getFieldsAnnotatedWith(EntitySelectMenuByClass.class).forEach(field -> {
            EntitySelectMenuByClass annotation = field.getAnnotation(EntitySelectMenuByClass.class);
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, getEntitySelectMenu(annotation.value()));
        });

        jda.addEventListener(this);
    }

    private boolean checkId(String id, Class<?> clazz) {
        if (id.contains(";")) {
            log.error("SelectMenu IDs cannot contain semicolons on select menu '{}'", clazz.getName());
            return true;
        }
        if (id.length() >= 60) {
            log.error("SelectMenu ID cannot be longer than 60 characters on select menu '{}'", clazz.getName());
            return true;
        }
        return false;
    }

    /**
     * Retrieves the TemplateSelectMenu instance with the specified ID.
     *
     * @param id The ID of the select menu.
     * @return The TemplateSelectMenu instance.
     * @since 1.0.0-alpha.11
     */
    public TemplateSelectMenu getSelectMenu(String id) {

        if (stringSelectMenuExecutableList.containsKey(id)) {
            StringSelectMenuDefinition definition = stringSelectMenuExecutableList.get(id).getClass().getAnnotation(StringSelectMenuDefinition.class);
            return new TemplateSelectMenu(definition, id);
        } else if (entitySelectMenuExecutableList.containsKey(id)) {
            EntitySelectMenuDefinition definition = entitySelectMenuExecutableList.get(id).getClass().getAnnotation(EntitySelectMenuDefinition.class);
            return new TemplateSelectMenu(definition, id);
        } else {
            return null;
        }
    }

    /**
     * Retrieves the StringSelectMenu instance associated with the specified class.
     *
     * @param clazz The class associated with the select menu.
     * @return The SelectMenuButton instance.
     * @since 1.0.0-alpha.11
     */
    public <T extends StringSelectMenuExecutor> TemplateSelectMenu getStringSelectMenu(Class<T> clazz) {
        String id = classList.get(clazz);
        return getSelectMenu(id);
    }

    /**
     * Retrieves the StringSelectMenu instance associated with the specified class.
     *
     * @param clazz The class associated with the select menu.
     * @return The SelectMenuButton instance.
     * @since 1.0.0-alpha.11
     */
    public <T extends EntitySelectMenuExecutor> TemplateSelectMenu getEntitySelectMenu(Class<T> clazz) {
        String id = classList.get(clazz);
        return getSelectMenu(id);
    }

    /**
     * Handles string select interaction events. When a select menu is submitted, this method finds the corresponding
     * StringSelectMenuExecutor instance and delegates the event to it.
     *
     * @param event The string select menu interaction event.
     * @since 1.0.0-alpha.11
     */
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {

        String[] idParts = event.getComponentId().split(";");

        if (stringSelectMenuExecutableList.containsKey(idParts[0])) {
            stringSelectMenuExecutableList.get(idParts[0]).onSelectMenuSubmit(event, idParts.length == 2 ? AdvancedSelectMenu.getVariablesFromId(idParts[1]) : new HashMap<>());
        }
    }

    /**
     * Handles entity select interaction events. When a select menu is submitted, this method finds the corresponding
     * EntitySelectMenuExecutor instance and delegates the event to it.
     *
     * @param event The entity select menu interaction event.
     * @since 1.0.0-alpha.11
     */
    @Override
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
        String[] idParts = event.getComponentId().split(";");

        if (entitySelectMenuExecutableList.containsKey(idParts[0])) {
            entitySelectMenuExecutableList.get(idParts[0]).onSelectMenuSubmit(event, idParts.length == 2 ? AdvancedSelectMenu.getVariablesFromId(idParts[1]) : new HashMap<>());
        }
    }
}
