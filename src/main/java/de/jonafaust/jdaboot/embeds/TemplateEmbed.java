package de.jonafaust.jdaboot.embeds;

import de.jonafaust.jdaboot.annotation.embed.Embed;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class TemplateEmbed {

    private final Embed embed;

    protected TemplateEmbed(Embed embed) {
        this.embed = embed;
    }

    protected Embed getEmbed() {
        return embed;
    }

    public AdvancedEmbed generateAdvancedEmbed(DiscordLocale locale) {
        return new AdvancedEmbed(this, locale);
    }

    public AdvancedEmbed generateAdvancedEmbed() {
        return new AdvancedEmbed(this, DiscordLocale.ENGLISH_US);
    }
}
