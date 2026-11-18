package de.swiftbyte.jdaboot.variables;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlaceholderEngineTest {

    @Test
    void resolvesNestedKeysAndTemplateReferences() {
        Map<String, String> values = Map.of(
                "language", "en",
                "greeting.en", "Hello ${name}",
                "name", "Ada"
        );
        PlaceholderEngine engine = PlaceholderEngine.builder()
                .resolver('$', values::get)
                .build();

        assertEquals("Hello Ada!", engine.resolve("${greeting.${language}}!"));
    }

    @Test
    void keepsPrefixResolversIndependent() {
        PlaceholderEngine engine = PlaceholderEngine.builder()
                .resolver('$', Map.of("name", "Ada")::get)
                .resolver('#', Map.of("status", "ready")::get)
                .resolver('?', Map.of("app.version", "1.0")::get)
                .build();

        assertEquals("Ada: ready (1.0)",
                engine.resolve("${name}: #{status} (?{app.version})"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plain text",
            "before ${missing} after",
            "${not-closed",
            "#{unregistered}",
            "${outer.${missing}}"
    })
    void preservesUnknownAndUnclosedPlaceholders(String text) {
        PlaceholderEngine engine = PlaceholderEngine.builder()
                .resolver('$', key -> null)
                .build();

        assertEquals(text, engine.resolve(text));
    }

    @Test
    void insertsRegexMetacharactersWithoutReplacementSemantics() {
        String value = "$5 \\ path \\ ${unknown}";
        PlaceholderEngine engine = PlaceholderEngine.builder()
                .resolver('$', Map.of("value", value)::get)
                .build();

        assertEquals("Value: " + value, engine.resolve("Value: ${value}"));
    }

    @Test
    void reportsTheReferencePathForCyclesAcrossPrefixes() {
        PlaceholderEngine engine = PlaceholderEngine.builder()
                .resolver('$', Map.of("first", "#{second}")::get)
                .resolver('#', Map.of("second", "${first}")::get)
                .build();

        var exception = assertThrows(PlaceholderEngine.PlaceholderCycleException.class,
                () -> engine.resolve("${first}"));

        assertEquals(List.of("${first}", "#{second}", "${first}"),
                exception.getCycle().stream()
                        .map(PlaceholderEngine.PlaceholderReference::token)
                        .toList());
    }

    @Test
    void limitsRecursiveReferenceResolution() {
        PlaceholderEngine engine = PlaceholderEngine.builder()
                .maxDepth(2)
                .resolver('$', Map.of(
                        "first", "${second}",
                        "second", "${third}",
                        "third", "done"
                )::get)
                .build();

        var exception = assertThrows(PlaceholderEngine.PlaceholderDepthException.class,
                () -> engine.resolve("${first}"));

        assertEquals(2, exception.getMaxDepth());
        assertEquals(List.of("${first}", "${second}", "${third}"),
                exception.getResolutionPath().stream()
                        .map(PlaceholderEngine.PlaceholderReference::token)
                        .toList());
    }

    @Test
    void cachesRepeatedReferencesOnlyWithinOneResolution() {
        AtomicInteger calls = new AtomicInteger();
        PlaceholderEngine engine = PlaceholderEngine.builder()
                .resolver('$', key -> Integer.toString(calls.incrementAndGet()))
                .build();

        assertEquals("1/1", engine.resolve("${value}/${value}"));
        assertEquals("2/2", engine.resolve("${value}/${value}"));
        assertEquals(2, calls.get());
    }

    @Test
    void canResolveRepeatedDynamicValuesWithoutCaching() {
        AtomicInteger calls = new AtomicInteger();
        PlaceholderEngine engine = PlaceholderEngine.builder()
                .cacheResults(false)
                .resolver('$', key -> Integer.toString(calls.incrementAndGet()))
                .build();

        assertEquals("1/2", engine.resolve("${value}/${value}"));
    }
}
