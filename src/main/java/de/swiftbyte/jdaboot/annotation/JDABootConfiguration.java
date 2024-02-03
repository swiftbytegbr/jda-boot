package de.swiftbyte.jdaboot.annotation;

import de.swiftbyte.jdaboot.MemberCachePolicyConfiguration;
import de.swiftbyte.jdaboot.configuration.ConfigProvider;
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
     * Specifies the configuration provider class to be used.
     * By default, it is set to {@link PropertiesConfigProviderImpl}.
     * Another available configuration provider is {@link YmlConfigProviderImpl},
     * which can be used to read configuration from YAML files.
     *
     * @return The configuration provider class.
     * @since alpha.4
     */
    Class<? extends ConfigProvider> configProvider() default PropertiesConfigProviderImpl.class;

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

}
