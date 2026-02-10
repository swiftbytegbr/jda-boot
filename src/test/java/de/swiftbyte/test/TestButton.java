package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalByPath;
import de.swiftbyte.jdaboot.interaction.button.ButtonExecutor;
import de.swiftbyte.jdaboot.interaction.modal.TemplateModal;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@ButtonDefinition(
        label = "Test",
        id = "test_button",
        emoji = "\uD83D\uDC4D",
        type = ButtonDefinition.Type.PRIMARY
)
public class TestButton implements ButtonExecutor {

    @ModalByPath("test.xml")
    private TemplateModal modal;

    @Override
    public void onButtonClick(@NonNull ButtonInteractionEvent event, @NonNull Map<String, String> variables) {
        System.out.println(variables.get("test"));
        event.replyModal(modal.advancedModal().build()).queue();
    }

}
