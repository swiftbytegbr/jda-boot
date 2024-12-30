# Getting Started
## Installation
JDA-Boot can easily be added to your project via the Maven Central Repository. Please replace 'VERSION' with the current
jda-boot version.
=== "Maven"
    ```xml
    <dependency>
      <groupId>de.swiftbyte</groupId>
      <artifactId>jda-boot</artifactId>
      <version>VERSION</version>
    </dependency>
    ```

=== "Gradle"
    ```groovy
    repositories {
      mavenCentral()
    }
    
    dependencies {
        implementation 'de.swiftbyte:jda-boot:VERSION'
    }
    ```

## Configure JDA-Boot
In order for JDA-Boot to start-up, it must first be configured. To do this, a configuration file with the name config.properties must be created in the resources folder of the project. JDA-Boot also supports yml files. It is also possible to write your own config provider. More detailed information can be found at [Configuration Providers](translation-configuration.md#config-providers)

So in order for JDA-Boot to know which Discord token it should use, it must be entered in the config. 'TOKEN' should be replaced with the Discord Bot Token from the Discord Developers Portal.
=== "config.properties"
    ```properties
    discord.token=TOKEN
    ```

=== "config.yml"
    ```yaml
    discord:
        token: TOKEN
    ```

## Setting up your main class
For the project to start properly, a main class must be set up last. For the sake of simplicity, we will call it Main here. In our Main method, we now execute the method `JDABoot.run(ExampleBot.class, args);`. When we start the project now, the bot should go online!
=== "Java"
    ```java
    public class Main {
        public static void main(String[] args) {
            JDABoot.run(Main.class, args);
        }
    }
    ```

=== "Kotlin"
    ```kotlin
    fun main(args: Array<String>) {
        JDABoot.run(Main::class.java, args)
    }

    class Main {}
    ```

It is often necessary to make further settings for starting JDA, such as gateway intents, cache flags or the member cache policy. JDA-Boot has introduced the `@JDABootConfiguration` annotation for this purpose. The main class must be annotated with this annotation in order to adopt the specified values.
=== "Java"
    ```java
    @JDABootConfiguration(
        intents = {GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS},
        disabledCacheFlags = {CacheFlag.ACTIVITY},
        memberCachePolicy = MemberCachePolicyConfiguration.VOICE
    )
    public class Main {
        public static void main(String[] args) {
            JDABoot.run(Main.class, args);
        }
    }
    ```

=== "Kotlin"
    ```kotlin
    fun main(args: Array<String>) {
        JDABoot.run(Main::class.java, args)
    }

    @JDABootConfiguration(
        intents = [GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS],
        disabledCacheFlags = [CacheFlag.ACTIVITY],
        memberCachePolicy = MemberCachePolicyConfiguration.VOICE
    )
    class Main {}
    ```

In addition, it may sometimes be necessary to execute your own program code after successfully booting JDA-Boot. We have introduced the `onReady()` method for this purpose. This method will be executed as soon as the initialization of JDA-Boot is completed.
=== "Java"
    ```java
    @JDABootConfiguration(
        intents = {GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS},
        disabledCacheFlags = {CacheFlag.ACTIVITY},
        memberCachePolicy = MemberCachePolicyConfiguration.VOICE
    )
    public class Main {
        public static void main(String[] args) {
            JDABoot.run(Main.class, args);
        }

        public void onReady() {
            System.out.println("JDA-Boot initialised!");
        }
    }
    ```

=== "Kotlin"
    ```kotlin
    fun main(args: Array<String>) {
        JDABoot.run(Main::class.java, args)
    }

    @JDABootConfiguration(
        intents = [GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS],
        disabledCacheFlags = [CacheFlag.ACTIVITY],
        memberCachePolicy = MemberCachePolicyConfiguration.VOICE
    )
    class Main {
        fun onReady() {
            println("JDA-Boot initialised!")
        }
    }
    ```

## Creating your first command
Now it's time to fill our Discord Bot with functions. At this point, we will only go into the features of JDA-Boot very briefly. For more detailed information on the individual topics, please refer to the dedicated wiki pages. The creation of a command always consists of two parts, the configuration part and the execution part. In JDA-Boot, the configuration is carried out via the `@SlashCommandDefinition` annotation. The execution takes place in a dedicated class, which extends the `SlashCommandExecutor` class. The previously mentioned annotation annotates this class. In addition to all the various optional configuration options of Discord commands, at least the name, which is also used as the command ID, and the type must always be set. We also recommend always setting a description, otherwise JDA-Boot will automatically add `“DESCRIPTION MISSING”`. The fired `SlashCommandInteractionEvent` is part of the superclass and has the variable name `event`. In concrete terms, the whole thing looks like this using the example of a ping command:
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
    }
    ```

Other possible command types are User and Message Context Commands, which are not slash commands per se, but are handled similarly by JDA-Boot. Further information on context commands can be found on the [command documentation page](commands.md). 

Last but not least, the created command must be registered on Discord using the `JDABoot.getInstance().updateCommands();` method. We recommend creating a [console command](commands.md#console-commands) for this. In addition, the command is registered globally, which can take up to an hour. For testing purposes, it is therefore advisable to specify a Guild ID as an argument of the method. This means that the command is only registered on the specified server, which should speed up the process considerably.

## Listening for your first event
To listen to an event, a method must be provided with the `@EventHandler` annotation. The type of event is then determined via the event type specified as a parameter. The method can be in any class, but when JDA-Boot is started an instance of this class is created which is used later. It is therefore best to use a separate class for one or more events. The given example shows how to create an event listener for the `GuildMemberJoinEvent`.
=== "Java"
    ```java
    public class EventClass {

        @EventHandler
        public void onMemberJoin(GuildMemberJoinEvent event) {
            System.out.println("User joined!");
        }
    }
    ```

=== "Kotlin"
    ```kotlin
    class EventClass {

        @EventHandler
        fun onReady(event: GuildMemberJoinEvent) {
            println("User joined!")
        }
    }
    ```

## More JDA-Boot Features
This was just a brief introduction to developing Discord bots with JDA-Boot, it's worth checking out the more topic specific documentation pages:

- Interactions
    - [Commands](commands.md)
    - [Buttons](buttons.md)
    - [Select Menus](select-menus.md)
    - [Modals](modals.md)
- [Embeds](embeds.md)
- [Events](events.md)
- [Translation and Configuration](translation-configuration.md)
- [Scheduler](scheduler.md)