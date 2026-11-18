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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanDefinitionReaderTest {
    private final BeanDefinitionReader reader = new BeanDefinitionReader();

    @Test
    void followsCyclicImportsAndDeduplicatesScanResults() {
        BeanDefinitionRegistry registry = reader.read(CycleA.class, List.of(CycleB.class, CycleA.class, CycleA.class));
        assertEquals(2, registry.definitions().size());
        assertEquals(List.of(CycleA.class, CycleB.class), registry.candidates(CycleB.class).get(0).importPath());
    }

    @Test
    void alwaysIncludesTheMainClassWithoutMakingAnOrdinaryMainABean() {
        assertTrue(reader.read(Root.class, List.of()).definitions().isEmpty());
        assertEquals(1, reader.read(Service.class, List.of()).definitions().size());
    }

    @Test
    void retainsTypedConstructorDependenciesWithoutConstructingObjects() {
        BeanDefinition definition = reader.read(Root.class, List.of(Consumer.class)).definitions().get(0);
        assertInstanceOf(BeanCreation.ConstructorCreation.class, definition.creation());
        ParameterizedType dependency = assertInstanceOf(ParameterizedType.class, definition.creation().dependencies().get(0));
        assertEquals(Service.class, dependency.getActualTypeArguments()[0]);
        assertTrue(definition.managed());
    }

    @Test
    void replacesAutomaticConcreteDefinitionsBeforeValidatingTheirConstructors() {
        BeanDefinitionRegistry registry = reader.read(Root.class, List.of(MultipleConstructors.class, Factories.class));
        List<BeanDefinition> offered = registry.candidates(MultipleConstructors.class);
        assertEquals(1, offered.size());
        assertInstanceOf(BeanCreation.FactoryCreation.class, offered.get(0).creation());
    }

    @Test
    void keepsExplicitFactoriesDistinctAndMatchesDeclaredInterfaces() {
        BeanDefinitionRegistry registry = reader.read(Root.class, List.of(TwoFactories.class));
        List<BeanDefinition> candidates = registry.candidates(Role.class);
        assertEquals(2, candidates.size());
        assertNotEquals(candidates.get(0).id(), candidates.get(1).id());
        assertTrue(registry.candidates(Service.class).isEmpty());
    }

    @Test
    void keepsTwoConcreteFactoriesWhileSuppressingAutomaticRegistration() {
        BeanDefinitionRegistry registry = reader.read(Root.class, List.of(Service.class, ConcreteFactories.class));
        List<BeanDefinition> definitions = registry.candidates(Service.class);
        assertEquals(2, definitions.size());
        assertTrue(definitions.stream().allMatch(d -> d.creation() instanceof BeanCreation.FactoryCreation));
    }

    @Test
    void recordsFactoryOwnerOwnershipPhaseAndConditions() {
        BeanDefinitionRegistry registry = reader.read(Root.class, List.of(DefaultFactories.class));
        BeanDefinition runtime = registry.candidates(Role.class).get(0);
        assertTrue(runtime.defaultDefinition());
        assertEquals(List.of(Role.class), runtime.missingBeanTypes());
        BeanCreation.FactoryCreation factory = assertInstanceOf(BeanCreation.FactoryCreation.class, runtime.creation());
        assertEquals(DefaultFactories.class, factory.ownerType().orElseThrow());
        BeanDefinition bootstrap = registry.candidates(String.class).get(0);
        assertEquals(BeanPhase.BOOTSTRAP, bootstrap.phase());
        assertFalse(bootstrap.managed());
        assertTrue(((BeanCreation.FactoryCreation) bootstrap.creation()).ownerType().isEmpty());
    }

    @Test
    void registersSettingsForLaterBinding() {
        BeanDefinitionRegistry registry = reader.read(Root.class, List.of(Settings.class));
        BeanCreation.SettingsCreation creation = assertInstanceOf(BeanCreation.SettingsCreation.class, registry.definitions().get(
                0).creation());
        assertEquals("app.tickets", creation.prefix());
    }

    @Test
    void producesImmutableDeterministicIndependentSnapshots() {
        BeanDefinitionRegistry first = reader.read(Root.class, List.of(Service.class, Consumer.class));
        BeanDefinitionRegistry second = reader.read(Root.class, List.of(Consumer.class, Service.class));
        assertEquals(first, second);
        assertNotSame(first, second);
        assertThrows(UnsupportedOperationException.class, () -> first.definitions().clear());
        assertThrows(UnsupportedOperationException.class, () -> first.discoveredTypes().clear());
        assertThrows(UnsupportedOperationException.class, () -> first.definitions().get(0).importPath().clear());
    }

    static Stream<Class<?>> handlers() {
        return Stream.of(Command.class, Button.class, Modal.class, XmlModal.class, EntityMenu.class, StringMenu.class,
                Console.class);
    }

    @ParameterizedTest
    @MethodSource("handlers")
    void handlerDefinitionsImplyOneComponent(Class<?> handler) {
        assertEquals(1, reader.read(Root.class, List.of(handler)).candidates(handler).size());
    }

    static Stream<Class<?>> invalidDeclarations() {
        return Stream.of(MultipleConstructors.class, AbstractComponent.class, InnerComponent.class,
                InvalidFactory.class, InvalidBootstrap.class, InvalidCondition.class, InvalidOwnership.class,
                InvalidSettings.class, InvalidImport.class, DuplicateNames.class, OrphanFactory.class);
    }

    @ParameterizedTest
    @MethodSource("invalidDeclarations")
    void rejectsInvalidDeclarationsWithAnOrigin(Class<?> invalid) {
        MetadataException error = assertThrows(MetadataException.class, () -> reader.read(Root.class, List.of(invalid)));
        assertTrue(error.getMessage().contains(invalid.getName()), error.getMessage());
    }

    static class Root {
    }

    interface Role {
    }

    @Component
    static class Service implements Role {
    }

    @Component
    static class Consumer {
        Consumer(List<Service> services) {
            throw new AssertionError("Discovery must not construct beans");
        }
    }

    @Component
    @Import(CycleB.class)
    static class CycleA {
    }

    @Component
    @Import(CycleA.class)
    static class CycleB {
    }

    @Component
    static class MultipleConstructors {
        MultipleConstructors() {
        }

        MultipleConstructors(String value) {
        }
    }

    @Configuration
    static class Factories {
        @Bean
        MultipleConstructors multiple() {
            throw new AssertionError("Discovery must not invoke factories");
        }
    }

    @Configuration
    static class ConcreteFactories {
        @Bean
        Service firstService() {
            throw new AssertionError();
        }

        @Bean
        Service secondService() {
            throw new AssertionError();
        }
    }

    @Configuration
    static class TwoFactories {
        @Bean
        Role first() {
            throw new AssertionError();
        }

        @Bean
        Role second() {
            throw new AssertionError();
        }
    }

    @AutoConfiguration
    static class DefaultFactories {
        @Bean
        @ConditionalOnMissingBean(Role.class)
        Role role() {
            throw new AssertionError();
        }

        @Bean(phase = BeanPhase.BOOTSTRAP, managed = false)
        static String early() {
            throw new AssertionError();
        }
    }

    @ConfigurationProperties(prefix = "app.tickets")
    record Settings(String token) {
    }

    @Component
    @SlashCommandDefinition(name = "test", description = "Test", type = SlashCommandDefinition.Type.SLASH)
    static class Command {
    }

    @ButtonDefinition(label = "Test", type = ButtonDefinition.Type.PRIMARY)
    static class Button {
    }

    @ModalDefinition(title = "Test")
    static class Modal {
    }

    @XmlModalDefinition
    static class XmlModal {
    }

    @EntitySelectMenuDefinition
    static class EntityMenu {
    }

    @StringSelectMenuDefinition(options = {})
    static class StringMenu {
    }

    @ConsoleCommandDefinition(name = "test")
    static class Console {
    }

    @Component
    abstract static class AbstractComponent {
    }

    @Component
    class InnerComponent {
    }

    @Configuration
    static class InvalidFactory {
        @Bean
        void invalid() {
        }
    }

    @Configuration
    static class InvalidBootstrap {
        @Bean(phase = BeanPhase.BOOTSTRAP)
        String invalid() {
            return "";
        }
    }

    @Configuration
    static class InvalidCondition {
        @Bean
        @ConditionalOnMissingBean(Role.class)
        String invalid() {
            return "";
        }
    }

    @Configuration
    static class InvalidOwnership {
        @Bean(managed = false, destroyMethod = "close")
        String invalid() {
            return "";
        }
    }

    @Component
    @ConfigurationProperties(prefix = "app")
    static class InvalidSettings {
    }

    @Import(String.class)
    static class InvalidImport {
    }

    @Configuration
    static class DuplicateNames {
        @Bean("duplicate")
        String first() {
            return "";
        }

        @Bean("duplicate")
        String second() {
            return "";
        }
    }

    static class OrphanFactory {
        @Bean
        String invalid() {
            return "";
        }
    }
}
