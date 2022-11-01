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
        <id>github</id>
        <url>https://maven.pkg.github.com/jonafaust/jda-boot</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependecies>
    <dependency>
        <groupId>de.jonafaust</groupId>
        <artifactId>jda-boot</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependecies>
```


#### Gradle
```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/jonafaust/jda-boot")
    }
}

dependencies {
    implementation 'de.jonafaust:jda-boot:1.0.0'
}
```
---

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

