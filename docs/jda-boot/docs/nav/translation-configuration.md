# Translation and Configuration

JDA-Boot provides robust systems for managing translations and configurations, allowing you to create dynamic and localized bots with ease. This page explains how to use these systems and customize them for your project.

## Translation System

The translation system in JDA-Boot allows you to use localized strings in your bot. By default, JDA-Boot uses Java resource bundles for translations, but you can also implement your own `TranslationProvider`.

### Using Translations

Translations can be used in any place where normal variables are supported by using the syntax `#{TRANSLATION_KEY}`. You can also use variables within translations to dynamically customize the content.

#### Default Translation Files

- The default language file is `messages.properties`.
- For other languages, use the format `messages_<lang>.properties` (e.g., `messages_de.properties` for German).

!!! example
    `messages.properties`:
    ```
    welcome.message=Welcome, ${username}!
    ```

    === "Java"
        Usage in code:
        ```java
        @Embed(
            title = "#{welcome.message}",
            description = "Enjoy your stay!"
        )
        private TemplateEmbed embed;
        ```

If the `username` variable is set to "John", the title will render as "Welcome, John!".

### Custom Translation Providers

You can create a custom translation provider by implementing the `TranslationProvider` interface and setting it in the `translationProvider` field of the `@JDABootConfiguration` annotation.

!!! example
    === "Java"
        ```java
        @JDABootConfiguration(
            translationProvider = CustomTranslationProvider.class
        )
        public class Main {
            // Main Class
        }
        ```

## Configuration System

The configuration system in JDA-Boot allows you to manage settings for your bot. Configuration values can be retrieved dynamically or injected into fields using annotations.

### Using Configuration

Configuration values can be used in any place where normal variables are supported by using the syntax `?{CONFIG_KEY}`. Alternatively, you can use the `@SetValue` annotation to inject configuration values into fields.

!!! example
    === "yml"
        `config.yml`:
        ```yaml
        app:
          name: "MyBot"
        ```
    === "properties"
        `config.properties`:
        ```
        app.name=MyBot
        ```

    Usage in code:
    === "java"
        ```java
        @SetValue("app.name")
        private String appName;
        ```

    The `appName` field will be automatically set to "MyBot".

### Configuration Providers

By default, JDA-Boot supports the following configuration providers:
1. **Environment Variables** (including .env files): Variables must have the prefix `JDA_BOOT_` and be in uppercase with underscores (e.g., `JDA_BOOT_APP_NAME`).
2. **Properties Files**: Configuration can be stored in `.properties` files.
3. **YAML Files**: Configuration can be stored in `.yml` files.

These providers are organized in a chain, so if a value is not found in one provider, the next provider in the chain is used. The default order is:
1. Environment Variables
2. Properties Files
3. YAML Files

### Configuration Profiles

You can define configuration profiles to manage different environments (e.g., development, production). The active profile is determined by the `profile` configuration key. For example, if the profile is set to `dev`, the files `config-dev.yml` or `config-dev.properties` will be used.

!!! example
    === "yml"
        `config.yml`:
        ```yaml
        profile: "dev"
        ```
    
        `config-dev.yml`:
        ```yaml
        app:
          name: "MyBot (Development)"
        ```
    === "properties"
        `config.properties`:
        ```
        profile=dev
        ```
    
        `config-dev.properties`:
        ```
        app.name=MyBot (Development)
        ```

### Custom Configuration Providers

You can create a custom configuration provider by implementing the `ConfigProvider` class and adding it to the `additionalConfigProviders` field in the `@JDABootConfiguration` annotation.

!!! example
    === "java"
        ```java
        @JDABootConfiguration(
            additionalConfigProviders = {CustomConfigProvider.class}
        )
        public class Main {
            // Main Class
        }
        ```