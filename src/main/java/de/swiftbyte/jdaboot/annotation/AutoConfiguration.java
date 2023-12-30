package de.swiftbyte.jdaboot.annotation;

import de.swiftbyte.jdaboot.configuration.ConfigProvider;
import de.swiftbyte.jdaboot.configuration.PropertiesConfigProviderImpl;
import de.swiftbyte.jdaboot.configuration.YmlConfigProviderImpl;
import de.swiftbyte.jdaboot.variables.ResourceBundleTranslationProviderImpl;
import de.swiftbyte.jdaboot.variables.TranslationProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the configuration provider and translation provider for a class.
 * By default, the configuration provider is set to {@link PropertiesConfigProviderImpl} and the translation provider is set to {@link ResourceBundleTranslationProviderImpl}.
 * Another available configuration provider is {@link YmlConfigProviderImpl}, which can be used to read configuration from YAML files.
 *
 * @since alpha.4
 * @see ConfigProvider
 * @see TranslationProvider
 * @see PropertiesConfigProviderImpl
 * @see ResourceBundleTranslationProviderImpl
 * @see YmlConfigProviderImpl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoConfiguration {

    /**
     * Specifies the configuration provider class to be used.
     * By default, it is set to {@link PropertiesConfigProviderImpl}.
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

}
