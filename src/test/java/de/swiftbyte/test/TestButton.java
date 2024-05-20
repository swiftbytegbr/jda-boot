package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalByClass;
import de.swiftbyte.jdaboot.interaction.button.ButtonExecutor;
import de.swiftbyte.jdaboot.interaction.modal.TemplateModal;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@ButtonDefinition(
        label = "Test",
        id = "test_button",
        emoji = "\uD83D\uDC4D",
        type = ButtonDefinition.Type.PRIMARY
)
public class TestButton implements ButtonExecutor {

    @ModalByClass(TestModal.class)
    private TemplateModal modal;

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        event.replyModal(modal.advancedModal().setVariable("title", "Test").build()).queue();
    }

}