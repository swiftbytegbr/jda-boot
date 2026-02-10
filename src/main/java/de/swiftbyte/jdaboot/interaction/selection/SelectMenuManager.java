package de.swiftbyte.jdaboot.interaction.selection;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.interaction.selection.EntitySelectMenuByClass;
import de.swiftbyte.jdaboot.annotation.interaction.selection.EntitySelectMenuDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.selection.SelectMenuById;
import de.swiftbyte.jdaboot.annotation.interaction.selection.StringSelectMenuByClass;
import de.swiftbyte.jdaboot.annotation.interaction.selection.StringSelectMenuDefinition;
import de.swiftbyte.jdaboot.exceptions.ElementNotFoundException;
import de.swiftbyte.jdaboot.exceptions.ElementRegistrationException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;
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
    private @NonNull HashMap<@NonNull String, @NonNull StringSelectMenuExecutor> stringSelectMenuExecutableList = new HashMap<>();

    /**
     * The map of select menu IDs to EntitySelectMenuExecutor instances.
     */
    private @NonNull HashMap<@NonNull String, @NonNull EntitySelectMenuExecutor> entitySelectMenuExecutableList = new HashMap<>();

    /**
     * The map of classes to select menu IDs.
     */
    private @NonNull HashMap<@NonNull Class<?>, @NonNull String> classList = new HashMap<>();


    /**
     * Constructor for SelectMenuManager. Initializes the manager with the specified JDA instance and main class.
     * It uses reflection to find classes annotated with @StringSelectMenuDefinition or @EntitySelectMenuDefinition and creates instances of those classes.
     *
     * @param jda       The JDA instance to use for button handling.
     * @param mainClass The main class of your project.
     * @since 1.0.0-alpha.11
     */
    public SelectMenuManager(@NonNull JDA jda, @NonNull Class<?> mainClass) {
        Reflections reflections = new Reflections(mainClass.getPackageName(), Scanners.FieldsAnnotated, Scanners.TypesAnnotated);

        reflections.getTypesAnnotatedWith(StringSelectMenuDefinition.class).forEach(clazz -> {

            StringSelectMenuDefinition annotation = clazz.getAnnotation(StringSelectMenuDefinition.class);

            String id = annotation.id().isEmpty() ? UUID.randomUUID().toString() : annotation.id();
            checkId(id, clazz);

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
            checkId(id, clazz);

            if (EntitySelectMenuExecutor.class.isAssignableFrom(clazz)) {
                EntitySelectMenuExecutor cmd = (EntitySelectMenuExecutor) JDABootObjectManager.getOrInitialiseObject(clazz);

                entitySelectMenuExecutableList.put(id, cmd);
                classList.put(clazz, id);

                log.info("Registered entity select menu {}", clazz.getName());
            }
        });

        reflections.getFieldsAnnotatedWith(SelectMenuById.class).forEach(field -> {
            checkTemplateSelectMenuFieldType(field, SelectMenuById.class.getSimpleName());
            SelectMenuById annotation = field.getAnnotation(SelectMenuById.class);
            TemplateSelectMenu selectMenu = getSelectMenu(annotation.value());
            if (selectMenu == null) {
                throw new ElementNotFoundException("Could not found select menu", annotation.value(), field);
            }
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, selectMenu);
        });
        reflections.getFieldsAnnotatedWith(StringSelectMenuByClass.class).forEach(field -> {
            checkTemplateSelectMenuFieldType(field, StringSelectMenuByClass.class.getSimpleName());
            TemplateSelectMenu selectMenu = getStringSelectMenu(field.getAnnotation(StringSelectMenuByClass.class).value());
            if (selectMenu == null) {
                throw new ElementNotFoundException("Could not found select menu", field);
            }
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, selectMenu);
        });
        reflections.getFieldsAnnotatedWith(EntitySelectMenuByClass.class).forEach(field -> {
            checkTemplateSelectMenuFieldType(field, EntitySelectMenuByClass.class.getSimpleName());
            TemplateSelectMenu selectMenu = getEntitySelectMenu(field.getAnnotation(EntitySelectMenuByClass.class).value());
            if (selectMenu == null) {
                throw new ElementNotFoundException("Could not found select menu", field);
            }
            JDABootObjectManager.injectField(field.getDeclaringClass(), field, selectMenu);
        });

        jda.addEventListener(this);
    }

    private void checkId(@NonNull String id, @NonNull Class<?> clazz) {
        if (id.contains(";")) {
            throw new ElementRegistrationException("SelectMenu IDs cannot contain semicolons", clazz);
        }
        if (id.length() >= 60) {
            throw new ElementRegistrationException("SelectMenu ID cannot be longer than 60 characters", clazz);
        }
    }

    private void checkTemplateSelectMenuFieldType(@NonNull Field field, @NonNull String annotationName) {
        if (!TemplateSelectMenu.class.isAssignableFrom(field.getType())) {
            throw new ElementRegistrationException(
                    String.format("Fields annotated with @%s must be of type TemplateSelectMenu", annotationName),
                    field
            );
        }
    }

    /**
     * Retrieves the TemplateSelectMenu instance with the specified ID.
     *
     * @param id The ID of the select menu.
     * @return The TemplateSelectMenu instance.
     * @since 1.0.0-alpha.11
     */
    public @Nullable TemplateSelectMenu getSelectMenu(@NonNull String id) {

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
    public <T extends StringSelectMenuExecutor> @Nullable TemplateSelectMenu getStringSelectMenu(@NonNull Class<T> clazz) {
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
    public <T extends EntitySelectMenuExecutor> @Nullable TemplateSelectMenu getEntitySelectMenu(Class<T> clazz) {
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
    public void onStringSelectInteraction(@NonNull StringSelectInteractionEvent event) {

        String[] idParts = event.getComponentId().split(";");

        if (stringSelectMenuExecutableList.containsKey(idParts[0])) {
            stringSelectMenuExecutableList.get(idParts[0]).onSelectMenuSubmit(event, idParts.length == 2 ? Objects.requireNonNullElse(AdvancedSelectMenu.getVariablesFromId(idParts[1]), new HashMap<>()) : new HashMap<>());
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
    public void onEntitySelectInteraction(@NonNull EntitySelectInteractionEvent event) {
        String[] idParts = event.getComponentId().split(";");

        if (entitySelectMenuExecutableList.containsKey(idParts[0])) {
            entitySelectMenuExecutableList.get(idParts[0]).onSelectMenuSubmit(event, idParts.length == 2 ? Objects.requireNonNullElse(AdvancedSelectMenu.getVariablesFromId(idParts[1]), new HashMap<>()) : new HashMap<>());
        }
    }
}
