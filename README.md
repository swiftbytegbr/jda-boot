# JDA-Boot
### jda-boot is an annotation-driven jda framework that lets you create bots insanely fast 
WIP

This project uses [JDA](https://github.com/DV8FromTheWorld/JDA) by [Austin Keener](https://github.com/DV8FromTheWorld/). 

More information coming soon!


### Installation

#### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependecies>
	<dependency>
	    <groupId>com.github.jonafaust</groupId>
	    <artifactId>jda-boot</artifactId>
	    <version>alpha.2</version>
	</dependency>
</dependecies>
```


#### Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.jonafaust:jda-boot:alpha.2.3'
}
```
---
### Roadmap

✅ = Implemented, 🚧 = Work in Progress, ⛔ = ToDo

- ✅ Commands
- ✅ Option Commands
- ✅ Events
- ✅ Embeds
- ✅ Variable System
- ✅ internationalization (i18n)
- 🚧 Config System
- ⛔ Other Interactions (Buttons, Select Menus, Modals)
- ⛔ Database ORM System
- ⛔ Scheduler System
- ⛔ Configuration Classes
- ⛔ Music Implementation (LavaPlayer)

### Usage

#### Creating a bot

```java
public class MyBot {
    public static void main(String[] args) {
        JdaBoot.run(MyBot.class);
    }
}
```

***Note*** that you need a discord.token property in your config.properties file.

### Creating a command

#### Simple Command
```java
@Command(name = "ping", description = "Pong!")
public class PingCommand implements SimpleCommand {
    
    @Override
    public void onCommand(CommandContext ctx) {
        ctx.reply("Pong!");
    }
}
```

#### Option Command
```java
@Command(name = "echo", description = "Echoes a message")
public class EchoCommand implements OptionCommand {
    
    @Override
    SlashCommandData onEnable(SlashCommandData data) {
        data.addOption(OptionType.STRING, "message", "The message to echo", true);
        return data;
    }
    
    @Override
    public void onCommand(CommandContext ctx) {
        String message = ctx.getOption("message").getAsString();
        ctx.reply(message);
    }
}
```
---

### Listen for events

```java
public class ReadyListener {
    
    @EventHandler
    public void onReady(ReadyEvent event) {
        System.out.println("Bot is ready!");
    }
}
```
***Note*** The method must be public and have only single parameter of the event you want to listen for. The name of the method doesn't matter. 
The class need to have a public no-args constructor.

