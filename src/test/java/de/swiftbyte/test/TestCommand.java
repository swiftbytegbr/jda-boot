package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.annotation.interactions.command.CommandOption;
import de.swiftbyte.jdaboot.annotation.interactions.command.SlashCommand;
import de.swiftbyte.jdaboot.embeds.TemplateEmbed;
import de.swiftbyte.jdaboot.interactions.commands.SlashCommandExecutor;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.List;

@SlashCommand(
        name = "test",
        description = "-",
        type = SlashCommand.Type.SLASH,
        options = {
                @CommandOption(
                        name = "test",
                        description = "test",
                        type = OptionType.STRING,
                        autoComplete = true,
                        required = true)
        }
)
public class TestCommand extends SlashCommandExecutor {

    @Embed(
            basedOn = "testEmbed",
            title = "${test}Test",
            description = "Version: ?{app.version}"
    )
    private TemplateEmbed embed;

    @Override
    public void onCommand() {

        event.replyEmbeds(embed.generateAdvancedEmbed().generateEmbed()).queue();

    }

    @Override
    public void onAutoComplete(AutoCompleteQuery query, CommandAutoCompleteInteractionEvent event) {
        List<Command.Choice> choices = new ArrayList<>();
        choices.add(new Command.Choice("test", "test"));
        event.replyChoices(choices).queue();
    }
}
