package de.swiftbyte.jdaboot.button;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface BotButton {

    void onButtonClick(ButtonInteractionEvent event);

}
