package de.swiftbyte.jdaboot.annotation;

import de.swiftbyte.jdaboot.MemberCachePolicyConfiguration;
import de.swiftbyte.jdaboot.ShardManagerBuilderCustomizer;
import de.swiftbyte.jdaboot.configuration.ConfigProvider;
import de.swiftbyte.jdaboot.variables.ResourceBundleTranslationProviderImpl;
import de.swiftbyte.jdaboot.variables.TranslationProvider;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jspecify.annotations.NonNull;

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
     * You can specify additional providers by setting this value.
     *
     * @return The configuration provider chain.
     * @since 1.0.0-alpha.5
     */
    @NonNull Class<? extends ConfigProvider> @NonNull [] additionalConfigProviders() default {};

    /**
     * Specifies the translation provider class to be used.
     * By default, it is set to {@link ResourceBundleTranslationProviderImpl}.
     *
     * @return The translation provider class.
     * @since alpha.4
     */
    @NonNull Class<? extends TranslationProvider> translationProvider() default ResourceBundleTranslationProviderImpl.class;

    /**
     * Specifies the ShardManagerBuilderCustomizer classes to be used for customizing the ShardManagerBuilder.
     * By default, no customizers are specified.
     *
     * @return An array of ShardManagerBuilderCustomizer classes.
     * @since 1.0.0-beta.2
     */
    @NonNull
    Class<? extends ShardManagerBuilderCustomizer> @NonNull [] builderCustomizers() default {};

    /**
     * Specifies the GatewayIntents that the JDA instance should be configured with.
     * By default, GatewayIntent.DEFAULT is used.
     *
     * @return An array of GatewayIntents.
     * @since alpha.4
     */
    @NonNull GatewayIntent @NonNull [] intents() default {};

    /**
     * Specifies the CacheFlags that should be enabled in the JDA instance.
     * By default, no CacheFlags are enabled.
     *
     * @return An array of CacheFlags to enable.
     * @since alpha.4
     */
    @NonNull CacheFlag @NonNull [] enabledCacheFlags() default {};

    /**
     * Specifies the CacheFlags that should be disabled in the JDA instance.
     * By default, no CacheFlags are disabled.
     *
     * @return An array of CacheFlags to disable.
     * @since alpha.4
     */
    @NonNull CacheFlag @NonNull [] disabledCacheFlags() default {};

    /**
     * Specifies the MemberCachePolicy that the JDA instance should use.
     * By default, the policy is set to MemberCachePolicyAutoConfiguration.DEFAULT.
     *
     * @return The MemberCachePolicy to use.
     * @since alpha.4
     */
    @NonNull MemberCachePolicyConfiguration memberCachePolicy() default MemberCachePolicyConfiguration.DEFAULT;

    /**
     * Specifies whether console commands should be enabled.
     * By default, console commands are enabled.
     *
     * @return True if console commands should be enabled, false otherwise.
     * @since alpha.4
     */
    boolean enableConsoleCommands() default true;

    /**
     * Specifies additional active configuration profiles.
     * The default profile is always active and loaded before these profiles.
     *
     * @return The additional profiles in loading order.
     * @since 1.0.0-beta.2
     */
    @NonNull String @NonNull [] configProfiles() default {};

}
