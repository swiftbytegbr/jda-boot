package de.swiftbyte.jdaboot.embed;

import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.annotation.embed.EmbedField;
import de.swiftbyte.jdaboot.exceptions.ElementNotFoundException;
import de.swiftbyte.jdaboot.utils.StringUtils;
import de.swiftbyte.jdaboot.variables.VariableProcessor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AdvancedEmbed class is responsible for generating advanced embeds based on a provided TemplateEmbed.
 * It allows setting variables that can be used in the embed and provides methods to generate the final embed.
 *
 * @since alpha.4
 */
@Getter
@Slf4j
public class AdvancedEmbed {

    private @NonNull TemplateEmbed template;

    @Getter
    @Setter
    private @NonNull DiscordLocale locale;

    private @NonNull HashMap<@NonNull String, @NonNull String> variables = new HashMap<>();
    private @NonNull List<@NonNull DynamicEmbedField> dynamicFields = new ArrayList<>();

    /**
     * Constructor for AdvancedEmbed. Initializes the embed with the specified template, variables, and locale.
     *
     * @param template  The TemplateEmbed to use as a base for the advanced embed.
     * @param variables The variables to use in the embed.
     * @param locale    The locale to use for the embed.
     * @since alpha.4
     */
    private AdvancedEmbed(@NonNull TemplateEmbed template, @NonNull HashMap<@NonNull String, @NonNull String> variables, @NonNull DiscordLocale locale) {
        this.template = template;
        this.variables = variables;
        this.locale = locale;
    }

    /**
     * Constructor for AdvancedEmbed. Initializes the embed with the specified template and locale.
     *
     * @param template The TemplateEmbed to use as a base for the advanced embed.
     * @param locale   The locale to use for the embed.
     * @since alpha.4
     */
    protected AdvancedEmbed(@NonNull TemplateEmbed template, @NonNull DiscordLocale locale) {
        this.template = template;
        this.locale = locale;
    }

    /**
     * Sets a variable to be used in the embed.
     *
     * @param variable The name of the variable.
     * @param value    The value of the variable.
     * @return The AdvancedEmbed instance for chaining.
     * @throws NullPointerException If the variable key or value is null.
     * @since alpha.4
     */
    public @NonNull AdvancedEmbed setVariable(@NonNull String variable, @NonNull String value) {

        if (variable == null || value == null) {
            throw new NullPointerException("Can not use null as variable key or value on variable " + variable);
        }

        variables.put(variable, value);
        return this;
    }

    /**
     * Appends multiple variables to be used in the embed.
     *
     * @param variables The variables to append.
     * @return The AdvancedEmbed instance for chaining.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedEmbed setVariables(@NonNull Map<@NonNull String, @NonNull String> variables) {
        this.variables.putAll(variables);
        return this;
    }

    /**
     * Add a field to the embed at runtime. The field also supports variables.
     *
     * @param title       The title of the field.
     * @param description The description of the field.
     * @param inline      Whether the field should be inline.
     * @return The AdvancedEmbed instance for chaining.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedEmbed addDynamicField(@NonNull String title, @NonNull String description, boolean inline) {
        dynamicFields.add(new DynamicEmbedField(title, description, inline));
        return this;
    }

    /**
     * Add a field to the embed at runtime. The field also supports variables.
     *
     * @param dynamicField The field to add.
     * @return The AdvancedEmbed instance for chaining.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedEmbed addDynamicField(@NonNull DynamicEmbedField dynamicField) {
        dynamicFields.add(dynamicField);
        return this;
    }

    /**
     * Add fields to the embed at runtime.
     *
     * @param dynamicFields The fields to add.
     * @return The AdvancedEmbed instance for chaining.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedEmbed addDynamicFields(@NonNull DynamicEmbedField @NonNull ... dynamicFields) {
        this.dynamicFields.addAll(List.of(dynamicFields));
        return this;
    }

    /**
     * Add fields to the embed at runtime.
     *
     * @param dynamicFields The fields to add.
     * @return The AdvancedEmbed instance for chaining.
     * @since 1.0.0-alpha.7
     */
    public @NonNull AdvancedEmbed addDynamicFields(@NonNull List<@NonNull DynamicEmbedField> dynamicFields) {
        this.dynamicFields.addAll(dynamicFields);
        return this;
    }


    /**
     * Generates a MessageEmbed based on the template and the set variables.
     *
     * @return The generated MessageEmbed.
     * @since alpha.4
     */
    public @NonNull MessageEmbed build() {
        return build(null);
    }

    /**
     * Generates a MessageEmbed based on the template and the set variables, with the specified timestamp.
     *
     * @param timestamp The timestamp to set in the embed.
     * @return The generated MessageEmbed.
     * @since alpha.4
     */
    public @NonNull MessageEmbed build(@Nullable Instant timestamp) {

        return generateEmbedBuilder(timestamp).build();
    }

    /**
     * Generates a EmbedBuilder based on the template and the set variables, with the specified timestamp.
     *
     * @param timestamp The timestamp to set in the embed.
     * @return The generated EmbedBuilder.
     * @since 1.0.0-beta.1
     */
    private @NonNull EmbedBuilder generateEmbedBuilder(@Nullable Instant timestamp) {
        EmbedBuilder builder = new EmbedBuilder();
        Embed embed = template.getEmbed();

        if (StringUtils.isNotBlank(embed.basedOn())) {
            TemplateEmbed basedOn = EmbedManager.getTemplateEmbed(embed.basedOn());
            if (basedOn != null) {
                builder.copyFrom(basedOn.advancedEmbed(locale).setVariables(variables).generateEmbedBuilder(timestamp));
            } else {
                throw new ElementNotFoundException(String.format("Could not base embed on embed with ID %s because it does not exist", embed.basedOn()), embed.id());
            }
        }

        if (StringUtils.isNotBlank(embed.title())) {
            String title = processVar(embed.title());
            String url = processVar(embed.url());
            if (StringUtils.isNotBlank(title)) {
                builder.setTitle(title, StringUtils.isNotBlank(url) ? url : null);
            }
        }

        if (StringUtils.isNotBlank(embed.thumbnailUrl())) {
            String thumbnailUrl = processVar(embed.thumbnailUrl());
            String thumbnailDescription = processVar(embed.thumbnailDescription());
            if (StringUtils.isNotBlank(thumbnailUrl)) {
                builder.setThumbnail(thumbnailUrl, thumbnailDescription);
            }
        }

        if (StringUtils.isNotBlank(embed.imageUrl())) {
            String imageUrl = processVar(embed.imageUrl());
            String imageDescription = processVar(embed.imageDescription());
            if (StringUtils.isNotBlank(imageUrl)) {
                builder.setImage(imageUrl, imageDescription);
            }
        }

        if (StringUtils.isNotBlank(embed.description())) {
            String description = processVar(embed.description());
            if (StringUtils.isNotBlank(description)) {
                builder.setDescription(description);
            }
        }

        String hexColor = processVar(embed.hexColor());
        if (StringUtils.isNotBlank(hexColor) || embed.color().getColor() != null) {
            builder.setColor(StringUtils.isNotBlank(hexColor) ? Color.decode(hexColor) : embed.color().getColor());
        }

        if (StringUtils.isNotBlank(embed.author().name())) {
            String authorName = processVar(embed.author().name());
            String authorUrl = processVar(embed.author().url());
            String authorIconUrl = processVar(embed.author().iconUrl());
            if (StringUtils.isNotBlank(authorName)) {
                builder.setAuthor(authorName, StringUtils.isNotBlank(authorUrl) ? authorUrl : null, StringUtils.isNotBlank(authorIconUrl) ? authorIconUrl : null);
            }
        }

        if (StringUtils.isNotBlank(embed.footer().text())) {
            String footerText = processVar(embed.footer().text());
            String footerIconUrl = processVar(embed.footer().iconUrl());
            if (StringUtils.isNotBlank(footerText)) {
                builder.setFooter(footerText, StringUtils.isNotBlank(footerIconUrl) ? footerIconUrl : null);
            }
        }

        for (EmbedField embedField : embed.fields()) {
            builder.addField(processVar(embedField.title()), processVar(embedField.description()), embedField.inline());
        }

        for (DynamicEmbedField dynamicField : dynamicFields) {
            builder.addField(processVar(dynamicField.title()), processVar(dynamicField.description()), dynamicField.inline());
        }

        if (timestamp != null) {
            builder.setTimestamp(timestamp);
        }

        return builder;
    }

    /**
     * Processes the variables in the given string.
     *
     * @param old The original string with placeholders.
     * @return The processed string with placeholders replaced by variable values.
     * @since alpha.4
     */
    private @NonNull String processVar(@NonNull String old) {
        return VariableProcessor.processVariable(locale, old, variables, template.getEmbed().defaultVars());
    }

    /**
     * Used to store dynamic fields that are added at runtime.
     *
     * @since 1.0.0-alpha.7
     */
    public record DynamicEmbedField(@NonNull String title, @NonNull String description, boolean inline) {

    }
}
