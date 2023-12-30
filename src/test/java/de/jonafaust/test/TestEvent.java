package de.jonafaust.test;

import de.swiftbyte.jdaboot.annotation.EventHandler;
import de.swiftbyte.jdaboot.variables.TranslationProcessor;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class TestEvent {

    @EventHandler
    public void onReady(GuildMemberJoinEvent event) {
        System.out.println(TranslationProcessor.getTranslatedString(DiscordLocale.GERMAN, "test"));
    }

}
