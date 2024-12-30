# Commands

## Slash Commands
Slash Commands are the regular Discord commands which can be executed via the chat with `/`. To create a command in JDA-Boot, a `@SlashCommandDefinition` annotation must be annotated to a class that extends the `SlashCommandExecutor` class. In addition, the commands must be registered with Discord via `JDABoot.getInstance.updateCommands()` or `JDABoot.getInstance.updateCommands(guildId)`.

### Command Configuration
The `@SlashCommandDefinition` annotation has many customization options for the command. This is a list of all fields with a description of what each field does. If a command is not global, it can be registered via `JDABoot.getInstance.registerCommand(guildId, commandId)`. The command id corresponds to the name of the command.

#### @SlashCommandDefinition
The fields `name` and `type` are required. It also makes sense to set `description`.

| Annotation Field   | Description                                                                               | Data Type                             |
| ------------------ | ----------------------------------------------------------------------------------------- | ------------------------------------- |
| `name`             | The name of the command in discord                                                        | String                                |
| `description`      | The description of the command in discord                                                 | String                                |
| `type`             | The type of the command, must be Type.SLASH                                               | SlashCommandDefinition.Type           |
| `enabledFor`       | The default needed Permission of the command                                              | Permission                            |
| `guildOnly`        | Specifies whether the command should only be used on servers and not in private messages  | Boolean                               |
| `isGlobal`         | Specifies whether the command should be globaly available                                 | Boolean                               |
| `options`          | The options that the command should have                                                  | [CommandOption[]](#commandoption)     |
| `subcommandGroups` | The subcommand groups that the command should have                                        | [SubcommandGroup[]](#subcommandgroup) |
| `subcommands`      | The subcommands that the command should have                                              | [Subcommand[]](#subcommand)           |

#### @CommandOption
The fields `type`, `name` and `description` are required.

| Annotation Field   | Description                                                                               | Data Type                             |
| ------------------ | ----------------------------------------------------------------------------------------- | ------------------------------------- |
| `type`             | The type of the option                                                                    | OptionType                            |
| `name`             | The name of the option                                                                    | String                                |
| `description`      | The description of the option                                                             | String                                |
| `minLength`        | The minimum length of the option                                                          | Integer                               |
| `maxLength`        | The maximum length of the option                                                          | Integer                               |
| `minValue`         | The minimum value of the option                                                           | Integer                               |
| `maxValue`         | The maximum value of the option                                                           | Integer                               |
| `required`         | Specifies whether the option is required                                                  | Boolean                               |
| `autoComplete`     | Specifies whether the option supports auto-complete                                       | Boolean                               |
| `channelTypes`     | The channel types for the option                                                          | ChannelType[]                         |
| `optionChoices`    | The choices for the option                                                                | CommandOption.Choice[]                |

#### @Subcommand
The fields `name` and `description` are required.

| Annotation Field   | Description                                                                               | Data Type                             |
| ------------------ | ----------------------------------------------------------------------------------------- | ------------------------------------- |
| `name`             | The name of the subcommand                                                                | String                                |
| `description`      | The description of the subcommand                                                         | String                                |
| `options`          | The options of the subcommand                                                             | [CommandOption[]](#commandoption)     |

#### @SubcommandGroup
The fields `name` and `description` are required.

| Annotation Field   | Description                                                                               | Data Type                             |
| ------------------ | ----------------------------------------------------------------------------------------- | ------------------------------------- |
| `name`             | The name of the subcommand group                                                          | String                                |
| `description`      | The description of the subcommand group                                                   | String                                |
| `subcommands`      | The subcommands in the subcommand group                                                   | [Subcommand[]](#subcommand)           |

### Command Execution
If the Command class is extended with SlashCommandExecutor, the `onCommand` method must be implemented. This is executed every time the command is used on Discord. The `SlashCommandInteractionEvent` is a field of the superclass with the name `event`. Optionally, the method `onEnable(SlashCommandData data)` can also be implemented. As the name suggests, this is executed when the command is initialized by JDA-Boot. In addition, the method `onAutoComplete(AutoCompleteQuery query, CommandAutoCompleteInteractionEvent event)` can be overwritten. This is only necessary if `autoComplete = true` in a command option. The functionality is the same as with JDA. In addition to these methods, there are also other utility methods that simplify the use of [Embed Systems](embeds.md).

!!! example
    === "Java"
        ```java
        @SlashCommandDefinition(
                name = "ping",
                description = "Check the bot's ping.",
                type = SlashCommandDefinition.Type.SLASH
        )
        public class PingCommand extends SlashCommandExecutor {

            @Override
            public void onCommand() {
                long time = System.currentTimeMillis();

                event.reply("Pong!").setEphemeral(true).queue(response ->
                        response.editOriginal("Pong: " + (System.currentTimeMillis() - time) + " ms").queue());

            }

            @Override
            public void onEnable(SlashCommandData data) {
                System.out.println("Ping command enabled!");
            }
        }
        ```

## Console Commands
JDABoot supports a simple small console command system. It is important that the setting `enableConsoleCommands` of the `@JDABootConfiguration` annotation is enabled. Then a class that implements the `ConsoleCommandExecutor` class can be annotated with the `@ConsoleCommandDefinition` annotation. This has the field `name` which sets the name of the command and the field `aliases` which takes an array of strings and sets alternative names for the command. The command itself is implemented in the method `onCommand(String[] args)`. The arguments specified during execution are passed as an array.

!!! example
    === "Java"
        ```java
        @ConsoleCommandDefinition(name = "registerCommands")
        public class RegisterCommandsCommand implements ConsoleCommandExecutor {

            @Override
            public void onCommand(String[] args) {
                System.out.println("Registering commands!");
                JDABoot.getInstance().updateCommands();
            }
        }
        ```

## User and Message Context Commands
In addition to the classic slash commands, there are now also user and message context commands. These are commands that can be executed by right-clicking on a user or a message. The creation of such a command is very similar to the slash commands, in fact so similar that the same `SlashCommandDefinition` annotation is used. Only the type has to be changed and some options cannot be set. However, the `SlashCommandExecutor` may no longer be extended, but the `UserContextCommandExecutor` or `MessageContextCommandExecutor` must be implemented.