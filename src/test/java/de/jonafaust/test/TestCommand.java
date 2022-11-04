package de.jonafaust.test;

import de.jonafaust.jdaboot.annotation.command.Command;
import de.jonafaust.jdaboot.command.SlashCommand;
import de.jonafaust.jdaboot.command.info.SlashCommandInfo;
import de.jonafaust.jdaboot.embeds.AdvancedEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Command(
        name = "test",
        type = Command.Type.SLASH
)
public class TestCommand implements SlashCommand {

    /*@Override
    public void onCommand(SlashCommandInfo ctx) {

        AdvancedEmbed embed = EmbedClass.embed.generateAdvancedEmbed(DiscordLocale.ENGLISH_US);

        embed.setVariable("name", "Hufeisen");
        embed.setVariable("credits", "2");

        ctx.replyEmbeds(embed.generateEmbed()).queue();

    }*/

    @Override
    public void onCommand(SlashCommandInfo event) {

        AdvancedEmbed embed = EmbedClass.embed.generateAdvancedEmbed(DiscordLocale.ENGLISH_US);

        embed.setVariable("name", "Hufeisen");
        embed.setVariable("credits", "2");
        event.replyEmbeds(embed.generateEmbed()).queue();

    }
}
