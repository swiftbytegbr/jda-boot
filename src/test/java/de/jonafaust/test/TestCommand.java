package de.jonafaust.test;

import de.jonafaust.jdaboot.annotation.command.Command;
import de.jonafaust.jdaboot.annotation.embed.Embed;
import de.jonafaust.jdaboot.annotation.embed.EmbedField;
import de.jonafaust.jdaboot.annotation.embed.EmbedFooter;
import de.jonafaust.jdaboot.button.ButtonManager;
import de.jonafaust.jdaboot.command.SlashCommand;
import de.jonafaust.jdaboot.command.info.SlashCommandInfo;
import de.jonafaust.jdaboot.embeds.AdvancedEmbed;
import de.jonafaust.jdaboot.embeds.TemplateEmbed;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Command(
        name = "setup",
        description = "Use this command to customise Neptun to your server",
        type = Command.Type.SLASH,
        enabledFor = Permission.ADMINISTRATOR,
        guildOnly = true
)
public class TestCommand implements SlashCommand {


    @Embed(
            title = "Setup",
            hexColor = "#8f62c3",
            thumbnailUrl = "https://neptun-bot.tk/Neptun.png",
            footer = @EmbedFooter(
                    text = TestClient.NEPTUNVERSION
            ),
            fields = {
                    @EmbedField(
                            title = "Coming soon"
                    ),
            }
    )
    private static TemplateEmbed reply;


    @Override
    public void onCommand(SlashCommandInfo event) {

        AdvancedEmbed embed = reply.generateAdvancedEmbed(DiscordLocale.ENGLISH_US);

        event.replyEmbeds(embed.generateEmbed()).addActionRow(ButtonManager.getButton(TestButton.class)).setEphemeral(true).queue();

    }
}
