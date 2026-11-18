package de.swiftbyte.jdaboot.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * Describes how a bean will be created without constructing an object or invoking user code.
 *
 * @since 1.0.0-beta.2
 */
public sealed interface BeanCreation {

    /**
     * Returns the generic parameter types required by the creation operation.
     *
     * @return The immutable list of constructor or factory parameter types.
     * @since 1.0.0-beta.2
     */
    List<Type> dependencies();

    /**
     * Describes automatic construction of a component.
     *
     * @param constructor The single constructor selected for automatic creation.
     * @since 1.0.0-beta.2
     */
    record ConstructorCreation(Constructor<?> constructor) implements BeanCreation {

        /**
         * Returns the generic constructor parameter types.
         *
         * @return The immutable list of constructor dependencies.
         * @since 1.0.0-beta.2
         */
        @Override
        public List<Type> dependencies() {
            return List.of(constructor.getGenericParameterTypes());
        }
    }

    /**
     * Describes creation through a configuration factory method.
     *
     * @param method    The declared factory method.
     * @param ownerType The configuration owner type, absent for a static factory.
     * @since 1.0.0-beta.2
     */
    record FactoryCreation(Method method, Optional<Class<?>> ownerType) implements BeanCreation {

        /**
         * Returns factory parameter types. The configuration owner is recorded separately.
         *
         * @return The immutable list of factory parameter dependencies.
         * @since 1.0.0-beta.2
         */
        @Override
        public List<Type> dependencies() {
            return List.of(method.getGenericParameterTypes());
        }
    }

    /**
     * Describes creation of a settings object by the configuration binder.
     *
     * @param prefix The configuration key prefix for the settings object.
     * @since 1.0.0-beta.2
     */
    record SettingsCreation(String prefix) implements BeanCreation {

        /**
         * Returns the bean dependencies of settings creation. Binding uses the configuration environment.
         *
         * @return An empty dependency list.
         * @since 1.0.0-beta.2
         */
        @Override
        public List<Type> dependencies() {
            return List.of();
        }
    }
}
