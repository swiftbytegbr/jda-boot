package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalRow;
import de.swiftbyte.jdaboot.interaction.button.TemplateButton;
import de.swiftbyte.jdaboot.utils.StringUtils;
import de.swiftbyte.jdaboot.variables.VariableProcessor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The AdvancedModal class is responsible for generating advanced modals based on a provided TemplateModal.
 * It allows setting variables that can be used in the modal and provides methods to generate the final modal.
 *
 * @since 1.0.0-alpha.7
 */
public class AdvancedModal {

    private TemplateModal template;

    @Getter
    @Setter
    private DiscordLocale locale;

    private HashMap<String, String> variables = new HashMap<>();

    /**
     * Constructor for AdvancedModal. Initializes the modal with the specified template, variables, and locale.
     *
     * @param template The TemplateModal to use as a base for the advanced button.
     * @param locale   The locale to use for the modal.
     * @since 1.0.0-alpha.7
     */
    protected AdvancedModal(TemplateModal template, DiscordLocale locale) {
        this.template = template;
        this.locale = locale;
    }

    /**
     * Sets a variable to be used in the modal.
     *
     * @param key   The name of the variable.
     * @param value The value of the variable.
     * @return The AdvancedModal instance for chaining.
     * @throws NullPointerException If the variable key or value is null.
     * @since 1.0.0-alpha.7
     */
    public AdvancedModal setVariable(String key, String value) {
        variables.put(key, value);
        return this;
    }

    /**
     * Generates a Modal based on the template and the set variables.
     *
     * @return The generated Modal.
     * @since 1.0.0-alpha.7
     */
    public Modal build() {

        String id = processVar(template.getId());
        String title = processVar(template.getDefinition().title());

        Modal.Builder modal = Modal.create(id, title);

        for (ModalRow inputDefinition : template.getDefinition().rows()) {
            String inputId = processVar(inputDefinition.id());
            String placeholder = processVar(inputDefinition.placeholder());
            String label = processVar(inputDefinition.label());
            TextInputStyle style = switch (inputDefinition.inputStyle()) {
                case PARAGRAPH -> TextInputStyle.PARAGRAPH;
                case SHORT -> TextInputStyle.SHORT;
            };

            TextInput.Builder input = TextInput.create(inputId, label, style);
            if(StringUtils.isNotBlank(placeholder)) input.setPlaceholder(placeholder);
            input.setRequired(inputDefinition.required());
            if(inputDefinition.maxLength() > 0) input.setMaxLength(inputDefinition.maxLength());
            if(inputDefinition.minLength() > 0) input.setMinLength(inputDefinition.minLength());
            if(StringUtils.isNotBlank(inputDefinition.defaultValue())) input.setValue(processVar(inputDefinition.defaultValue()));
            modal.addActionRow(input.build());
        }

        return modal.build();
    }

    /**
     * Processes the variables in the given string.
     *
     * @param old The original string with placeholders.
     * @return The processed string with placeholders replaced by variable values.
     * @since 1.0.0-alpha.7
     */
    private String processVar(String old) {
        return VariableProcessor.processVariable(locale, old, variables, template.getDefinition().defaultVars());
    }

}
