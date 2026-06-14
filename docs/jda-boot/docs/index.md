# JDA-Boot Documentation

This project uses [JDA](https://github.com/DV8FromTheWorld/JDA) by [Austin Keener](https://github.com/DV8FromTheWorld/).

New to JDA-Boot? Check out the [Getting Started page](nav/getting-started.md)!

## Why JDA-Boot?
JDA-Boot aims to simplify Discord Bot development by providing pre-built tools. This allows developers to focus on the essentials of bot development without having to spend hours on a custom command system, for example. JDABoot manages Discord connections through JDA's `ShardManager`, while still exposing the underlying JDA API when direct access is required. This ensures maximum flexibility. In addition, JDA-Boot is built in such a way that it can easily be extended if the existing functionality is not sufficient.

## Installation
JDA-Boot can easily be added to your project via the [Maven Central Repository](https://central.sonatype.com/artifact/de.swiftbyte/jda-boot). Please replace 'VERSION' with the current
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
## Roadmap
```
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
- ✅ Updated and improved docs
- ⛔ Improved Kotlin friendliness
- ⛔ Database ORM System
- ⛔ Better error handling
- ✅ Variable transfer between commands, buttons, and modals
    - ⛔ Options to save variables and random command ids between bot restarts
- ✅ Scheduler System
- ✅ Functionality to set a voice dispatch interceptor
```