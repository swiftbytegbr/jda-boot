package de.jonafaust.test;

import de.swiftbyte.jdaboot.annotation.command.Command;
import de.swiftbyte.jdaboot.annotation.command.Subcommand;
import de.swiftbyte.jdaboot.command.SlashCommand;
import de.swiftbyte.jdaboot.command.info.SlashCommandInfo;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Command(
        name = "test",
        description = "-",
        type = Command.Type.SLASH,
        subcommands = {
                @Subcommand(
                        name = "subcommand",
                        description = "-"
                )
        }
)
public class TestCommand implements SlashCommand {
    @Override
    public void onCommand(SlashCommandInfo event) {

        if (event.getSubcommandName().equals("subcommand")) {
            event.replyEmbeds(EmbedClass.embed.generateAdvancedEmbed().generateEmbed()).queue();
        } else {
            event.replyEmbeds(EmbedClass.embed.generateAdvancedEmbed().generateEmbed()).queue();
        }

    }
}
