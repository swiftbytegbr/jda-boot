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

@Getter
public class AdvancedEmbed {

    private TemplateEmbed template;

    @Getter
    @Setter
    private DiscordLocale locale;

    private HashMap<String, String> variables = new HashMap<>();

    private AdvancedEmbed(TemplateEmbed template, HashMap<String, String> variables, DiscordLocale locale) {
        this.variables = variables;
        this.locale = locale;
    }

    protected AdvancedEmbed(TemplateEmbed template, DiscordLocale locale) {
        this.template = template;
        this.locale = locale;
    }

    public AdvancedEmbed setVariable(String variable, String value) {

        if (variable == null || value == null)
            throw new RuntimeException("Can not use null as variable key or value on variable " + variable);

        variables.put(variable, value);
        return this;
    }

    public MessageEmbed generateEmbed() {
        return generateEmbed(null);
    }

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

    private String processVar(String old) {
        return JDABoot.getInstance().getVariableProcessor().processVariable(locale, old, variables, template.getEmbed().defaultVars());
    }
}
