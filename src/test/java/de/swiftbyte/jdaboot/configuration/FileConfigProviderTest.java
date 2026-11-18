package de.swiftbyte.jdaboot.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileConfigProviderTest {

    @TempDir
    Path resources;

    private ClassLoader previousClassLoader;
    private URLClassLoader resourceClassLoader;

    @BeforeEach
    void isolateConfigurationResources() throws IOException {
        previousClassLoader = Thread.currentThread().getContextClassLoader();
        // No parent: local ignored config files must never influence these tests.
        resourceClassLoader = new URLClassLoader(new URL[]{resources.toUri().toURL()}, null);
        Thread.currentThread().setContextClassLoader(resourceClassLoader);
    }

    @AfterEach
    void restoreContextClassLoader() throws IOException {
        Thread.currentThread().setContextClassLoader(previousClassLoader);
        if (resourceClassLoader != null) {
            resourceClassLoader.close();
        }
    }

    @ParameterizedTest(name = "{0}: profiles override values and retain base settings")
    @MethodSource("providers")
    void overlaysProfilesInOrder(String extension, Supplier<ConfigProvider> factory,
                                 String base, String dev, String local) throws IOException {
        Files.writeString(resources.resolve("config." + extension), base);
        Files.writeString(resources.resolve("config-dev." + extension), dev);
        Files.writeString(resources.resolve("config-local." + extension), local);
        ConfigProvider provider = factory.get();
        provider.setActiveProfiles(List.of("dev", "local"));

        provider.reload();

        assertAll(
                () -> assertEquals("local", provider.getString("app.name")),
                () -> assertEquals(8123, provider.getInt("app.port")),
                () -> assertTrue(provider.getBoolean("app.enabled")),
                () -> assertEquals("retained", provider.getString("app.base-only"))
        );
    }

    @ParameterizedTest(name = "{0}: absent files and profiles use explicit defaults")
    @MethodSource("providers")
    void handlesAbsentResources(String extension, Supplier<ConfigProvider> factory,
                                String base, String dev, String local) {
        ConfigProvider provider = factory.get();
        provider.setActiveProfiles(List.of("missing"));

        provider.reload();

        assertAll(
                () -> assertFalse(provider.hasKey("app.name")),
                () -> assertEquals("fallback", provider.getString("app.name", "fallback")),
                () -> assertEquals(9000, provider.getInt("app.port", 9000)),
                () -> assertTrue(provider.getBoolean("app.enabled", true))
        );
    }

    @ParameterizedTest(name = "{0}: reload removes values no longer present")
    @MethodSource("providers")
    void reloadsWithoutRetainingRemovedKeys(String extension, Supplier<ConfigProvider> factory,
                                            String base, String dev, String local) throws IOException {
        Path file = resources.resolve("config." + extension);
        Files.writeString(file, base);
        ConfigProvider provider = factory.get();
        provider.reload();
        assertTrue(provider.hasKey("app.base-only"));

        Files.writeString(file, local);
        provider.reload();

        assertAll(
                () -> assertEquals("local", provider.getString("app.name")),
                () -> assertFalse(provider.hasKey("app.base-only"))
        );
    }

    static Stream<Arguments> providers() {
        return Stream.of(
                Arguments.of("properties", (Supplier<ConfigProvider>) PropertiesConfigProviderImpl::new,
                        "app.name=base\napp.port=8123\napp.enabled=false\napp.base-only=retained\n",
                        "app.name=dev\napp.enabled=true\n",
                        "app.name=local\n"),
                Arguments.of("yml", (Supplier<ConfigProvider>) YmlConfigProviderImpl::new,
                        "app:\n  name: base\n  port: 8123\n  enabled: false\n  base-only: retained\n",
                        "app:\n  name: dev\n  enabled: true\n",
                        "app:\n  name: local\n")
        );
    }
}
