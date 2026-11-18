package de.swiftbyte.jdaboot.context;

import de.swiftbyte.jdaboot.exceptions.MetadataException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores a complete, immutable snapshot of discovered bean definitions.
 *
 * @param definitions     The ordered bean definitions, including conditional defaults.
 * @param discoveredTypes The inspected application classes and imported classes.
 * @since 1.0.0-beta.2
 */
public record BeanDefinitionRegistry(List<BeanDefinition> definitions, List<Class<?>> discoveredTypes) {

    /**
     * Copies the discovered metadata and rejects duplicate bean identities or names.
     *
     * @param definitions     The ordered bean definitions, including conditional defaults.
     * @param discoveredTypes The inspected application classes and imported classes.
     * @throws MetadataException If two definitions have the same identity or name.
     * @since 1.0.0-beta.2
     */
    public BeanDefinitionRegistry {

        definitions = List.copyOf(definitions);
        discoveredTypes = List.copyOf(discoveredTypes);

        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();

        for (BeanDefinition definition : definitions) {
            if (!ids.add(definition.id()) || !names.add(definition.name())) {
                if (definition.creation() instanceof BeanCreation.FactoryCreation factory) {
                    throw new MetadataException("Duplicate bean identity '" + definition.name() + "'", factory.method());
                }
                throw new MetadataException("Duplicate bean identity '" + definition.name() + "'", definition.offeredType());
            }
        }
    }

    /**
     * Finds definitions assignable to a required type using their declared offered types.
     * Conditional defaults remain included until dependency resolution evaluates their conditions.
     *
     * @param requiredType The type required by the consumer.
     * @return The immutable list of matching definitions.
     * @since 1.0.0-beta.2
     */
    public List<BeanDefinition> candidates(Class<?> requiredType) {
        return definitions.stream().filter(d -> requiredType.isAssignableFrom(d.offeredType())).toList();
    }
}
