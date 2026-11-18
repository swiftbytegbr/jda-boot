package de.swiftbyte.jdaboot.manual;

import de.swiftbyte.jdaboot.annotation.EventHandler;
import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.embed.TemplateEmbed;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public class ManualEventHandlers {

    @Embed(
            title = "${test}Test",
            description = "Version: ?{app.version}"
    )
    private TemplateEmbed embed;

    @EventHandler
    public void onReady(GuildMemberJoinEvent event) {
        event.getGuild().getSystemChannel().sendMessageEmbeds(embed.advancedEmbed().build()).queue();
    }

}
