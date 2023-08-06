package de.jonafaust.test;

import de.swiftbyte.jdaboot.annotation.Button;
import de.swiftbyte.jdaboot.button.BotButton;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Button(
        label = "Test",
        emoji = "\uD83D\uDC4D",
        url = "https://hufeisen-games.de",
        type = Button.Type.LINK
)
public class TestButton implements BotButton {

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        System.out.println("Button clicked!");
    }

}