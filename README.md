# JDA-Boot

### jda-boot is an annotation-driven jda framework that lets you create bots insanely fast

WIP

This project uses [JDA](https://github.com/DV8FromTheWorld/JDA) by [Austin Keener](https://github.com/DV8FromTheWorld/).

More information coming soon!

## Installation

JDA-Boot can easily be added to your project via the Maven Central Repository. Please replace 'VERSION' with the current
jda-boot version.

### Maven

```xml

<dependency>
    <groupId>de.swiftbyte</groupId>
    <artifactId>jda-boot</artifactId>
    <version>VERSION</version>
</dependency>
```

### Gradle

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'de.swiftbyte:jda-boot:VERSION'
}
```

### Development Builds

Development builds can be added via JitPack. Further instructions are available directly from
JitPack: https://jitpack.io/#swiftbytegbr/jda-boot

## Getting Started

A detailed description of all functions will be added soon.

### Initialise JDA-Boot

The token of the Discord bot is specified via a file with the name 'config.properties' in the Resources folder:

```properties
discord.token=TOKEN
```

In a more advanced configuration it is possible to use config profiles and more config types like .env files.

The JDABoot.run() method must then be executed in the main class of the project.

```java

@JDABootConfiguration(
        intents = {GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS},
        memberCachePolicy = MemberCachePolicyConfiguration.VOICE
)
public class ExampleBot {

    public static void main(String[] args) {
        JDABoot.run(ExampleBot.class, args);
    }

    public void onReady() {
        System.out.println("Ready");
    }
}
```

If there is a method available in your main class called `onReady()`, the method gets executed after jda-boot is initialized.

### Create a Command

Commands can be created by creating a class that implements the SlashCommandExecutor interface and annotating it with
the SlashCommand annotation.

```java

@SlashCommandDefinition(
        name = "test",
        description = "test",
        type = SlashCommandDefinition.Type.SLASH,
        options = {
                @CommandOption(
                        name = "test",
                        description = "test",
                        type = OptionType.STRING,
                        autoComplete = true,
                        required = true)
        }
)
public class TestCommand extends SlashCommandExecutor {

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        event.reply("Test").queue();
    }
}
```

To make this command available for all servers, the method `JDABoot.getInstance().updateCommands()` must be executed
last. Please note that it can take up to an hour until the command is globally available. For testing purposes, the
command should only be updated on one guild. You can use console commands for this purpose.

### Buttons

To create a button,
a class must be created that implements the ButtonExecutor interface
and is annotated with the ButtonDefinition annotation.

```java

@ButtonDefinition(
        label = "Test",
        id = "test_button",
        emoji = "\uD83D\uDC4D",
        type = ButtonDefinition.Type.PRIMARY
)
public class TestButton implements ButtonExecutor {

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        event.reply("Button clicked!").queue();
    }

}
```

To be able to use a button, a ButtonTemplate variable must be created,
which is provided with either a ButtonById or ButtonByClass annotation.

```java
public class ButtonClass {

    @ButtonByClass(TestButton.class)
    private ButtonTemplate button;

}
```

To use the button, the button must be converted into a JDA button using `button.advancedButton().build()`.

### Create an Event

Events can be created by creating a class that contains methods annotated with the EventHandler annotation. The method
must have a parameter of the type of the event that should be handled.

```java
public class TestEvent {

    @EventHandler
    public void onReady(GuildMemberJoinEvent event) {
        System.out.println("User joined");
    }

}
```

### Create an Embed

Embeds can be created by adding the Embed annotation to a variable of type TemplateEmbed. It is possible to define
several embeds in a class that is intended for this purpose, or directly in the class in which the embed is used.

```java
public class EmbedClass {

    @Embed(
            title = "Title",
            description = "Description",
            color = EmbedColor.BLACK
    )
    private TemplateEmbed embed;

}
```

To be able to use the template embed, it must be converted into a JDA embed
using `embed.advancedEmbed().build()`. You can define an id to an embed and creat another embed based on this id. The new
embed then copies all values from the base embed.

### Variables and Translation

It is also possible to use variables in jda-boot objects like buttons, select menus, embeds, etc. The syntax for this is 
as follows: `${VARIABLE_NAME}`. 'VARIABLE_NAME' is a freely selectable name. It is also possible for variables to appear in a translation texts. They are set in the
annotation via the defaultVars setting or via the `setVariable()` method in the Advanced Object. To be able to use
values from the Config, a ? must be used instead of the $. The Java resources bundles are used as the translation system
by default. The `messages.properties` file must therefore be added to the resources for the "default language". All
others must append a language code to the file name. The file name for German would therefore
be `messages_de.properties`. Translated content can be used in embeds as well as in most other areas of jda-boot where a
string is required. A usage looks like this: `#{TRANSLATION_KEY}`. The translation key is the key of the translation in
the properties file. To use values from the config in you code you can create a variable annotated with setValue("SETTING_KEY"):

```java
import de.swiftbyte.jdaboot.annotation.SetValue;

public class ConfigClass {

  @SetValue("discord.token")
  private String token;

}

```

## Roadmap

✅ = Implemented, 🚧 = Work in Progress, ⛔ = ToDo

- ✅ Commands
- ✅ Option Commands
- ✅ Events
- ✅ Embeds
- ✅ Variable System
- ✅ internationalization (i18n)
- ✅ Config System
- ✅ Other Interactions (✅ Buttons, ✅ Select Menus, ✅ Modals)
- ✅ .env configuration support
- ✅ Scheduler System
- ✅ Functionality to set a voice dispatch interceptor
- ✅ Variable transfer between commands, buttons, and modals
    - ⛔ Options to save variables and random command ids between bot restarts
- 🚧 Support for JDA 5.3.0 (User-Installable Apps)
- 🚧 Updated and improved docs
- ⛔ Improved Kotlin friendliness
- ⛔ Database ORM System
- ⛔ Better error handling


