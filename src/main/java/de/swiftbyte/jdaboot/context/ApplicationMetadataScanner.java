package de.swiftbyte.jdaboot.context;

import de.swiftbyte.jdaboot.exceptions.MetadataException;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Discovers application classes and reads a deterministic bean metadata snapshot.
 * Discovery does not initialize application classes or invoke constructors and factories.
 *
 * @since 1.0.0-beta.2
 */
public final class ApplicationMetadataScanner {

    /**
     * Creates a stateless scanner for application metadata discovery.
     *
     * @since 1.0.0-beta.2
     */
    public ApplicationMetadataScanner() {
    }

    /**
     * Scans the application package once and follows explicit imports without scanning their packages.
     *
     * @param mainClass The application entry class and mandatory metadata root.
     * @return The complete immutable metadata snapshot.
     * @throws MetadataException If a class cannot be inspected or a declaration is invalid.
     * @since 1.0.0-beta.2
     */
    public BeanDefinitionRegistry discover(Class<?> mainClass) {

        String packageName = mainClass.getPackageName();
        if (packageName.isEmpty()) throw new MetadataException("The main class must belong to a named package", mainClass);

        ClassLoader loader = mainClass.getClassLoader();
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setClassLoaders(new ClassLoader[]{loader})
                .setUrls(ClasspathHelper.forPackage(packageName, loader))
                .filterInputsBy(new FilterBuilder().includePackage(packageName))
                .setScanners(Scanners.SubTypes.filterResultsBy(name -> true))
                .setExpandSuperTypes(false));

        Set<String> names = new TreeSet<>();
        reflections.getStore().getOrDefault(Scanners.SubTypes.index(), Map.of()).forEach((parent, children) -> {
            names.add(parent);
            names.addAll(children);
        });

        List<Class<?>> classes = new ArrayList<>();
        for (String name : names) {
            if (!name.startsWith(packageName + ".")) continue;
            try {
                classes.add(Class.forName(name, false, loader));
            } catch (ClassNotFoundException | LinkageError e) {
                throw new MetadataException("Cannot inspect application class", name, e);
            }
        }

        return new BeanDefinitionReader().read(mainClass, classes);
    }

}
