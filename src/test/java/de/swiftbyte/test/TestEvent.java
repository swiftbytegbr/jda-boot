package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.EventHandler;
import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.embeds.TemplateEmbed;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

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
