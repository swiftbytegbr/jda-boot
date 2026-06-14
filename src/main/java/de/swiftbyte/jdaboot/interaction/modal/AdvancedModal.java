package de.swiftbyte.jdaboot.interaction.modal;

import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalRow;
import de.swiftbyte.jdaboot.exceptions.ElementBuildException;
import de.swiftbyte.jdaboot.interaction.component.v2.model.XmlDefaultVariable;
import de.swiftbyte.jdaboot.interaction.modal.model.XmlModalLayoutDefinition;
import de.swiftbyte.jdaboot.interaction.modal.model.XmlModalNodes;
import de.swiftbyte.jdaboot.utils.StringUtils;
import de.swiftbyte.jdaboot.variables.VariableProcessor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import net.dv8tion.jda.api.components.checkbox.Checkbox;
import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroup;
import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroupOption;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.api.components.radiogroup.RadioGroup;
import net.dv8tion.jda.api.components.radiogroup.RadioGroupOption;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.modals.Modal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final @NonNull ConcurrentHashMap<@NonNull String, @NonNull Map<@NonNull String, @NonNull String>> variableTransfer = new ConcurrentHashMap<>();

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
     * Appends multiple variables to be used in the modal.
     *
     * @param variables The variables to append.
     * @return The AdvancedModal instance for chaining.
     * @since 1.0.0-beta.2
     */
    public @NonNull AdvancedModal setVariables(@NonNull Map<@NonNull String, @NonNull String> variables) {
        this.variables.putAll(variables);
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
        try {
            String variableId = UUID.randomUUID().toString();
            variableTransfer.put(variableId, Map.copyOf(getTransferredVariables()));

            String id = template.getId() + ";" + variableId;
            String title = processVar(getTitle());

            Modal.Builder modal = Modal.create(id, title);

            if (template.getDefinition() != null) {
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
            } else if (template.getXmlDefinition() != null) {
                for (XmlModalNodes.LabelNode labelNode : template.getXmlDefinition().labels()) {
                    String label = processVar(labelNode.text());
                    String description = processVar(labelNode.description());
                    LabelChildComponent child = buildLabelChild(labelNode.child());

                    if (StringUtils.isNotBlank(description)) {
                        modal.addComponents(Label.of(label, description, child));
                    } else {
                        modal.addComponents(Label.of(label, child));
                    }
                }
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
        } catch (Exception e) {
            XmlModalLayoutDefinition xmlDefinition = template.getXmlDefinition();
            if (xmlDefinition != null) {
                throw new ElementBuildException("Failed to build modal", xmlDefinition.sourcePath() + ", layout: " + xmlDefinition.id(), e);
            }
            throw new ElementBuildException("Failed to build modal", e);
        }
    }

    private @NonNull LabelChildComponent buildLabelChild(XmlModalNodes.LabelChildNode childNode) {
        if (childNode instanceof XmlModalNodes.StringInputNode inputNode) {
            TextInput.Builder input = TextInput.create(processVar(inputNode.id()), inputNode.style());
            if (StringUtils.isNotBlank(inputNode.placeholder())) {
                input.setPlaceholder(processVar(inputNode.placeholder()));
            }
            input.setRequired(inputNode.required());
            if (inputNode.maxLength() > 0) {
                input.setMaxLength(inputNode.maxLength());
            }
            if (inputNode.minLength() > 0) {
                input.setMinLength(inputNode.minLength());
            }
            if (StringUtils.isNotBlank(inputNode.defaultValue())) {
                input.setValue(processVar(inputNode.defaultValue()));
            }
            return input.build();
        }

        if (childNode instanceof XmlModalNodes.FileInputNode inputNode) {
            AttachmentUpload.Builder input = AttachmentUpload.create(processVar(inputNode.id()));
            input.setRequired(inputNode.required());
            input.setRequiredRange(inputNode.minValues(), inputNode.maxValues());
            return input.build();
        }

        if (childNode instanceof XmlModalNodes.StringSelectNode inputNode) {
            StringSelectMenu.Builder input = StringSelectMenu.create(processVar(inputNode.id()));
            if (StringUtils.isNotBlank(inputNode.placeholder())) {
                input.setPlaceholder(processVar(inputNode.placeholder()));
            }

            List<SelectOption> options = new ArrayList<>();
            for (XmlModalNodes.StringSelectOptionNode optionNode : inputNode.options()) {
                SelectOption option = SelectOption.of(
                        processVar(optionNode.label()),
                        processVar(optionNode.value())
                );
                if (StringUtils.isNotBlank(optionNode.description())) {
                    option = option.withDescription(processVar(optionNode.description()));
                }
                if (optionNode.defaultOption()) {
                    option = option.withDefault(true);
                }
                options.add(option);
            }

            input.addOptions(options);
            input.setRequired(inputNode.required());
            input.setRequiredRange(inputNode.minValues(), inputNode.maxValues());
            input.setDisabled(inputNode.disabled());
            return input.build();
        }

        if (childNode instanceof XmlModalNodes.EntitySelectNode inputNode) {
            EntitySelectMenu.Builder input = EntitySelectMenu.create(processVar(inputNode.id()), inputNode.targets());
            if (StringUtils.isNotBlank(inputNode.placeholder())) {
                input.setPlaceholder(processVar(inputNode.placeholder()));
            }
            if (!inputNode.channelTypes().isEmpty()) {
                input.setChannelTypes(inputNode.channelTypes());
            }

            input.setRequired(inputNode.required());
            input.setRequiredRange(inputNode.minValues(), inputNode.maxValues());
            input.setDisabled(inputNode.disabled());
            return input.build();
        }

        if (childNode instanceof XmlModalNodes.CheckboxNode inputNode) {
            return Checkbox.of(processVar(inputNode.id()), inputNode.defaultValue());
        }

        if (childNode instanceof XmlModalNodes.CheckboxGroupNode inputNode) {
            CheckboxGroup.Builder input = CheckboxGroup.create(processVar(inputNode.id()));
            for (XmlModalNodes.GroupOptionNode optionNode : inputNode.options()) {
                input.addOptions(CheckboxGroupOption.of(
                        processVar(optionNode.label()),
                        processVar(optionNode.value()),
                        optionalProcessedValue(optionNode.description()),
                        optionNode.defaultOption()
                ));
            }
            input.setRequired(inputNode.required());
            if (inputNode.minValues() >= 0) {
                input.setMinValues(inputNode.minValues());
            }
            if (inputNode.maxValues() >= 0) {
                input.setMaxValues(inputNode.maxValues());
            }
            return input.build();
        }

        if (childNode instanceof XmlModalNodes.RadioGroupNode inputNode) {
            RadioGroup.Builder input = RadioGroup.create(processVar(inputNode.id()));
            for (XmlModalNodes.GroupOptionNode optionNode : inputNode.options()) {
                input.addOptions(RadioGroupOption.of(
                        processVar(optionNode.label()),
                        processVar(optionNode.value()),
                        optionalProcessedValue(optionNode.description()),
                        optionNode.defaultOption()
                ));
            }
            input.setRequired(inputNode.required());
            return input.build();
        }

        throw new IllegalStateException("Unsupported XML modal label child: " + childNode.getClass().getName());
    }

    private @Nullable String optionalProcessedValue(@NonNull String value) {
        return StringUtils.isNotBlank(value) ? processVar(value) : null;
    }

    /**
     * Processes the variables in the given string.
     *
     * @param old The original string with placeholders.
     * @return The processed string with placeholders replaced by variable values.
     * @since 1.0.0-alpha.7
     */
    private @NonNull String processVar(@NonNull String old) {
        if (template.getDefinition() != null) {
            return VariableProcessor.processVariable(locale, old, variables, template.getDefinition().defaultVars());
        }

        XmlModalLayoutDefinition xmlDefinition = template.getXmlDefinition();
        if (xmlDefinition != null) {
            return VariableProcessor.processVariable(locale, old, variables, xmlDefinition.defaultVars());
        }

        return old;
    }

    private @NonNull String getTitle() {
        if (template.getDefinition() != null) {
            return template.getDefinition().title();
        }

        XmlModalLayoutDefinition xmlDefinition = template.getXmlDefinition();
        if (xmlDefinition != null) {
            return xmlDefinition.title();
        }

        return "";
    }

    private @NonNull HashMap<@NonNull String, @NonNull String> getTransferredVariables() {
        HashMap<String, String> transferred = new HashMap<>();

        if (template.getDefinition() != null) {
            for (var defaultVar : template.getDefinition().defaultVars()) {
                transferred.put(defaultVar.variable(), defaultVar.value());
            }
        }

        XmlModalLayoutDefinition xmlDefinition = template.getXmlDefinition();
        if (xmlDefinition != null) {
            for (XmlDefaultVariable defaultVar : xmlDefinition.defaultVars()) {
                transferred.put(defaultVar.key(), defaultVar.value());
            }
        }

        transferred.putAll(variables);
        return transferred;
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
    public static @Nullable Map<@NonNull String, @NonNull String> getVariablesFromId(@NonNull String id) {
        Map<String, String> storedVariables = variableTransfer.get(id);
        if (storedVariables == null) {
            return null;
        }
        return new HashMap<>(storedVariables);
    }
}
