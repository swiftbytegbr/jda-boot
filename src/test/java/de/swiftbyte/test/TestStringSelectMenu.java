package de.swiftbyte.test;

import de.swiftbyte.jdaboot.annotation.interaction.selection.StringSelectMenuDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.selection.StringSelectOption;
import de.swiftbyte.jdaboot.interaction.selection.StringSelectMenuExecutor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@Slf4j
@StringSelectMenuDefinition(
        options = {
                @StringSelectOption(label = "Test Option", value = "Test"),
                @StringSelectOption(label = "${test}", value = "${test}")
        },
        placeholder = "Placeholder",
        maxOptions = 2
)
public class TestStringSelectMenu extends StringSelectMenuExecutor {

    @Override
    public void onSelectMenuSubmit(@NonNull StringSelectInteractionEvent event, @NonNull Map<String, String> variables) {

        event.reply("Working! Selection[0]: " + event.getSelectedOptions().get(0).getValue()).queue();

    }
}
