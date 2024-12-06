package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.annotation.embed.EmbedAuthor;
import de.swiftbyte.jdaboot.annotation.embed.EmbedField;
import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonByClass;
import de.swiftbyte.jdaboot.annotation.interaction.command.CommandOption;
import de.swiftbyte.jdaboot.annotation.interaction.command.SlashCommandDefinition;
import de.swiftbyte.jdaboot.embed.AdvancedEmbed;
import de.swiftbyte.jdaboot.embed.TemplateEmbed;
import de.swiftbyte.jdaboot.interaction.button.TemplateButton;
import de.swiftbyte.jdaboot.interaction.command.SlashCommandExecutor;
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
            author = @EmbedAuthor(name = "${name}${test}"),
            fields = {
                    @EmbedField(description = "#{test2}#{nested}")
            },
            description = "Version: ?{app.version}?{app.test}"
    )
    private TemplateEmbed embed;

    @ButtonByClass(TestButton.class)
    public static TemplateButton button;

    @Override
    public void onCommand() {

        AdvancedEmbed advancedEmbed = embed.advancedEmbed();
        advancedEmbed.setVariable("test", "Test");
        advancedEmbed.setVariable("credits", "1");
        advancedEmbed.setVariable("test4", "test4");
        advancedEmbed.addDynamicField("Dynamisches Feld", "${test}", false);

        event.replyEmbeds(advancedEmbed.build()).setActionRow(button.advancedButton().setVariable("test", "Transferred Variable").setVariable("user", event.getUser().getName()).build()).queue();

    }

    @Override
    public void onAutoComplete(AutoCompleteQuery query, CommandAutoCompleteInteractionEvent event) {
        List<Command.Choice> choices = new ArrayList<>();
        choices.add(new Command.Choice("test", "test"));
        event.replyChoices(choices).queue();
    }
}
