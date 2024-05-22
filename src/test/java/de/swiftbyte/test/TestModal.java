package de.swiftbyte.test;


import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalRow;
import de.swiftbyte.jdaboot.interaction.modal.ModalExecutor;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

@ModalDefinition(
        title = "${title} Modal",
        rows = {
                @ModalRow(
                        id = "test",
                        label = "Test",
                        placeholder = "This is a test modal.",
                        inputStyle = ModalRow.InputStyle.SHORT,
                        required = true
                ),
                @ModalRow(
                        id = "test2",
                        label = "Test2",
                        placeholder = "This is a test modal 2.",
                        inputStyle = ModalRow.InputStyle.PARAGRAPH,
                        minLength = 5,
                        maxLength = 10
                ),
                @ModalRow(
                        id = "test3",
                        label = "Test3",
                        placeholder = "This is a test modal 3.",
                        inputStyle = ModalRow.InputStyle.SHORT,
                        required = true,
                        defaultValue = "TestDefault"
                )
        }
)
public class TestModal implements ModalExecutor {

    @Override
    public void onModalSubmit(ModalInteractionEvent event) {
        event.reply("Test Modal Submitted!").setEphemeral(true).queue();
    }
}
