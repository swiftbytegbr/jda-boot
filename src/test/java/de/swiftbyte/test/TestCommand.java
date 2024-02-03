package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.interactions.command.Command;
import de.swiftbyte.jdaboot.annotation.interactions.command.Subcommand;
import de.swiftbyte.jdaboot.interactions.commands.SlashCommandExecutor;
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
public class TestCommand implements SlashCommandExecutor {

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {

        if (event.getSubcommandName().equals("subcommand")) {
            event.replyEmbeds(EmbedClass.embed.generateAdvancedEmbed().generateEmbed()).queue();
        } else {
            event.replyEmbeds(EmbedClass.embed.generateAdvancedEmbed().generateEmbed()).queue();
        }

    }
}
