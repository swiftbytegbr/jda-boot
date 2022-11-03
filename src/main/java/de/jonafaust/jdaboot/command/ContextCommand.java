package de.jonafaust.jdaboot.command;

import de.jonafaust.jdaboot.command.info.ContextCommandInfo;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

interface ContextCommand<T extends ContextCommandInfo> {

    default void onEnable(CommandData data) {
    }

    void onCommand(T event);

}
