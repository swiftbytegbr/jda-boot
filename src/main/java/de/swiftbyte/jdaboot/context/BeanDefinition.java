package de.swiftbyte.jdaboot.context;

import java.util.List;
import java.util.Objects;

/**
 * Stores immutable metadata used by configuration binding, dependency resolution and lifecycle management.
 *
 * @param id                The stable identity of this definition.
 * @param name              The unique bean name.
 * @param offeredType       The declared type offered for dependency lookup.
 * @param creation          The creation operation and its typed dependencies.
 * @param phase             The creation phase.
 * @param managed           Whether the context owns the resulting object.
 * @param destroyMethod     The explicit cleanup method name, or an empty string.
 * @param defaultDefinition Whether the definition belongs to default configuration.
 * @param missingBeanTypes  The offered types that must be absent to enable this definition.
 * @param importPath        The discovery path from metadata root to declaration.
 * @since 1.0.0-beta.2
 */
public record BeanDefinition(String id, String name, Class<?> offeredType, BeanCreation creation,
                             BeanPhase phase, boolean managed, String destroyMethod,
                             boolean defaultDefinition, List<Class<?>> missingBeanTypes, List<Class<?>> importPath) {

    /**
     * Validates the definition identity and copies collection metadata.
     *
     * @param id                The stable identity of this definition.
     * @param name              The unique bean name.
     * @param offeredType       The declared type offered for dependency lookup.
     * @param creation          The creation operation and its typed dependencies.
     * @param phase             The creation phase.
     * @param managed           Whether the context owns the resulting object.
     * @param destroyMethod     The explicit cleanup method name, or an empty string.
     * @param defaultDefinition Whether the definition belongs to default configuration.
     * @param missingBeanTypes  The offered types that must be absent to enable this definition.
     * @param importPath        The discovery path from metadata root to declaration.
     * @throws NullPointerException     If required metadata is null.
     * @throws IllegalArgumentException If the identity or name is blank.
     * @since 1.0.0-beta.2
     */
    public BeanDefinition {

        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(offeredType);
        Objects.requireNonNull(creation);
        Objects.requireNonNull(phase);
        Objects.requireNonNull(destroyMethod);

        missingBeanTypes = List.copyOf(missingBeanTypes);
        importPath = List.copyOf(importPath);

        if (id.isBlank() || name.isBlank()) throw new IllegalArgumentException("Bean identity must not be blank");
    }

    /**
     * Returns a readable description of the declaring class or factory method.
     *
     * @return The declaration used for diagnostics.
     * @since 1.0.0-beta.2
     */
    public String origin() {
        if (creation instanceof BeanCreation.FactoryCreation factory) return factory.method().toGenericString();
        return offeredType.getName();
    }
}
