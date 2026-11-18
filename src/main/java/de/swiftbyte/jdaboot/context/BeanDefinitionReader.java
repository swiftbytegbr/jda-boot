package de.swiftbyte.jdaboot.context;

import de.swiftbyte.jdaboot.exceptions.MetadataException;

import de.swiftbyte.jdaboot.annotation.context.AutoConfiguration;
import de.swiftbyte.jdaboot.annotation.context.Bean;
import de.swiftbyte.jdaboot.annotation.context.Component;
import de.swiftbyte.jdaboot.annotation.context.ConditionalOnMissingBean;
import de.swiftbyte.jdaboot.annotation.context.Configuration;
import de.swiftbyte.jdaboot.annotation.configuration.ConfigurationProperties;
import de.swiftbyte.jdaboot.annotation.context.Import;
import de.swiftbyte.jdaboot.annotation.cli.ConsoleCommandDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.button.ButtonDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.command.SlashCommandDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.modal.ModalDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.modal.XmlModalDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.selection.EntitySelectMenuDefinition;
import de.swiftbyte.jdaboot.annotation.interaction.selection.StringSelectMenuDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Reads bean definitions from discovered application classes.
 * Imports are followed and declarations are validated without constructing application objects.
 *
 * @since 1.0.0-beta.2
 */
final class BeanDefinitionReader {
    /** Component and handler markers recognized by automatic registration. */
    private static final List<Class<? extends Annotation>> COMPONENT_MARKERS = List.of(
            Component.class, Configuration.class, AutoConfiguration.class, ConsoleCommandDefinition.class,
            ButtonDefinition.class, SlashCommandDefinition.class, ModalDefinition.class, XmlModalDefinition.class,
            EntitySelectMenuDefinition.class, StringSelectMenuDefinition.class);

    /**
     * Creates a stateless reader for discovered bean declarations.
     *
     * @since 1.0.0-beta.2
     */
    BeanDefinitionReader() {
    }

    /**
     * Reads definitions and import metadata from a completed classpath scan.
     *
     * @param mainClass    The application class included regardless of scan results.
     * @param scannedTypes The application classes found by the classpath scanner.
     * @return The immutable definition snapshot.
     * @throws MetadataException If a declaration or bean identity is invalid.
     * @since 1.0.0-beta.2
     */
    BeanDefinitionRegistry read(Class<?> mainClass, Collection<Class<?>> scannedTypes) {

        Map<Class<?>, List<Class<?>>> paths = collectImportPaths(mainClass, scannedTypes);
        List<BeanDefinition> factories = readFactories(paths);
        Set<Class<?>> replaced = findReplacedTypes(factories);
        List<BeanDefinition> definitions = readAutomaticDefinitions(paths, replaced);

        definitions.addAll(factories);
        definitions.sort(Comparator.comparing(BeanDefinition::defaultDefinition).thenComparing(BeanDefinition::id));

        List<Class<?>> discoveredTypes = paths.keySet().stream().sorted(Comparator.comparing(Class::getName)).toList();

        return new BeanDefinitionRegistry(definitions, discoveredTypes);
    }

    /**
     * Collects application classes and the first import path to each class.
     *
     * @param mainClass    The mandatory application metadata root.
     * @param scannedTypes The classes found by the classpath scanner.
     * @return The classes in discovery order and their import paths.
     * @throws MetadataException If a declaration is invalid.
     * @since 1.0.0-beta.2
     */
    private static Map<Class<?>, List<Class<?>>> collectImportPaths(Class<?> mainClass, Collection<Class<?>> scannedTypes) {

        Objects.requireNonNull(mainClass);
        Map<Class<?>, List<Class<?>>> paths = new LinkedHashMap<>();

        visit(mainClass, List.of(mainClass), paths);

        scannedTypes.stream().sorted(Comparator.comparing(Class::getName))
                .forEach(type -> visit(type, List.of(type), paths));

        return paths;
    }

    /**
     * Reads explicitly declared factory methods in a deterministic order.
     *
     * @param paths The discovered classes and their import paths.
     * @return The explicit factory definitions.
     * @throws MetadataException If a declaration is invalid.
     * @since 1.0.0-beta.2
     */
    private static List<BeanDefinition> readFactories(Map<Class<?>, List<Class<?>>> paths) {

        List<BeanDefinition> factories = new ArrayList<>();
        for (Class<?> type : paths.keySet()) {

            List<Method> methods = Arrays.stream(type.getDeclaredMethods())
                    .sorted(Comparator.comparing(Method::toGenericString)).toList();
            for (Method method : methods) {

                Bean bean = method.getDeclaredAnnotation(Bean.class);
                if (bean == null) {
                    if (method.isAnnotationPresent(ConditionalOnMissingBean.class)) {
                        throw new MetadataException("Missing @Bean", method);
                    }
                    continue;
                }

                validateFactory(method, bean);
                factories.add(createFactoryDefinition(method, bean, paths.get(type)));
            }
        }

        return factories;
    }

    /**
     * Validates the configuration owner, signature, phase and ownership of a factory.
     *
     * @param method The declared factory method.
     * @param bean   The factory annotation.
     * @throws MetadataException If a declaration is invalid.
     * @since 1.0.0-beta.2
     */
    private static void validateFactory(Method method, Bean bean) {

        Class<?> type = method.getDeclaringClass();

        if (!isConfiguration(type)) throw new MetadataException("@Bean requires @Configuration or @AutoConfiguration", method);

        boolean isStatic = Modifier.isStatic(method.getModifiers());
        if (Modifier.isAbstract(method.getModifiers()) || method.isBridge() || method.isSynthetic() || method
                .getReturnType() == void.class || method.getReturnType().isPrimitive() || method
                        .getTypeParameters().length != 0) {
            throw new MetadataException("Invalid bean factory", method);
        }

        if (bean.phase() == BeanPhase.BOOTSTRAP && !isStatic) {
            throw new MetadataException("BOOTSTRAP factories must be static", method);
        }

        if (!bean.managed() && !bean.destroyMethod().isEmpty()) {
            throw new MetadataException("Externally managed beans cannot declare a destroy method", method);
        }

        ConditionalOnMissingBean condition = method.getDeclaredAnnotation(ConditionalOnMissingBean.class);
        if (condition != null && (!type.isAnnotationPresent(AutoConfiguration.class) || condition.value().length == 0)) {
            throw new MetadataException("Missing-bean conditions require default configuration and explicit types",
                    method);
        }
    }

    /**
     * Creates metadata for a validated factory declaration.
     *
     * @param method The validated factory method.
     * @param bean   The factory annotation.
     * @param path   The import path to the declaring configuration class.
     * @return The factory definition.
     * @since 1.0.0-beta.2
     */
    private static BeanDefinition createFactoryDefinition(Method method, Bean bean, List<Class<?>> path) {

        Class<?> type = method.getDeclaringClass();
        boolean isStatic = Modifier.isStatic(method.getModifiers());

        ConditionalOnMissingBean condition = method.getDeclaredAnnotation(ConditionalOnMissingBean.class);
        String name = bean.value().isEmpty() ? method.getName() : bean.value();

        return new BeanDefinition("factory:" + method.toGenericString(), name, method.getReturnType(),
                new BeanCreation.FactoryCreation(method, isStatic ? Optional.empty() : Optional.of(type)),
                bean.phase(), bean.managed(), bean.destroyMethod(), type.isAnnotationPresent(AutoConfiguration.class),
                condition == null ? List.of() : List.of(condition.value()), path);
    }

    /**
     * Finds automatic class definitions replaced by explicit concrete factories.
     *
     * @param factories The explicit factory definitions.
     * @return The concrete types supplied by application factories.
     * @since 1.0.0-beta.2
     */
    private static Set<Class<?>> findReplacedTypes(List<BeanDefinition> factories) {

        Set<Class<?>> replaced = new HashSet<>();
        for (BeanDefinition factory : factories) {

            Class<?> type = factory.offeredType();
            if (!factory.defaultDefinition() && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
                replaced.add(type);
            }
        }

        return replaced;
    }

    /**
     * Reads automatic component and settings definitions not replaced by factories.
     *
     * @param paths    The discovered classes and their import paths.
     * @param replaced The concrete types supplied by explicit application factories.
     * @return The automatic class definitions.
     * @throws MetadataException If a declaration is invalid.
     * @since 1.0.0-beta.2
     */
    private static List<BeanDefinition> readAutomaticDefinitions(Map<Class<?>, List<Class<?>>> paths, Set<Class<?>> replaced) {

        List<BeanDefinition> definitions = new ArrayList<>();
        for (Class<?> type : paths.keySet()) {

            boolean component = isComponent(type);
            ConfigurationProperties settings = type.getDeclaredAnnotation(ConfigurationProperties.class);

            if (!component && settings == null) continue;
            if (component && settings != null) throw new MetadataException("Settings cannot also be a component", type);
            if (replaced.contains(type)) continue;

            validateConcrete(type);
            BeanCreation creation = readClassCreation(type);
            Component marker = type.getDeclaredAnnotation(Component.class);
            String name = marker == null || marker.value().isEmpty() ? type.getName() : marker.value();

            definitions.add(new BeanDefinition(classId(type), name, type, creation, BeanPhase.RUNTIME, true, "",
                    type.isAnnotationPresent(AutoConfiguration.class), List.of(), paths.get(type)));
        }
        return definitions;
    }

    /**
     * Determines settings binding or the single constructor used for automatic creation.
     *
     * @param type The automatic component or settings class.
     * @return The creation metadata for the class.
     * @throws MetadataException If a declaration is invalid.
     * @since 1.0.0-beta.2
     */
    private static BeanCreation readClassCreation(Class<?> type) {

        ConfigurationProperties settings = type.getDeclaredAnnotation(ConfigurationProperties.class);

        if (settings != null) return new BeanCreation.SettingsCreation(settings.prefix());

        Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length != 1) {
            throw new MetadataException("Automatic beans require exactly one constructor; use @Bean", type);
        }

        return new BeanCreation.ConstructorCreation(constructors[0]);
    }

    /**
     * Visits a metadata root and follows its imports exactly once.
     *
     * @param type  The class to inspect.
     * @param path  The import path to this class.
     * @param paths The collected classes and their first discovery paths.
     * @throws MetadataException If an import does not declare a supported definition.
     * @since 1.0.0-beta.2
     */
    private static void visit(Class<?> type, List<Class<?>> path, Map<Class<?>, List<Class<?>>> paths) {

        if (paths.putIfAbsent(type, List.copyOf(path)) != null) return;

        Import imports = type.getDeclaredAnnotation(Import.class);
        if (imports == null) return;

        for (Class<?> imported : Arrays.stream(imports.value()).sorted(Comparator.comparing(Class::getName)).toList()) {

            if (!isComponent(imported) && !imported.isAnnotationPresent(ConfigurationProperties.class)) {
                throw new MetadataException("@Import target has no supported definition: " + imported.getName(), type);
            }

            List<Class<?>> next = new ArrayList<>(path);
            next.add(imported);
            visit(imported, next, paths);
        }
    }

    /**
     * Checks whether a class declares application or default configuration.
     *
     * @param type The class to inspect.
     * @return True if the class is a configuration owner.
     * @since 1.0.0-beta.2
     */
    private static boolean isConfiguration(Class<?> type) {
        return type.isAnnotationPresent(Configuration.class) || type.isAnnotationPresent(AutoConfiguration.class);
    }

    /**
     * Checks the supported component and handler definition annotations.
     *
     * @param type The class to inspect.
     * @return True if the class declares an automatic component.
     * @since 1.0.0-beta.2
     */
    private static boolean isComponent(Class<?> type) {
        return COMPONENT_MARKERS.stream().anyMatch(type::isAnnotationPresent);
    }

    /**
     * Validates that a class can be constructed without an enclosing object.
     *
     * @param type The declared bean class.
     * @throws MetadataException If the class is not independently constructible.
     * @since 1.0.0-beta.2
     */
    private static void validateConcrete(Class<?> type) {
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers()) || type.isEnum() || type.isAnonymousClass() || type
                .isLocalClass() || (type.isMemberClass() && !Modifier.isStatic(type.getModifiers()))) {
            throw new MetadataException("Bean class must be concrete and independently constructible", type);
        }
    }

    /**
     * Builds the stable identity of an automatic class definition.
     *
     * @param type The declared bean class.
     * @return The identity derived from the fully qualified class name.
     * @since 1.0.0-beta.2
     */
    private static String classId(Class<?> type) {
        return "class:" + type.getName();
    }
}
