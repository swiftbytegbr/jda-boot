package de.swiftbyte.jdaboot.interaction.selection;

import de.swiftbyte.jdaboot.annotation.interaction.selection.EntitySelectMenuDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.selection.StringSelectMenuDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.selection.StringSelectOption;
import de.swiftbyte.jdaboot.utils.StringUtils;
import de.swiftbyte.jdaboot.variables.VariableProcessor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.*;

/**
 * The AdvancedSelectMenu class is responsible for generating advanced select menus based on a provided TemplateSelectMenu.
 * It allows setting variables that can be used in the select menu and provides methods to generate the final select menu.
 *
 * @since 1.0.0-alpha.11
 */
public class AdvancedSelectMenu {

    private TemplateSelectMenu template;

    @Getter
    @Setter
    private DiscordLocale locale;

    private HashMap<String, String> variables = new HashMap<>();
    private static HashMap<String, HashMap<String, String>> variableTransfer = new HashMap<>();

    private List<EntitySelectMenu.DefaultValue> defaultValues = new ArrayList<>();

    private List<DynamicStringSelectMenuOption> dynamicOptions = new ArrayList<>();

    /**
     * Constructor for AdvancedSelectMenu. Initializes the select menu with the specified template, variables, and locale.
     *
     * @param template The TemplateSelectMenu to use as a base for the advanced select menu.
     * @param locale   The locale to use for the select menu.
     * @since 1.0.0-alpha.11
     */
    protected AdvancedSelectMenu(TemplateSelectMenu template, DiscordLocale locale) {
        this.template = template;
        this.locale = locale;
    }

    /**
     * Sets a variable to be used in the select menu.
     *
     * @param key   The name of the variable.
     * @param value The value of the variable.
     * @return The AdvancedSelectMenu instance for chaining.
     * @throws NullPointerException If the variable key or value is null.
     * @since 1.0.0-alpha.11
     */
    public AdvancedSelectMenu setVariable(String key, String value) {
        variables.put(key, value);
        return this;
    }

    /**
     * Adds a default value to the entity select menu.
     *
     * @param defaultValue The default value to add.
     * @return The AdvancedSelectMenu instance for chaining.
     * @since 1.0.0-alpha.11
     */
    public AdvancedSelectMenu addEntityDefaultValue(EntitySelectMenu.DefaultValue defaultValue) {
        defaultValues.add(defaultValue);
        return this;
    }

    /**
     * Add an option to the string select menu at runtime. The option also supports variables.
     *
     * @param label       The label of the option.
     * @param value       The value of the option.
     * @param description The description of the option.
     * @param emoji       The emoji of the option.
     * @param isDefault   true if the option is selected by default.
     * @return The AdvancedSelectMenu instance for chaining.
     * @since 1.0.0-alpha.11
     */
    public AdvancedSelectMenu addDynamicOption(String label, String value, String description, String emoji, boolean isDefault) {
        dynamicOptions.add(new DynamicStringSelectMenuOption(label, value, description, emoji, isDefault));
        return this;
    }

    /**
     * Add an option to the string select menu at runtime. The option also supports variables.
     *
     * @param label The label of the option.
     * @param value The value of the option.
     * @return The AdvancedSelectMenu instance for chaining.
     * @since 1.0.0-alpha.11
     */
    public AdvancedSelectMenu addDynamicOption(String label, String value) {
        dynamicOptions.add(new DynamicStringSelectMenuOption(label, value, "", "", false));
        return this;
    }

    /**
     * Add an option to the string select menu at runtime. The option also supports variables.
     *
     * @param dynamicOption The option to add.
     * @return The AdvancedSelectMenu instance for chaining.
     * @since 1.0.0-alpha.11
     */
    public AdvancedSelectMenu addDynamicOption(DynamicStringSelectMenuOption dynamicOption) {
        dynamicOptions.add(dynamicOption);
        return this;
    }

    /**
     * Add an option to the string select menu at runtime. The option also supports variables.
     *
     * @param dynamicOptions The options to add.
     * @return The AdvancedSelectMenu instance for chaining.
     * @since 1.0.0-alpha.11
     */
    public AdvancedSelectMenu addDynamicOption(DynamicStringSelectMenuOption... dynamicOptions) {
        this.dynamicOptions.addAll(List.of(dynamicOptions));
        return this;
    }

    /**
     * Add an option to the string select menu at runtime. The option also supports variables.
     *
     * @param dynamicOptions The options to add.
     * @return The AdvancedSelectMenu instance for chaining.
     * @since 1.0.0-alpha.11
     */
    public AdvancedSelectMenu addDynamicOption(Collection<DynamicStringSelectMenuOption> dynamicOptions) {
        this.dynamicOptions.addAll(dynamicOptions);
        return this;
    }

    /**
     * Generates a select menu based on the template and the set variables.
     *
     * @return The generated select menu.
     * @since 1.0.0-alpha.11
     */
    public SelectMenu build() {

        if (template.getStringDefinition() != null) {
            return buildStringSelectMenu();
        } else if (template.getEntityDefinition() != null) {
            return buildEntitySelectMenu();
        }
        return null;
    }

    /**
     * Generates an entity select menu based on the template and the set variables.
     *
     * @return The generated entity select menu or null when the template is from a different type.
     * @since 1.0.0-alpha.11
     */
    public EntitySelectMenu buildEntitySelectMenu() {

        if (template.getEntityDefinition() == null) {
            return null;
        }

        String variableId = UUID.randomUUID().toString();
        variableTransfer.put(variableId, variables);

        EntitySelectMenuDefinition definition = template.getEntityDefinition();
        String id = template.getId() + ";" + variableId;

        EntitySelectMenu.Builder menuBuilder = EntitySelectMenu.create(id, getEntitySelectMenuTargetTypes(definition))
                .setDisabled(definition.isDisabled())
                .setMaxValues(definition.maxOptions())
                .setMinValues(definition.minOptions())
                .setChannelTypes(definition.channelTypes())
                .setDefaultValues(defaultValues);

        if (StringUtils.isNotBlank(definition.placeholder())) {
            menuBuilder.setPlaceholder(processVar(definition.placeholder()));
        }

        return menuBuilder.build();
    }

    /**
     * Retrieves the list of EntitySelectMenu.SelectTarget types based on the provided EntitySelectMenuDefinition.
     * This method checks the definition for enabled target types such as channels, roles, and users.
     *
     * @param definition The EntitySelectMenuDefinition containing the target type settings.
     * @return A list of EntitySelectMenu.SelectTarget types for the entity select menu.
     * @since 1.0.0-alpha.11
     */
    private List<EntitySelectMenu.SelectTarget> getEntitySelectMenuTargetTypes(EntitySelectMenuDefinition definition) {

        List<EntitySelectMenu.SelectTarget> selectTargets = new ArrayList<>();

        if (definition.enableChannel()) {
            selectTargets.add(EntitySelectMenu.SelectTarget.CHANNEL);
        }

        if (definition.enableRoles()) {
            selectTargets.add(EntitySelectMenu.SelectTarget.ROLE);
        }

        if (definition.enableUser()) {
            selectTargets.add(EntitySelectMenu.SelectTarget.USER);
        }

        return selectTargets;
    }

    /**
     * Generates a string select menu based on the template and the set variables.
     *
     * @return The generated string select menu or null when the template is from a different type.
     * @since 1.0.0-alpha.11
     */
    public StringSelectMenu buildStringSelectMenu() {

        if (template.getStringDefinition() == null) {
            return null;
        }

        String variableId = UUID.randomUUID().toString();
        variableTransfer.put(variableId, variables);

        StringSelectMenuDefinition definition = template.getStringDefinition();
        String id = template.getId() + ";" + variableId;

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(id)
                .addOptions(generateStringSelectOptions(definition))
                .setDisabled(definition.isDisabled())
                .setMaxValues(definition.maxOptions())
                .setMinValues(definition.minOptions());

        if (StringUtils.isNotBlank(definition.placeholder())) {
            menuBuilder.setPlaceholder(processVar(definition.placeholder()));
        }

        return menuBuilder.build();
    }

    /**
     * Generates a list of SelectOption objects based on the provided StringSelectMenuDefinition.
     * This method processes both predefined options from the definition and dynamic options added at runtime.
     *
     * @param definition The StringSelectMenuDefinition containing the predefined options.
     * @return A list of SelectOption objects for the string select menu.
     * @since 1.0.0-alpha.11
     */
    private List<SelectOption> generateStringSelectOptions(StringSelectMenuDefinition definition) {

        List<SelectOption> selectOptions = new ArrayList<>();

        for (StringSelectOption optionDefinition : definition.options()) {
            SelectOption option = SelectOption.of(processVar(optionDefinition.label()), processVar(optionDefinition.value()));
            if (StringUtils.isNotBlank(optionDefinition.description())) {
                option = option.withDescription(processVar(optionDefinition.description()));
            }

            if (StringUtils.isNotBlank(optionDefinition.emoji())) {
                option = option.withEmoji(Emoji.fromUnicode(processVar(optionDefinition.emoji())));
            }

            if (optionDefinition.isDefault()) {
                option = option.withDefault(optionDefinition.isDefault());
            }
            selectOptions.add(option);
        }

        for (DynamicStringSelectMenuOption optionDefinition : dynamicOptions) {
            SelectOption option = SelectOption.of(processVar(optionDefinition.label()), processVar(optionDefinition.value()));
            if (StringUtils.isNotBlank(optionDefinition.description())) {
                option = option.withDescription(processVar(optionDefinition.description()));
            }

            if (StringUtils.isNotBlank(optionDefinition.emoji())) {
                option = option.withEmoji(Emoji.fromUnicode(processVar(optionDefinition.emoji())));
            }

            if (optionDefinition.isDefault()) {
                option = option.withDefault(optionDefinition.isDefault());
            }
            selectOptions.add(option);
        }

        return selectOptions;
    }

    /**
     * Processes the variables in the given string.
     *
     * @param old The original string with placeholders.
     * @return The processed string with placeholders replaced by variable values.
     * @since 1.0.0-alpha.11
     */
    private String processVar(String old) {
        if (template.getStringDefinition() != null) {
            return VariableProcessor.processVariable(locale, old, variables, template.getStringDefinition().defaultVars());
        }

        return VariableProcessor.processVariable(locale, old, variables, template.getEntityDefinition().defaultVars());
    }

    /**
     * Retrieves the variables associated with the specified ID.
     *
     * @param id The ID of the select menu.
     * @return The variables associated with the ID.
     * @since 1.0.0-alpha.11
     */
    public static HashMap<String, String> getVariablesFromId(String id) {
        return variableTransfer.get(id);
    }

    /**
     * Used to store dynamic string select options that are added at runtime.
     *
     * @since 1.0.0-alpha.11
     */
    public record DynamicStringSelectMenuOption(String label, String value, String description, String emoji,
                                                boolean isDefault) {

    }
}
