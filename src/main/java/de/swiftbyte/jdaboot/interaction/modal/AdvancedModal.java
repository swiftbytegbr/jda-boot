package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalRow;
import de.swiftbyte.jdaboot.utils.StringUtils;
import de.swiftbyte.jdaboot.variables.VariableProcessor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.modals.Modal;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * The AdvancedModal class is responsible for generating advanced modals based on a provided TemplateModal.
 * It allows setting variables that can be used in the modal and provides methods to generate the final modal.
 *
 * @since 1.0.0-alpha.7
 */
public class AdvancedModal {

    private @NonNull TemplateModal template;

    @Getter
    @Setter
    private @NonNull DiscordLocale locale;

    private @NonNull HashMap<@NonNull String, @NonNull String> variables = new HashMap<>();
    private @NonNull List<@NonNull DynamicModalRow> dynamicRows = new ArrayList<>();
    private static @NonNull HashMap<@NonNull String, @NonNull HashMap<@NonNull String, @NonNull String>> variableTransfer = new HashMap<>();

    /**
     * Constructor for AdvancedModal. Initializes the modal with the specified template, variables, and locale.
     *
     * @param template The TemplateModal to use as a base for the advanced button.
     * @param locale   The locale to use for the modal.
     * @since 1.0.0-alpha.7
     */
    protected AdvancedModal(@NonNull TemplateModal template, @NonNull DiscordLocale locale) {
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
    public @NonNull AdvancedModal setVariable(@NonNull String key, @NonNull String value) {
        variables.put(key, value);
        return this;
    }

    /**
     * Add a row to the modal at runtime.
     *
     * @param dynamicModalRow The row to add.
     * @return The AdvancedModal instance for chaining.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedModal addDynamicRow(@NonNull DynamicModalRow dynamicModalRow) {
        dynamicRows.add(dynamicModalRow);
        return this;
    }

    /**
     * Add a row to the modal at runtime.
     *
     * @param id           The ID of the row.
     * @param label        The label of the row.
     * @param style        The style of the row.
     * @param placeholder  The placeholder of the row.
     * @param required     Whether the row is required.
     * @param maxLength    The maximum length of the row.
     * @param minLength    The minimum length of the row.
     * @param defaultValue The default value of the row.
     * @return The AdvancedModal instance for chaining.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedModal addDynamicModalRow(@NonNull String id, @NonNull String label, @NonNull TextInputStyle style, @NonNull String placeholder, boolean required, int maxLength, int minLength, @NonNull String defaultValue) {
        dynamicRows.add(new DynamicModalRow(id, label, style, placeholder, required, maxLength, minLength, defaultValue));
        return this;
    }

    /**
     * Add multiple rows to the modal at runtime.
     *
     * @param dynamicModalRow The rows to add.
     * @return The AdvancedModal instance for chaining.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedModal addDynamicRows(@NonNull DynamicModalRow @NonNull ... dynamicModalRow) {
        dynamicRows.addAll(List.of(dynamicModalRow));
        return this;
    }

    /**
     * Add multiple rows to the modal at runtime.
     *
     * @param dynamicModalRow The rows to add.
     * @return The AdvancedModal instance for chaining.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedModal addDynamicRows(@NonNull Collection<@NonNull DynamicModalRow> dynamicModalRow) {
        dynamicRows.addAll(dynamicModalRow);
        return this;
    }

    /**
     * Generates a Modal based on the template and the set variables.
     *
     * @return The generated Modal.
     * @since 1.0.0-alpha.7
     */
    public @NonNull Modal build() {

        String variableId = UUID.randomUUID().toString();
        variableTransfer.put(variableId, variables);

        String id = template.getId() + ";" + variableId;
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

            TextInput.Builder input = TextInput.create(inputId, style);
            if (StringUtils.isNotBlank(placeholder)) {
                input.setPlaceholder(placeholder);
            }
            input.setRequired(inputDefinition.required());
            if (inputDefinition.maxLength() > 0) {
                input.setMaxLength(inputDefinition.maxLength());
            }
            if (inputDefinition.minLength() > 0) {
                input.setMinLength(inputDefinition.minLength());
            }
            if (StringUtils.isNotBlank(inputDefinition.defaultValue())) {
                input.setValue(processVar(inputDefinition.defaultValue()));
            }
            modal.addComponents(Label.of(label, input.build()));
        }

        for (DynamicModalRow inputDefinition : dynamicRows) {
            String inputId = processVar(inputDefinition.id());
            String placeholder = processVar(inputDefinition.placeholder());
            String label = processVar(inputDefinition.label());
            TextInputStyle style = switch (inputDefinition.style) {
                case PARAGRAPH -> TextInputStyle.PARAGRAPH;
                case UNKNOWN -> TextInputStyle.UNKNOWN;
                case SHORT -> TextInputStyle.SHORT;
            };

            TextInput.Builder input = TextInput.create(inputId, style);
            if (StringUtils.isNotBlank(placeholder)) {
                input.setPlaceholder(placeholder);
            }
            input.setRequired(inputDefinition.required());
            if (inputDefinition.maxLength() > 0) {
                input.setMaxLength(inputDefinition.maxLength());
            }
            if (inputDefinition.minLength() > 0) {
                input.setMinLength(inputDefinition.minLength());
            }
            if (StringUtils.isNotBlank(inputDefinition.defaultValue())) {
                input.setValue(processVar(inputDefinition.defaultValue()));
            }
            modal.addComponents(Label.of(label, input.build()));
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
    private @NonNull String processVar(@NonNull String old) {
        return VariableProcessor.processVariable(locale, old, variables, template.getDefinition().defaultVars());
    }

    /**
     * The DynamicModalRow class is used to store dynamic rows that are added at runtime.
     *
     * @since 1.0.0-alpha.7
     */
    @Accessors(fluent = true)
    @AllArgsConstructor
    @Data
    public static class DynamicModalRow {

        private @NonNull String id;
        private @NonNull String label;
        private @NonNull TextInputStyle style;
        private @NonNull String placeholder = "";
        private boolean required;
        private int maxLength;
        private int minLength;
        private @NonNull String defaultValue = "";

        public DynamicModalRow(@NonNull String id, @NonNull String label, @NonNull TextInputStyle style) {
            this.id = id;
            this.label = label;
            this.style = style;
        }
    }

    /**
     * Get the variables from the given ID.
     *
     * @param id The ID to get the variables from.
     * @return The variables from the given ID.
     * @since 1.0.0-alpha.9
     */
    public static @NonNull HashMap<@NonNull String, @NonNull String> getVariablesFromId(@NonNull String id) {
        return variableTransfer.get(id);
    }
}
