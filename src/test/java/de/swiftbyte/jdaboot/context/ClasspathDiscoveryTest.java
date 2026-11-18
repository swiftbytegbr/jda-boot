package de.swiftbyte.jdaboot.context;

import de.swiftbyte.jdaboot.context.fixtures.application.DiscoveryApplication;
import de.swiftbyte.jdaboot.context.fixtures.external.ExternalConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClasspathDiscoveryTest {
    @Test
    void discoversDefinitionsInsideAPackagedJar(@TempDir Path directory) throws Exception {
        String packageName = "example.packaged";
        Path source = directory.resolve("JarMain.java");
        Files.writeString(source, """
                package example.packaged;
                @de.swiftbyte.jdaboot.annotation.context.Component
                public class JarMain {
                    static { rejectInitialization(); }
                    private static void rejectInitialization() { throw new AssertionError("Initialized during discovery"); }
                }
                """);
        Path output = Files.createDirectory(directory.resolve("classes"));
        int result = ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "-proc:none", "-classpath", System.getProperty("java.class.path"), "-d", output.toString(), source.toString());
        assertEquals(0, result);
        Path jar = directory.resolve("application.jar");
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jar))) {
            for (String name : new String[]{"example/", "example/packaged/", "example/packaged/JarMain.class"}) {
                stream.putNextEntry(new JarEntry(name));
                if (name.endsWith(".class")) Files.copy(output.resolve(name), stream);
                stream.closeEntry();
            }
        }
        try (URLClassLoader loader = new URLClassLoader(new java.net.URL[]{jar.toUri().toURL()}, getClass().getClassLoader())) {
            Class<?> main = Class.forName(packageName + ".JarMain", false, loader);
            BeanDefinitionRegistry registry = new ApplicationMetadataScanner().discover(main);
            assertEquals(1, registry.definitions().size());
            assertSame(main, registry.definitions().get(0).offeredType());
        }
    }

    @Test
    void scansOnlyTheApplicationPackageAndImportsExternalDefinitionsWithoutInitialization() {
        BeanDefinitionRegistry registry = new ApplicationMetadataScanner().discover(DiscoveryApplication.class);
        assertEquals(3, registry.definitions().size());
        assertEquals(1, registry.candidates(DiscoveryApplication.LocalService.class).size());
        assertEquals(1, registry.candidates(ExternalConfiguration.class).size());
        assertEquals(1, registry.candidates(String.class).size());
        assertTrue(registry.candidates(ExternalConfiguration.UnimportedNeighbour.class).isEmpty());
    }
}
