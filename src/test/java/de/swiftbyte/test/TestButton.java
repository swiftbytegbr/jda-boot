package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalByClass;
import de.swiftbyte.jdaboot.interaction.button.ButtonExecutor;
import de.swiftbyte.jdaboot.interaction.modal.AdvancedModal;
import de.swiftbyte.jdaboot.interaction.modal.TemplateModal;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.HashMap;

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
    public void onButtonClick(ButtonInteractionEvent event, HashMap<String, String> variables) {
        System.out.println(variables.get("test"));
        event.replyModal(modal.advancedModal().setVariable("title", variables.get("test")).setVariable("user", variables.get("user")).addDynamicRow(new AdvancedModal.DynamicModalRow("dynamic_test", "Dynamic Test", TextInputStyle.SHORT)).build()).queue();
    }

}