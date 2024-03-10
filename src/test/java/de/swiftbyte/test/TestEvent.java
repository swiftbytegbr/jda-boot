package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.DefaultVariable;
import de.swiftbyte.jdaboot.annotation.EventHandler;
import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.annotation.embed.EmbedAuthor;
import de.swiftbyte.jdaboot.annotation.embed.EmbedField;
import de.swiftbyte.jdaboot.annotation.embed.EmbedFooter;
import de.swiftbyte.jdaboot.embeds.EmbedColor;
import de.swiftbyte.jdaboot.embeds.TemplateEmbed;
import de.swiftbyte.jdaboot.variables.TranslationProcessor;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class TestEvent {

    @Embed(
            title = "${test}Test",
            description = "Version: ?{app.version}"
    )
    private TemplateEmbed embed;

    @EventHandler
    public void onReady(GuildMemberJoinEvent event) {
        event.getGuild().getSystemChannel().sendMessageEmbeds(embed.generateAdvancedEmbed().generateEmbed()).queue();
    }

}
