package de.swiftbyte.jdaboot.interaction.command;

import de.swiftbyte.jdaboot.JDABootObjectManager;
import de.swiftbyte.jdaboot.annotation.interaction.command.CommandOption;
import de.swiftbyte.jdaboot.annotation.interaction.command.SlashCommandDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.command.Subcommand;
import de.swiftbyte.jdaboot.annotation.interaction.command.SubcommandGroup;
import de.swiftbyte.jdaboot.variables.TranslationProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The CommandManager class is responsible for managing commands in the application.
 * It uses reflection to find classes annotated with @Command and creates instances of those classes.
 * It also handles command invocation events and delegates them to the appropriate command instances.
 *
 * @since alpha.4
 */
@Slf4j
public class CommandManager extends ListenerAdapter {

    private HashMap<String, SlashCommandExecutor> commands = new HashMap<>();

    private HashMap<String, ContextCommandExecutor<?>> contextCommands = new HashMap<>();

    @Getter
    private HashMap<String, CommandData> commandData = new HashMap<>();

    /**
     * Constructor for CommandManager. Initializes the manager with the specified JDA instance and main class.
     * It uses reflection to find classes annotated with @Command and creates instances of those classes.
     *
     * @param jda       The JDA instance to use for command handling.
     * @param mainClass The main class of your project.
     * @since alpha.4
     */
    public CommandManager(JDA jda, Class<?> mainClass) {
        Reflections reflections = new Reflections(mainClass.getPackageName());

        reflections.getTypesAnnotatedWith(SlashCommandDefinition.class).forEach(clazz -> {

            SlashCommandDefinition annotation = clazz.getAnnotation(SlashCommandDefinition.class);

            String name = annotation.name();

            if (SlashCommandExecutor.class.isAssignableFrom(clazz)) {
                SlashCommandExecutor cmd = (SlashCommandExecutor) JDABootObjectManager.getOrInitialiseObject(clazz);

                CommandData data = buildCommand(annotation);
                if (!data.getType().equals(net.dv8tion.jda.api.interactions.commands.Command.Type.SLASH)) {
                    log.warn("Command {} is not a slash command but is a child of SlashCommand! Skipping...", name);
                    return;
                }
                commandData.put(data.getName(), data);
                commands.put(data.getName(), cmd);
                if (!annotation.isGlobal()) {
                    return;
                }
                jda.upsertCommand(data).queue();
                cmd.onEnable((SlashCommandData) data);
                log.info("Registered slash command {}", clazz.getName());
            } else if (UserContextCommandExecutor.class.isAssignableFrom(clazz)) {

                ContextCommandExecutor<?> cmd = (ContextCommandExecutor<?>) JDABootObjectManager.getOrInitialiseObject(clazz);

                CommandData data = buildCommand(annotation);
                if (!data.getType().equals(net.dv8tion.jda.api.interactions.commands.Command.Type.USER)) {
                    log.warn("Command {} is not a user context command but is a child of UserContextCommand! Skipping...", name);
                    return;
                }
                commandData.put(data.getName(), data);
                contextCommands.put(data.getName(), cmd);
                if (!annotation.isGlobal()) {
                    return;
                }
                jda.upsertCommand(data).queue();
                cmd.onEnable(data);
                log.info("Registered user context command {}", clazz.getName());
            } else if (MessageContextCommandExecutor.class.isAssignableFrom(clazz)) {

                ContextCommandExecutor<?> cmd = (ContextCommandExecutor<?>) JDABootObjectManager.getOrInitialiseObject(clazz);

                CommandData data = buildCommand(annotation);
                if (!data.getType().equals(net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE)) {
                    log.warn("Command {} is not a message context command but is a child of MessageContextCommand! Skipping...", name);
                    return;
                }
                commandData.put(data.getName(), data);
                contextCommands.put(data.getName(), cmd);
                if (!annotation.isGlobal()) {
                    return;
                }
                jda.upsertCommand(data).queue();
                cmd.onEnable(data);
                log.info("Registered message context command {}", clazz.getName());
            } else {
                log.warn("Command {} is not a child of SlashCommand or ContextCommand! Skipping...", name);
            }
        });

        jda.addEventListener(this);
    }

    /**
     * Handles slash command interaction events. When a slash command is invoked, this method finds the corresponding
     * command instance and delegates the event to it.
     *
     * @param event The slash command interaction event.
     * @since alpha.4
     */
    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        String name = event.getName();
        SlashCommandExecutor executor = commands.get(name);
        if (executor != null) {
            executor.call(event);
        }

    }

    /**
     * Handles command autocomplete interaction events. When a command autocomplete is invoked, this method finds the corresponding
     * command instance and delegates the event to it.
     *
     * @param event The command autocomplete interaction event.
     * @since alpha.4
     */
    @Override
    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
        String name = event.getName();
        SlashCommandExecutor executor = commands.get(name);
        if (executor != null) {
            executor.onAutoComplete(event.getFocusedOption(), event);
        }
    }

    /**
     * Handles user context interaction events. When a user context command is invoked, this method finds the corresponding
     * command instance and delegates the event to it.
     *
     * @param event The user context interaction event.
     * @since alpha.4
     */
    @Override
    public void onUserContextInteraction(@Nonnull UserContextInteractionEvent event) {
        genericContextEvent(event);
    }

    /**
     * Handles message context interaction events. When a message context command is invoked, this method finds the corresponding
     * command instance and delegates the event to it.
     *
     * @param event The message context interaction event.
     * @since alpha.4
     */
    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
        genericContextEvent(event);
    }

    /**
     * Handles generic context interaction events. When a context command is invoked, this method finds the corresponding
     * command instance and delegates the event to it.
     *
     * @param event The generic context interaction event.
     * @since alpha.4
     */
    private void genericContextEvent(GenericContextInteractionEvent<?> event) {
        String name = event.getName();
        ContextCommandExecutor<?> executor = contextCommands.get(name);

        if (event instanceof UserContextInteractionEvent) {
            if (executor != null) {
                ((UserContextCommandExecutor) executor).onCommand((UserContextInteractionEvent) event);
            }
        } else if (event instanceof MessageContextInteractionEvent) {
            if (executor != null) {
                ((MessageContextCommandExecutor) executor).onCommand((MessageContextInteractionEvent) event);
            }
        }
    }

    /**
     * Builds a CommandData object based on the provided Command annotation.
     * The type of the command (slash, user, or message) determines how the CommandData object is built.
     *
     * @param command The Command annotation to use for building the CommandData.
     * @return The built CommandData.
     * @since alpha.4
     */
    private CommandData buildCommand(SlashCommandDefinition command) {

        String id = TranslationProcessor.processTranslation(DiscordLocale.ENGLISH_US, command.name());

        return switch (command.type()) {
            case SLASH -> buildSlashCommand(id, command);
            case USER, MESSAGE -> buildUserOrChatCommand(command.type(), id, command);
        };

    }

    /**
     * Builds a SlashCommandData object based on the provided Command annotation.
     *
     * @param command The Command annotation to use for building the SlashCommandData.
     * @return The built SlashCommandData.
     * @since alpha.4
     */
    private SlashCommandData buildSlashCommand(String id, SlashCommandDefinition command) {

        String description = TranslationProcessor.processTranslation(DiscordLocale.ENGLISH_US, command.description());
        SlashCommandData data = Commands.slash(id, description);

        //Command
        if (command.enabledFor() != Permission.UNKNOWN) {
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.enabledFor()));
        }

        if (command.guildOnly()) {
            data.setContexts(InteractionContextType.GUILD);
        } else {
            data.setContexts(command.contexts());
        }
        data.setIntegrationTypes(command.integrationTypes());

        data.setNameLocalizations(generateDiscordLocalised(command.name()));
        data.setDescriptionLocalizations(generateDiscordLocalised(command.description()));
        data.addOptions(buildOptions(command.options()));
        data.addSubcommands(buildSubcommands(command.subcommands()));
        //SubCommandGroups
        for (SubcommandGroup subcommandGroup : command.subcommandGroups()) {

            String subGroupId = TranslationProcessor.processTranslation(DiscordLocale.ENGLISH_US, subcommandGroup.name());
            String subGroupDescription = TranslationProcessor.processTranslation(DiscordLocale.ENGLISH_US, subcommandGroup.description());
            SubcommandGroupData subGroupData = new SubcommandGroupData(subGroupId, subGroupDescription);

            subGroupData.setNameLocalizations(generateDiscordLocalised(subcommandGroup.name()));
            subGroupData.setDescriptionLocalizations(generateDiscordLocalised(subcommandGroup.description()));
            subGroupData.addSubcommands(buildSubcommands(subcommandGroup.subcommands()));

            data.addSubcommandGroups(subGroupData);
        }

        return data;
    }

    /**
     * Builds a CommandData object based on the provided Command annotation and command type.
     *
     * @param type    The type of the command.
     * @param id      The id of the command.
     * @param command The Command annotation to use for building the CommandData.
     * @return The built CommandData.
     * @since alpha.4
     */
    private CommandData buildUserOrChatCommand(SlashCommandDefinition.Type type, String id, SlashCommandDefinition command) {

        CommandData data = null;

        if (type.equals(SlashCommandDefinition.Type.USER)) {
            data = Commands.user(id);
        } else if (type.equals(SlashCommandDefinition.Type.MESSAGE)) {
            data = Commands.message(id);
        }

        if (command.enabledFor() != Permission.UNKNOWN) {
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.enabledFor()));
        }

        if (command.guildOnly()) {
            data.setContexts(InteractionContextType.GUILD);
        } else {
            data.setContexts(command.contexts());
        }
        data.setIntegrationTypes(command.integrationTypes());

        data.setNameLocalizations(generateDiscordLocalised(command.name()));

        return data;
    }

    /**
     * Generates a map of localized strings for the provided string.
     *
     * @param old The original string to localize.
     * @return The map of localized strings.
     * @since alpha.4
     */
    private HashMap<DiscordLocale, String> generateDiscordLocalised(String old) {

        HashMap<DiscordLocale, String> map = new HashMap<>();

        for (DiscordLocale locale : DiscordLocale.values()) {
            if (locale != DiscordLocale.UNKNOWN) {
                map.put(locale, TranslationProcessor.processTranslation(locale, old));
            }
        }

        return map;
    }

    /**
     * Builds a list of OptionData objects based on the provided array of CommandOption annotations.
     *
     * @param options The array of CommandOption annotations to use for building the OptionData objects.
     * @return The list of built OptionData objects.
     * @since alpha.4
     */
    private List<OptionData> buildOptions(CommandOption[] options) {

        List<OptionData> optionDataList = new ArrayList<>();

        for (CommandOption option : options) {

            String optionId = TranslationProcessor.processTranslation(DiscordLocale.ENGLISH_US, option.name());
            String optionDescription = TranslationProcessor.processTranslation(DiscordLocale.ENGLISH_US, option.description());
            OptionData optionData = new OptionData(option.type(), optionId, optionDescription, option.required(), option.autoComplete());

            if (option.maxLength() > 0) {
                optionData.setMaxLength(option.maxLength());
            }
            if (option.minLength() > 0) {
                optionData.setMinLength(option.minLength());
            }
            if (option.maxValue() > 0) {
                optionData.setMaxValue(option.maxValue());
            }
            if (option.minValue() > 0) {
                optionData.setMinValue(option.minValue());
            }
            if (option.type().equals(OptionType.CHANNEL)) {
                optionData.setChannelTypes(option.channelTypes());
            }
            optionData.setNameLocalizations(generateDiscordLocalised(option.name()));
            optionData.setDescriptionLocalizations(generateDiscordLocalised(option.description()));

            //Choices
            for (CommandOption.Choice choice : option.optionChoices()) {
                optionData.addChoice(choice.name(), choice.value());
            }

            optionDataList.add(optionData);
        }

        return optionDataList;
    }

    /**
     * Builds a list of SubcommandData objects based on the provided array of Subcommand annotations.
     *
     * @param subcommands The array of Subcommand annotations to use for building the SubcommandData objects.
     * @return The list of built SubcommandData objects.
     * @since alpha.4
     */
    private List<SubcommandData> buildSubcommands(Subcommand[] subcommands) {

        List<SubcommandData> subcommandDataList = new ArrayList<>();

        for (Subcommand subcommand : subcommands) {

            String subId = TranslationProcessor.processTranslation(DiscordLocale.ENGLISH_US, subcommand.name());
            String subDescription = TranslationProcessor.processTranslation(DiscordLocale.ENGLISH_US, subcommand.description());
            SubcommandData subData = new SubcommandData(subId, subDescription);

            subData.setNameLocalizations(generateDiscordLocalised(subcommand.name()));
            subData.setDescriptionLocalizations(generateDiscordLocalised(subcommand.description()));
            subData.addOptions(buildOptions(subcommand.options()));

            subcommandDataList.add(subData);
        }

        return subcommandDataList;
    }
}
