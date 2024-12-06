package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.interaction.selection.EntitySelectMenuDefinition;
import de.swiftbyte.jdaboot.interaction.selection.EntitySelectMenuExecutor;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;

import java.util.HashMap;

@EntitySelectMenuDefinition(
        placeholder = "Placeholder",
        enableChannel = true
)
public class TestEntitySelectMenu implements EntitySelectMenuExecutor {

    @Override
    public void onSelectMenuSubmit(EntitySelectInteractionEvent event, HashMap<String, String> variables) {

        event.reply("It works!").queue();

    }
}
