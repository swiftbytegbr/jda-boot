package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.annotation.interactions.button.ButtonByClass;
import de.swiftbyte.jdaboot.annotation.interactions.command.CommandOption;
import de.swiftbyte.jdaboot.annotation.interactions.command.SlashCommandDefinition;
import de.swiftbyte.jdaboot.embeds.AdvancedEmbed;
import de.swiftbyte.jdaboot.embeds.TemplateEmbed;
import de.swiftbyte.jdaboot.interactions.button.AdvancedButton;
import de.swiftbyte.jdaboot.interactions.button.TemplateButton;
import de.swiftbyte.jdaboot.interactions.command.SlashCommandExecutor;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.List;

@SlashCommandDefinition(
        name = "test",
        description = "-",
        type = SlashCommandDefinition.Type.SLASH,
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

    @ButtonByClass(TestButton.class)
    public static TemplateButton button;

    @Override
    public void onCommand() {

        AdvancedEmbed advancedEmbed = embed.generateAdvancedEmbed();
        advancedEmbed.setVariable("test", "Test");

        event.replyEmbeds(advancedEmbed.generateEmbed()).setActionRow(button.generateAdvancedButton().build()).queue();

    }

    @Override
    public void onAutoComplete(AutoCompleteQuery query, CommandAutoCompleteInteractionEvent event) {
        List<Command.Choice> choices = new ArrayList<>();
        choices.add(new Command.Choice("test", "test"));
        event.replyChoices(choices).queue();
    }
}
