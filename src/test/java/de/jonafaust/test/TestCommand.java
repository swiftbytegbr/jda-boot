package de.jonafaust.test;

import de.swiftbyte.jdaboot.annotation.command.Command;
import de.swiftbyte.jdaboot.annotation.command.Subcommand;
import de.swiftbyte.jdaboot.command.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
    public void onCommand(SlashCommandInteractionEvent event) {

        if (event.getSubcommandName().equals("subcommand")) {
            event.replyEmbeds(EmbedClass.embed.generateAdvancedEmbed().generateEmbed()).queue();
        } else {
            event.replyEmbeds(EmbedClass.embed.generateAdvancedEmbed().generateEmbed()).queue();
        }

    }
}
