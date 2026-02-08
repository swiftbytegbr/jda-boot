package de.swiftbyte.jdaboot.interaction.button;

import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonDefinition;
import de.swiftbyte.jdaboot.variables.VariableProcessor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The AdvancedButton class is responsible for generating advanced buttons based on a provided TemplateButton.
 * It allows setting variables that can be used in the button and provides methods to generate the final button.
 *
 * @since 1.0.0-alpha.6
 */
public class AdvancedButton {

    private @NonNull TemplateButton template;

    @Getter
    @Setter
    private @NonNull DiscordLocale locale;

    private @NonNull HashMap<@NonNull String, @NonNull String> variables = new HashMap<>();
    private static final @NonNull ConcurrentHashMap<@NonNull String, @NonNull Map<@NonNull String, @NonNull String>> variableTransfer = new ConcurrentHashMap<>();

    /**
     * Constructor for AdvancedButton. Initializes the button with the specified template, variables, and locale.
     *
     * @param template The TemplateButton to use as a base for the advanced button.
     * @param locale   The locale to use for the button.
     * @since 1.0.0-alpha.6
     */
    protected AdvancedButton(@NonNull TemplateButton template, @NonNull DiscordLocale locale) {
        this.template = template;
        this.locale = locale;
    }

    /**
     * Sets a variable to be used in the button.
     *
     * @param key   The name of the variable.
     * @param value The value of the variable.
     * @return The AdvancedButton instance for chaining.
     * @throws NullPointerException If the variable key or value is null.
     * @since 1.0.0-alpha.6
     */
    public @NonNull AdvancedButton setVariable(@NonNull String key, @NonNull String value) {
        variables.put(key, value);
        return this;
    }

    /**
     * Generates a Button based on the template and the set variables.
     *
     * @return The generated Button.
     * @since 1.0.0-alpha.6
     */
    public @NonNull Button build() {

        String variableId = UUID.randomUUID().toString();
        variableTransfer.put(variableId, Map.copyOf(variables));

        ButtonDefinition definition = template.getDefinition();
        String id = template.getId() + ";" + variableId;
        String label = processVar(definition.label());

        Button btn = switch (definition.type()) {
            case PRIMARY -> Button.primary(id, label);
            case DANGER -> Button.danger(id, label);
            case SUCCESS -> Button.success(id, label);
            case SECONDARY -> Button.secondary(id, label);
            case LINK -> Button.link(processVar(definition.url()), label);
        };

        if (!definition.emoji().isEmpty()) {
            btn = btn.withEmoji(Emoji.fromUnicode(processVar(definition.emoji())));
        }

        return btn;
    }

    /**
     * Processes the variables in the given string.
     *
     * @param old The original string with placeholders.
     * @return The processed string with placeholders replaced by variable values.
     * @since 1.0.0-alpha.6
     */
    private @NonNull String processVar(@NonNull String old) {
        return VariableProcessor.processVariable(locale, old, variables, template.getDefinition().defaultVars());
    }

    /**
     * Retrieves the variables associated with the specified ID.
     *
     * @param id The ID of the button.
     * @return The variables associated with the ID.
     * @since 1.0.0-alpha.9
     */
    public static @Nullable HashMap<@NonNull String, @NonNull String> getVariablesFromId(@NonNull String id) {
        Map<String, String> storedVariables = variableTransfer.get(id);
        if (storedVariables == null) {
            return null;
        }
        return new HashMap<>(storedVariables);
    }

}
