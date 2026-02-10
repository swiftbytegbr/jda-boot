package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.embed.Embed;
import de.swiftbyte.jdaboot.annotation.embed.EmbedAuthor;
import de.swiftbyte.jdaboot.annotation.embed.EmbedField;
import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonByClass;
import de.swiftbyte.jdaboot.annotation.interaction.command.CommandOption;
import de.swiftbyte.jdaboot.annotation.interaction.command.SlashCommandDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.component.ComponentByPath;
import de.swiftbyte.jdaboot.annotation.interaction.selection.EntitySelectMenuByClass;
import de.swiftbyte.jdaboot.annotation.interaction.selection.StringSelectMenuByClass;
import de.swiftbyte.jdaboot.embed.TemplateEmbed;
import de.swiftbyte.jdaboot.interaction.button.TemplateButton;
import de.swiftbyte.jdaboot.interaction.command.SlashCommandExecutor;
import de.swiftbyte.jdaboot.interaction.component.v2.TemplateComponentV2;
import de.swiftbyte.jdaboot.interaction.selection.TemplateSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
            author = @EmbedAuthor(name = "${selfUsername}"),
            fields = {
                    @EmbedField(description = "#{test2}#{nested}")
            },
            description = "Version: ?{app.version}?{app.test}"
    )
    private TemplateEmbed embed;

    @ComponentByPath("test.xml")
    private TemplateComponentV2 component;

    @ButtonByClass(TestButton.class)
    public TemplateButton button;

    @StringSelectMenuByClass(TestStringSelectMenu.class)
    private TemplateSelectMenu menu;

    @EntitySelectMenuByClass(TestEntitySelectMenu.class)
    private TemplateSelectMenu menu2;

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {

        /*AdvancedEmbed advancedEmbed = embed.advancedEmbed();
        advancedEmbed.setVariable("test", "Test");
        advancedEmbed.setVariable("credits", "1");
        advancedEmbed.setVariable("test4", "test4");
        advancedEmbed.addDynamicField("Dynamisches Feld", "${test}", false);

        event.replyEmbeds(advancedEmbed.build())
                .addComponents(
                        ActionRow.of(button.advancedButton()
                                .setVariable("test", "Transferred Variable")
                                .setVariable("user", event.getUser().getName())
                                .build()),
                        ActionRow.of(menu.advancedSelectMenu()
                                .setVariable("test", "Test Variable")
                                .addDynamicOption("Dynamic Option", "Dynamic Option")
                                .build()),
                        ActionRow.of(menu2.advancedSelectMenu().build())
                ).queue();*/
        event.replyComponents(
                component.advancedComponent(event.getUserLocale())
                        .setVariable("imageUrl", "https://picsum.photos/200/300")
                        .build()
        ).useComponentsV2().setEphemeral(true).queue();

    }

    @Override
    public void onAutoComplete(AutoCompleteQuery query, CommandAutoCompleteInteractionEvent event) {
        List<Command.Choice> choices = new ArrayList<>();
        choices.add(new Command.Choice("test", "test"));
        event.replyChoices(choices).queue();
    }
}
