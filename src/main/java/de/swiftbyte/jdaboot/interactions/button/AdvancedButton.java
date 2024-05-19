package de.swiftbyte.jdaboot.interactions.button;

import de.swiftbyte.jdaboot.annotation.interactions.button.ButtonDefinition;
import de.swiftbyte.jdaboot.variables.VariableProcessor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.HashMap;

/**
 * The AdvancedButton class is responsible for generating advanced buttons based on a provided TemplateButton.
 * It allows setting variables that can be used in the button and provides methods to generate the final button.
 *
 * @since 1.0.0-alpha.6
 */
public class AdvancedButton {

    private TemplateButton template;

    @Getter
    @Setter
    private DiscordLocale locale;

    private HashMap<String, String> variables = new HashMap<>();

    /**
     * Constructor for AdvancedButton. Initializes the button with the specified template, variables, and locale.
     *
     * @param template  The TemplateButton to use as a base for the advanced button.
     * @param locale    The locale to use for the button.
     * @since 1.0.0-alpha.6
     */
    protected AdvancedButton(TemplateButton template, DiscordLocale locale) {
        this.template = template;
        this.locale = locale;
    }

    /**
     * Sets a variable to be used in the button.
     *
     * @param key The name of the variable.
     * @param value    The value of the variable.
     * @return The AdvancedButton instance for chaining.
     * @throws NullPointerException If the variable key or value is null.
     * @since 1.0.0-alpha.6
     */
    public AdvancedButton setVariable(String key, String value) {
        variables.put(key, value);
        return this;
    }

    /**
     * Generates a MessageEmbed based on the template and the set variables.
     *
     * @return The generated MessageEmbed.
     * @since 1.0.0-alpha.6
     */
    public Button build() {

        ButtonDefinition definition = template.getDefinition();
        String id = processVar(template.getId());
        String label = processVar(definition.label());

        Button btn = switch (definition.type()) {
            case PRIMARY -> Button.primary(id, label);
            case DANGER -> Button.danger(id, label);
            case SUCCESS -> Button.success(id, label);
            case SECONDARY -> Button.secondary(id, label);
            case LINK -> Button.link(processVar(definition.url()), label);
        };

        if (!definition.emoji().isEmpty()) btn = btn.withEmoji(Emoji.fromUnicode(processVar(definition.emoji())));

        return btn;
    }

    /**
     * Processes the variables in the given string.
     *
     * @param old The original string with placeholders.
     * @return The processed string with placeholders replaced by variable values.
     * @since 1.0.0-alpha.6
     */
    private String processVar(String old) {
        return VariableProcessor.processVariable(locale, old, variables, template.getDefinition().defaultVars());
    }

}
