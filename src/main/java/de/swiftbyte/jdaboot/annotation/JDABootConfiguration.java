package de.swiftbyte.jdaboot.annotation;

import de.swiftbyte.jdaboot.MemberCachePolicyConfiguration;
import de.swiftbyte.jdaboot.configuration.ConfigProvider;
import de.swiftbyte.jdaboot.configuration.EnvConfigProviderImpl;
import de.swiftbyte.jdaboot.configuration.PropertiesConfigProviderImpl;
import de.swiftbyte.jdaboot.configuration.YmlConfigProviderImpl;
import de.swiftbyte.jdaboot.variables.ResourceBundleTranslationProviderImpl;
import de.swiftbyte.jdaboot.variables.TranslationProvider;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the configuration for the JDABoot framework.
 *
 * @since alpha.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JDABootConfiguration {

    /**
     * The configuration provider chain is used to retrieve configuration values.
     * The first provider in the chain is used to retrieve the values.
     * If the value is not found, the next provider in the chain is used.
     *
     * @return The configuration provider chain.
     * @since alpha.5
     */
    Class<? extends ConfigProvider>[] configProviderChain() default {EnvConfigProviderImpl.class, PropertiesConfigProviderImpl.class, YmlConfigProviderImpl.class};

    /**
     * Specifies the translation provider class to be used.
     * By default, it is set to {@link ResourceBundleTranslationProviderImpl}.
     *
     * @return The translation provider class.
     * @since alpha.4
     */
    Class<? extends TranslationProvider> translationProvider() default ResourceBundleTranslationProviderImpl.class;

    /**
     * Specifies the GatewayIntents that the JDA instance should be configured with.
     * By default, GatewayIntent.DEFAULT is used.
     *
     * @return An array of GatewayIntents.
     * @since alpha.4
     */
    GatewayIntent[] intents() default {};

    /**
     * Specifies the CacheFlags that should be enabled in the JDA instance.
     * By default, no CacheFlags are enabled.
     *
     * @return An array of CacheFlags to enable.
     * @since alpha.4
     */
    CacheFlag[] enabledCacheFlags() default {};

    /**
     * Specifies the CacheFlags that should be disabled in the JDA instance.
     * By default, no CacheFlags are disabled.
     *
     * @return An array of CacheFlags to disable.
     * @since alpha.4
     */
    CacheFlag[] disabledCacheFlags() default {};

    /**
     * Specifies the MemberCachePolicy that the JDA instance should use.
     * By default, the policy is set to MemberCachePolicyAutoConfiguration.DEFAULT.
     *
     * @return The MemberCachePolicy to use.
     * @since alpha.4
     */
    MemberCachePolicyConfiguration memberCachePolicy() default MemberCachePolicyConfiguration.DEFAULT;

    /**
     * Specifies whether console commands should be enabled.
     * By default, console commands are enabled.
     *
     * @return True if console commands should be enabled, false otherwise.
     * @since alpha.4
     */
    boolean enableConsoleCommands() default true;

    /**
     * Specifies the profile to use for the configuration.
     * By default, the profile is set to "default".
     *
     * @return The profile to use for the configuration.
     * @since alpha.5
     */
    String configProfile() default "default";

}
