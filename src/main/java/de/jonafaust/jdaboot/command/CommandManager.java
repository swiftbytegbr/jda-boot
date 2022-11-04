package de.jonafaust.jdaboot.command;

import de.jonafaust.jdaboot.JDABoot;
import de.jonafaust.jdaboot.annotation.command.Command;
import de.jonafaust.jdaboot.annotation.command.CommandOption;
import de.jonafaust.jdaboot.annotation.command.Subcommand;
import de.jonafaust.jdaboot.annotation.command.SubcommandGroup;
import de.jonafaust.jdaboot.command.info.MessageContextCommandInfo;
import de.jonafaust.jdaboot.command.info.SlashCommandInfo;
import de.jonafaust.jdaboot.command.info.UserContextCommandInfo;
import de.jonafaust.jdaboot.variables.Translator;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class CommandManager extends ListenerAdapter {

    Reflections reflections;
    HashMap<String, SlashCommand> commands = new HashMap<>();
    HashMap<String, ContextCommand<?>> contextCommands = new HashMap<>();

    private Translator translator = JDABoot.getInstance().getTranslator();

    JDA jda;

    public CommandManager(JDA jda, Class<?> mainClass) {
        this.jda = jda;
        this.reflections = new Reflections(mainClass.getPackageName().split("\\.")[0]);

        this.reflections.getTypesAnnotatedWith(Command.class).forEach(clazz -> {

            try {
                Command annotation = clazz.getAnnotation(Command.class);

                String name = annotation.name();
                String description = annotation.description();

                if (SlashCommand.class.isAssignableFrom(clazz)) {
                    Constructor<?> constructor = clazz.getConstructor();
                    SlashCommand cmd = (SlashCommand) constructor.newInstance();

                    CommandData data = buildCommand(annotation);
                    if (!data.getType().equals(net.dv8tion.jda.api.interactions.commands.Command.Type.SLASH)) {
                        log.warn("Command " + name + " is not a slash command but is a child of SlashCommand! Skipping...");
                        return;
                    }
                    commands.put(data.getName(), cmd);
                    jda.upsertCommand(data).queue();
                    log.info("Registered command: " + data.getName());
                    cmd.onEnable((SlashCommandData) data);

                } else if (UserContextCommand.class.isAssignableFrom(clazz)) {

                    Constructor<?> constructor = clazz.getConstructor();
                    ContextCommand<?> cmd = (ContextCommand<?>) constructor.newInstance();

                    CommandData data = buildCommand(annotation);
                    if (!data.getType().equals(net.dv8tion.jda.api.interactions.commands.Command.Type.USER)) {
                        log.warn("Command " + name + " is not a user context command but is a child of UserContextCommand! Skipping...");
                        return;
                    }

                    contextCommands.put(data.getName(), cmd);
                    jda.upsertCommand(data).queue();
                    log.info("Registered command: " + data.getName());
                    cmd.onEnable(data);
                } else if (MessageContextCommand.class.isAssignableFrom(clazz)) {

                    Constructor<?> constructor = clazz.getConstructor();
                    ContextCommand<?> cmd = (ContextCommand<?>) constructor.newInstance();

                    CommandData data = buildCommand(annotation);
                    if (!data.getType().equals(net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE)) {
                        log.warn("Command " + name + " is not a message context command but is a child of MessageContextCommand! Skipping...");
                        return;
                    }

                    contextCommands.put(data.getName(), cmd);
                    jda.upsertCommand(data).queue();
                    log.info("Registered command: " + data.getName());
                    cmd.onEnable(data);
                } else {
                    log.warn("Command " + name + " is not a child of SlashCommand or ContextCommand! Skipping...");
                }


            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                e.printStackTrace();
            }

        });

        jda.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        String name = event.getName();
        SlashCommand executor = commands.get(name);
        if (executor != null) {
            executor.onCommand(new SlashCommandInfo(event.getJDA(), event.getResponseNumber(), event.getInteraction()));
        }

    }

    @Override
    public void onUserContextInteraction(@Nonnull UserContextInteractionEvent event) {
        genericContextEvent(event);
    }

    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
        genericContextEvent(event);
    }

    private void genericContextEvent(GenericContextInteractionEvent<?> event) {
        String name = event.getName();
        ContextCommand<?> executor = contextCommands.get(name);

        if (event instanceof UserContextInteractionEvent) {
            if (executor != null) {
                ((UserContextCommand) executor).onCommand(new UserContextCommandInfo(event.getJDA(), event.getResponseNumber(), (UserContextInteraction) event.getInteraction()));
            }
        } else if (event instanceof MessageContextInteractionEvent) {
            if (executor != null) {
                ((MessageContextCommand) executor).onCommand(new MessageContextCommandInfo(event.getJDA(), event.getResponseNumber(), (MessageContextInteraction) event.getInteraction()));
            }
        }
    }

    private CommandData buildCommand(Command command) {

        String id = translator.processTranslation(DiscordLocale.ENGLISH_US, command.name());

        return switch (command.type()) {
            case SLASH -> buildSlashCommand(id, command);
            case USER, MESSAGE -> buildUserOrChatCommand(command.type(), id, command);
        };

    }

    private SlashCommandData buildSlashCommand(String id, Command command) {

        String description = translator.processTranslation(DiscordLocale.ENGLISH_US, command.description());
        SlashCommandData data = Commands.slash(id, description);

        //Command
        data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.enabledFor()));
        data.setGuildOnly(command.guildOnly());
        data.setNameLocalizations(generateDiscordLocalised(command.name()));
        data.setDescriptionLocalizations(generateDiscordLocalised(command.description()));
        data.addOptions(buildOptions(command.options()));
        data.addSubcommands(buildSubcommands(command.subcommands()));

        //SubCommandGroups
        for (SubcommandGroup subcommandGroup : command.subcommandGroups()) {

            String subGroupId = translator.processTranslation(DiscordLocale.ENGLISH_US, subcommandGroup.name());
            String subGroupDescription = translator.processTranslation(DiscordLocale.ENGLISH_US, subcommandGroup.description());
            SubcommandGroupData subGroupData = new SubcommandGroupData(subGroupId, subGroupDescription);

            subGroupData.setNameLocalizations(generateDiscordLocalised(subcommandGroup.name()));
            subGroupData.setDescriptionLocalizations(generateDiscordLocalised(subcommandGroup.description()));
            subGroupData.addSubcommands(buildSubcommands(subcommandGroup.subcommands()));

            data.addSubcommandGroups(subGroupData);
        }

        return data;
    }

    private CommandData buildUserOrChatCommand(Command.Type type, String id, Command command) {

        CommandData data = null;

        if (type.equals(Command.Type.USER)) {
            data = Commands.user(id);
        } else if (type.equals(Command.Type.MESSAGE)) {
            data = Commands.message(id);
        }

        data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.enabledFor()));
        data.setGuildOnly(command.guildOnly());
        data.setNameLocalizations(generateDiscordLocalised(command.name()));

        return data;
    }

    private HashMap<DiscordLocale, String> generateDiscordLocalised(String old) {

        HashMap<DiscordLocale, String> map = new HashMap<>();

        for (DiscordLocale locale : DiscordLocale.values()) {
            if (locale != DiscordLocale.UNKNOWN) {
                map.put(locale, translator.processTranslation(locale, old));
            }
        }

        return map;
    }

    private List<OptionData> buildOptions(CommandOption[] options) {

        List<OptionData> optionDataList = new ArrayList<>();

        for (CommandOption option : options) {

            String optionId = translator.processTranslation(DiscordLocale.ENGLISH_US, option.name());
            String optionDescription = translator.processTranslation(DiscordLocale.ENGLISH_US, option.description());
            OptionData optionData = new OptionData(option.type(), optionId, optionDescription, option.required(), option.autoComplete());

            if (option.maxLength() > 0) optionData.setMaxLength(option.maxLength());
            if (option.minLength() > 0) optionData.setMinLength(option.minLength());
            if (option.maxValue() > 0) optionData.setMaxValue(option.maxValue());
            if (option.minValue() > 0) optionData.setMinValue(option.minValue());
            if (option.type().equals(OptionType.CHANNEL)) optionData.setChannelTypes(option.channelTypes());
            optionData.setNameLocalizations(generateDiscordLocalised(option.name()));
            optionData.setDescriptionLocalizations(generateDiscordLocalised(option.description()));

            //Choices
            for (CommandOption.Choice choice : option.optionChoices())
                optionData.addChoice(choice.name(), choice.value());

            optionDataList.add(optionData);
        }

        return optionDataList;
    }

    private List<SubcommandData> buildSubcommands(Subcommand[] subcommands) {

        List<SubcommandData> subcommandDataList = new ArrayList<>();

        for (Subcommand subcommand : subcommands) {

            String subId = translator.processTranslation(DiscordLocale.ENGLISH_US, subcommand.name());
            String subDescription = translator.processTranslation(DiscordLocale.ENGLISH_US, subcommand.description());
            SubcommandData subData = new SubcommandData(subId, subDescription);

            subData.setNameLocalizations(generateDiscordLocalised(subcommand.name()));
            subData.setDescriptionLocalizations(generateDiscordLocalised(subcommand.description()));
            subData.addOptions(buildOptions(subcommand.options()));

            subcommandDataList.add(subData);
        }

        return subcommandDataList;
    }
}
