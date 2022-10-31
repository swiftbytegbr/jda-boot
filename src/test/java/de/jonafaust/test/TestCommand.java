package de.jonafaust.test;

import de.jonafaust.jdaboot.annotation.Command;
import de.jonafaust.jdaboot.command.CommandContext;
import de.jonafaust.jdaboot.command.SimpleCommand;
import de.jonafaust.jdaboot.embeds.AdvancedEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Command(name = "test", description = "Test command")
public class TestCommand implements SimpleCommand {

    @Override
    public void onCommand(CommandContext ctx) {

        AdvancedEmbed embed = EmbedClass.embed.generateAdvancedEmbed(DiscordLocale.GERMAN);

        ctx.replyEmbeds(embed.generateEmbed()).queue();
    }
}
