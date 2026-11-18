package de.swiftbyte.jdaboot.manual;

import de.swiftbyte.jdaboot.annotation.interaction.selection.EntitySelectMenuDefinition;
import de.swiftbyte.jdaboot.interaction.selection.EntitySelectMenuExecutor;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@EntitySelectMenuDefinition(
        placeholder = "Placeholder",
        enableChannel = true
)
public class ManualEntitySelectMenu extends EntitySelectMenuExecutor {

    @Override
    public void onSelectMenuSubmit(@NonNull EntitySelectInteractionEvent event, @NonNull Map<String, String> variables) {

        event.reply("It works!").queue();

    }
}
