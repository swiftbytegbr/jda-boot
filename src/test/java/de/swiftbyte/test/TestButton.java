package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.interactions.button.ButtonDefinition;
import de.swiftbyte.jdaboot.interactions.button.ButtonExecutor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@ButtonDefinition(
        label = "${name}",
        id = "test_button",
        emoji = "\uD83D\uDC4D",
        type = ButtonDefinition.Type.PRIMARY
)
public class TestButton implements ButtonExecutor {

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        event.reply("Button clicked!").queue();
    }

}