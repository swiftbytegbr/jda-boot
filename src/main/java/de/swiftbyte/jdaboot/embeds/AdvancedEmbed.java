package de.swiftbyte.jdaboot.embeds;

import de.swiftbyte.jdaboot.JDABoot;
import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.annotation.embed.EmbedField;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;

/**
 * The AdvancedEmbed class is responsible for generating advanced embeds based on a provided TemplateEmbed.
 * It allows setting variables that can be used in the embed and provides methods to generate the final embed.
 *
 * @since alpha.4
 */
@Getter
public class AdvancedEmbed {

    private TemplateEmbed template;

    @Getter
    @Setter
    private DiscordLocale locale;

    private HashMap<String, String> variables = new HashMap<>();

    /**
     * Constructor for AdvancedEmbed. Initializes the embed with the specified template, variables, and locale.
     *
     * @param template The TemplateEmbed to use as a base for the advanced embed.
     * @param variables The variables to use in the embed.
     * @param locale The locale to use for the embed.
     * @since alpha.4
     */
    private AdvancedEmbed(TemplateEmbed template, HashMap<String, String> variables, DiscordLocale locale) {
        this.template = template;
        this.variables = variables;
        this.locale = locale;
    }

    /**
     * Constructor for AdvancedEmbed. Initializes the embed with the specified template and locale.
     *
     * @param template The TemplateEmbed to use as a base for the advanced embed.
     * @param locale The locale to use for the embed.
     * @since alpha.4
     */
    protected AdvancedEmbed(TemplateEmbed template, DiscordLocale locale) {
        this.template = template;
        this.locale = locale;
    }

    /**
     * Sets a variable to be used in the embed.
     *
     * @param variable The name of the variable.
     * @param value The value of the variable.
     * @return The AdvancedEmbed instance for chaining.
     * @throws NullPointerException If the variable key or value is null.
     * @since alpha.4
     */
    public AdvancedEmbed setVariable(String variable, String value) {

        if (variable == null || value == null)
            throw new NullPointerException("Can not use null as variable key or value on variable " + variable);

        variables.put(variable, value);
        return this;
    }

    /**
     * Generates a MessageEmbed based on the template and the set variables.
     *
     * @return The generated MessageEmbed.
     * @since alpha.4
     */
    public MessageEmbed generateEmbed() {
        return generateEmbed(null);
    }

    /**
     * Generates a MessageEmbed based on the template and the set variables, with the specified timestamp.
     *
     * @param timestamp The timestamp to set in the embed.
     * @return The generated MessageEmbed.
     * @since alpha.4
     */
    public MessageEmbed generateEmbed(Instant timestamp) {

        EmbedBuilder builder = new EmbedBuilder();
        Embed embed = template.getEmbed();

        if (!embed.title().isEmpty())
            builder.setTitle(processVar(embed.title()), !embed.url().isEmpty() ? processVar(embed.url()) : null);
        if (!embed.thumbnailUrl().isEmpty()) builder.setThumbnail(processVar(embed.thumbnailUrl()));
        if (!embed.imageUrl().isEmpty()) builder.setImage(processVar(embed.imageUrl()));

        builder
                .setDescription(processVar(embed.description()))
                .setColor(!embed.hexColor().isEmpty() ? Color.decode(embed.hexColor()) : embed.color().getColor())
                .setAuthor(processVar(embed.author().name()), !embed.author().url().isEmpty() ? processVar(embed.author().url()) : null, !embed.author().iconUrl().isEmpty() ? processVar(embed.author().iconUrl()) : null)
                .setFooter(processVar(embed.footer().text()), !embed.footer().iconUrl().isEmpty() ? processVar(embed.footer().iconUrl()) : null);

        for (EmbedField embedField : embed.fields()) {

            builder.addField(processVar(embedField.title()), processVar(embedField.description()), embedField.inline());

        }

        if (timestamp != null) builder.setTimestamp(timestamp);

        return builder.build();
    }

    /**
     * Processes the variables in the given string.
     *
     * @param old The original string with placeholders.
     * @return The processed string with placeholders replaced by variable values.
     * @since alpha.4
     */
    private String processVar(String old) {
        return JDABoot.getInstance().getVariableProcessor().processVariable(locale, old, variables, template.getEmbed().defaultVars());
    }
}
