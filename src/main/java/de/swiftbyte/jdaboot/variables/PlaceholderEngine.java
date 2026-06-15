package de.swiftbyte.jdaboot.variables;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves nested placeholders using a tree and independently registered prefix resolvers.
 * A registered prefix {@code '$'} represents placeholders using the syntax {@code ${content}}.
 *
 * @since 1.0.0-beta.2
 */
public final class PlaceholderEngine {

    private static final int DEFAULT_MAX_DEPTH = 64;

    private final @NonNull Map<Character, PlaceholderResolver> resolvers;
    private final int maxDepth;
    private final boolean cacheResults;

    /**
     * Creates a placeholder engine.
     *
     * @param resolvers    The resolvers indexed by placeholder prefix.
     * @param maxDepth     The maximum nested resolution depth.
     * @param cacheResults Whether resolved placeholders are cached for one call.
     * @since 1.0.0-beta.2
     */
    private PlaceholderEngine(@NonNull Map<Character, PlaceholderResolver> resolvers, int maxDepth, boolean cacheResults) {
        this.resolvers = Map.copyOf(resolvers);
        this.maxDepth = maxDepth;
        this.cacheResults = cacheResults;
    }

    /**
     * Creates a new engine builder.
     *
     * @return A new builder.
     * @since 1.0.0-beta.2
     */
    public static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Resolves all known placeholders in the supplied text.
     * Unknown placeholders are preserved unchanged.
     *
     * @param text The source text.
     * @return The resolved text.
     * @throws PlaceholderCycleException If a cyclic placeholder reference is detected.
     * @throws PlaceholderDepthException If the configured maximum resolution depth is exceeded.
     * @since 1.0.0-beta.2
     */
    public @NonNull String resolve(@NonNull String text) {
        ResolutionContext context = new ResolutionContext();
        return parse(text).evaluate(context, 0).text();
    }

    /**
     * Parses text into a placeholder tree.
     *
     * @param text The source text.
     * @return The parsed root node.
     * @since 1.0.0-beta.2
     */
    private @NonNull ContainerNode parse(@NonNull String text) {

        ContainerNode root = new ContainerNode();
        Deque<PlaceholderNode> placeholders = new ArrayDeque<>();
        ContainerNode current = root;

        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (resolvers.containsKey(character) && index + 1 < text.length() && text.charAt(index + 1) == '{') {
                PlaceholderNode placeholder = new PlaceholderNode(character);
                current.children.add(placeholder);
                placeholders.push(placeholder);
                current = placeholder;
                index++;
                continue;
            }

            if (character == '}' && !placeholders.isEmpty()) {
                PlaceholderNode placeholder = placeholders.pop();
                placeholder.closed = true;
                current = placeholders.isEmpty() ? root : placeholders.peek();
                continue;
            }

            current.append(character);
        }
        return root;
    }

    /**
     * Resolves a placeholder and recursively evaluates its replacement tree.
     *
     * @param prefix  The placeholder prefix.
     * @param key     The resolved placeholder content.
     * @param context The current resolution context.
     * @param depth   The current resolution depth.
     * @return The evaluated replacement, or an unresolved result.
     * @since 1.0.0-beta.2
     */
    private @NonNull EvaluationResult resolvePlaceholder(char prefix, @NonNull String key, @NonNull ResolutionContext context, int depth) {

        PlaceholderReference reference = new PlaceholderReference(prefix, key);
        EvaluationResult cached = cacheResults ? context.cache.get(reference) : null;

        if (cached != null) return cached;

        if (context.activeReferenceSet.contains(reference)) {
            throw new PlaceholderCycleException(createCycle(context.activeReferences, reference));
        }
        if (depth >= maxDepth) {
            throw new PlaceholderDepthException(maxDepth, List.copyOf(context.activeReferences), reference);
        }

        PlaceholderResolver resolver = resolvers.get(prefix);
        if (resolver == null) {
            return EvaluationResult.unresolved(reference.token());
        }

        context.activeReferences.addLast(reference);
        context.activeReferenceSet.add(reference);

        try {
            String replacement = resolver.resolve(key);
            if (replacement == null) {
                return EvaluationResult.unresolved(reference.token());
            }

            EvaluationResult result = parse(replacement).evaluate(context, depth + 1);
            if (cacheResults) {
                context.cache.put(reference, result);
            }
            return result;
        } finally {
            context.activeReferences.removeLast();
            context.activeReferenceSet.remove(reference);
        }
    }

    /**
     * Resolves placeholder content.
     *
     * @since 1.0.0-beta.2
     */
    @FunctionalInterface
    public interface PlaceholderResolver {

        /**
         * Resolves placeholder content.
         *
         * @param content The fully evaluated placeholder content.
         * @return The replacement text, or {@code null} to preserve the placeholder.
         * @since 1.0.0-beta.2
         */
        @Nullable String resolve(@NonNull String content);
    }

    /**
     * Identifies one resolved placeholder.
     *
     * @param prefix  The placeholder prefix.
     * @param content The resolved placeholder content.
     * @since 1.0.0-beta.2
     */
    public record PlaceholderReference(char prefix, @NonNull String content) {

        /**
         * Returns the complete placeholder token.
         *
         * @return The placeholder token.
         * @since 1.0.0-beta.2
         */
        public @NonNull String token() {
            return prefix + "{" + content + "}";
        }
    }

    /**
     * Builds a {@link PlaceholderEngine}.
     *
     * @since 1.0.0-beta.2
     */
    public static final class Builder {
        private final Map<Character, PlaceholderResolver> resolvers = new LinkedHashMap<>();
        private int maxDepth = DEFAULT_MAX_DEPTH;
        private boolean cacheResults = true;

        /**
         * Registers or replaces the resolver for a placeholder prefix.
         *
         * @param prefix   The prefix used by placeholders such as {@code ${content}}.
         * @param resolver The resolver invoked for this prefix.
         * @return This builder.
         * @since 1.0.0-beta.2
         */
        public @NonNull Builder resolver(char prefix, @NonNull PlaceholderResolver resolver) {
            if (prefix == '{' || prefix == '}') {
                throw new IllegalArgumentException("Placeholder prefix cannot be '{' or '}'");
            }
            resolvers.put(prefix, resolver);
            return this;
        }

        /**
         * Sets the maximum nested placeholder resolution depth.
         *
         * @param maxDepth The maximum depth, greater than zero.
         * @return This builder.
         * @since 1.0.0-beta.2
         */
        public @NonNull Builder maxDepth(int maxDepth) {
            if (maxDepth <= 0) {
                throw new IllegalArgumentException("Maximum placeholder depth must be greater than zero");
            }
            this.maxDepth = maxDepth;
            return this;
        }

        /**
         * Configures result caching for a single {@link PlaceholderEngine#resolve(String)} call.
         *
         * @param cacheResults Whether repeated placeholder references should reuse their result.
         * @return This builder.
         * @since 1.0.0-beta.2
         */
        public @NonNull Builder cacheResults(boolean cacheResults) {
            this.cacheResults = cacheResults;
            return this;
        }

        /**
         * Builds the configured engine.
         *
         * @return The placeholder engine.
         * @since 1.0.0-beta.2
         */
        public @NonNull PlaceholderEngine build() {
            return new PlaceholderEngine(resolvers, maxDepth, cacheResults);
        }
    }

    /**
     * Indicates a cyclic placeholder reference.
     *
     * @since 1.0.0-beta.2
     */
    public static final class PlaceholderCycleException extends IllegalStateException {
        private final @NonNull List<@NonNull PlaceholderReference> cycle;

        /**
         * Creates a cycle exception.
         *
         * @param cycle The detected reference cycle.
         * @since 1.0.0-beta.2
         */
        private PlaceholderCycleException(@NonNull List<@NonNull PlaceholderReference> cycle) {
            super("Detected cyclic placeholder references: " + formatReferences(cycle));
            this.cycle = List.copyOf(cycle);
        }

        /**
         * Returns the detected cycle.
         *
         * @return The placeholder reference cycle.
         * @since 1.0.0-beta.2
         */
        public @NonNull List<@NonNull PlaceholderReference> getCycle() {
            return cycle;
        }
    }

    /**
     * Indicates that placeholder resolution exceeded the configured depth.
     *
     * @since 1.0.0-beta.2
     */
    public static final class PlaceholderDepthException extends IllegalStateException {
        private final int maxDepth;
        private final @NonNull List<@NonNull PlaceholderReference> resolutionPath;

        /**
         * Creates a depth exception.
         *
         * @param maxDepth         The configured maximum depth.
         * @param activePath       The active resolution path.
         * @param currentReference The reference that exceeded the limit.
         * @since 1.0.0-beta.2
         */
        private PlaceholderDepthException(int maxDepth,
                                          @NonNull List<@NonNull PlaceholderReference> activePath,
                                          @NonNull PlaceholderReference currentReference) {
            super("Placeholder resolution exceeded maximum depth of " + maxDepth + ": "
                    + formatReferences(appendReference(activePath, currentReference)));
            this.maxDepth = maxDepth;
            this.resolutionPath = appendReference(activePath, currentReference);
        }

        /**
         * Returns the configured maximum depth.
         *
         * @return The maximum depth.
         * @since 1.0.0-beta.2
         */
        public int getMaxDepth() {
            return maxDepth;
        }

        /**
         * Returns the resolution path that exceeded the limit.
         *
         * @return The resolution path.
         * @since 1.0.0-beta.2
         */
        public @NonNull List<@NonNull PlaceholderReference> getResolutionPath() {
            return resolutionPath;
        }
    }

    /**
     * Node of a parsed placeholder tree.
     *
     * @since 1.0.0-beta.2
     */
    private interface Node {

        /**
         * Evaluates this node.
         *
         * @param context The resolution context.
         * @param depth   The current resolution depth.
         * @return The evaluated node.
         * @since 1.0.0-beta.2
         */
        @NonNull EvaluationResult evaluate(@NonNull ResolutionContext context, int depth);
    }

    /**
     * Contains an ordered list of tree nodes.
     *
     * @since 1.0.0-beta.2
     */
    private static class ContainerNode implements Node {
        private final List<Node> children = new ArrayList<>();

        /**
         * Appends a character to the current text node.
         *
         * @param character The character to append.
         * @since 1.0.0-beta.2
         */
        private void append(char character) {
            if (!children.isEmpty() && children.get(children.size() - 1) instanceof TextNode textNode) {
                textNode.text.append(character);
            } else {
                children.add(new TextNode(character));
            }
        }

        /**
         * Evaluates all child nodes.
         *
         * @param context The resolution context.
         * @param depth   The current resolution depth.
         * @return The combined child result.
         * @since 1.0.0-beta.2
         */
        @Override
        public @NonNull EvaluationResult evaluate(@NonNull ResolutionContext context, int depth) {
            StringBuilder result = new StringBuilder();
            boolean unresolved = false;
            for (Node child : children) {
                EvaluationResult childResult = child.evaluate(context, depth);
                result.append(childResult.text());
                unresolved = unresolved || childResult.unresolved();
            }
            return new EvaluationResult(result.toString(), unresolved);
        }
    }

    /**
     * Plain text tree node.
     *
     * @since 1.0.0-beta.2
     */
    private static final class TextNode implements Node {
        private final StringBuilder text = new StringBuilder();

        /**
         * Creates a text node.
         *
         * @param character The first character.
         * @since 1.0.0-beta.2
         */
        private TextNode(char character) {
            text.append(character);
        }

        /**
         * Returns the unchanged text.
         *
         * @param context The unused resolution context.
         * @param depth   The unused resolution depth.
         * @return The text result.
         * @since 1.0.0-beta.2
         */
        @Override
        public @NonNull EvaluationResult evaluate(@NonNull ResolutionContext context, int depth) {
            return new EvaluationResult(text.toString(), false);
        }
    }

    /**
     * Placeholder tree node with nested content.
     *
     * @since 1.0.0-beta.2
     */
    private final class PlaceholderNode extends ContainerNode {
        private final char prefix;
        private boolean closed;

        /**
         * Creates a placeholder node.
         *
         * @param prefix The registered placeholder prefix.
         * @since 1.0.0-beta.2
         */
        private PlaceholderNode(char prefix) {
            this.prefix = prefix;
        }

        /**
         * Evaluates nested content before resolving this placeholder.
         *
         * @param context The resolution context.
         * @param depth   The current resolution depth.
         * @return The resolved or preserved placeholder.
         * @since 1.0.0-beta.2
         */
        @Override
        public @NonNull EvaluationResult evaluate(@NonNull ResolutionContext context, int depth) {
            EvaluationResult key = super.evaluate(context, depth);
            String token = prefix + "{" + key.text() + (closed ? "}" : "");
            if (!closed || key.unresolved()) {
                return EvaluationResult.unresolved(token);
            }
            return resolvePlaceholder(prefix, key.text(), context, depth);
        }
    }

    /**
     * Mutable state for one engine resolution.
     *
     * @since 1.0.0-beta.2
     */
    private static final class ResolutionContext {
        private final Deque<PlaceholderReference> activeReferences = new ArrayDeque<>();
        private final Set<PlaceholderReference> activeReferenceSet = new HashSet<>();
        private final Map<PlaceholderReference, EvaluationResult> cache = new HashMap<>();
    }

    /**
     * Result of evaluating a tree node.
     *
     * @param text       The evaluated text.
     * @param unresolved Whether unresolved placeholders remain.
     * @since 1.0.0-beta.2
     */
    private record EvaluationResult(@NonNull String text, boolean unresolved) {

        /**
         * Creates an unresolved evaluation result.
         *
         * @param text The preserved text.
         * @return The unresolved result.
         * @since 1.0.0-beta.2
         */
        private static @NonNull EvaluationResult unresolved(@NonNull String text) {
            return new EvaluationResult(text, true);
        }
    }

    /**
     * Appends a reference to an immutable reference path.
     *
     * @param references The existing references.
     * @param reference  The reference to append.
     * @return The extended reference path.
     * @since 1.0.0-beta.2
     */
    private static @NonNull List<@NonNull PlaceholderReference> appendReference(
            @NonNull List<@NonNull PlaceholderReference> references,
            @NonNull PlaceholderReference reference) {
        List<PlaceholderReference> result = new ArrayList<>(references);
        result.add(reference);
        return List.copyOf(result);
    }

    /**
     * Creates a cycle path beginning with the first occurrence of the repeated reference.
     *
     * @param activeReferences The active resolution path.
     * @param repeatedReference The repeated reference closing the cycle.
     * @return The detected cycle.
     * @since 1.0.0-beta.2
     */
    private static @NonNull List<@NonNull PlaceholderReference> createCycle(
            @NonNull Iterable<@NonNull PlaceholderReference> activeReferences,
            @NonNull PlaceholderReference repeatedReference) {
        List<PlaceholderReference> cycle = new ArrayList<>();
        boolean cycleStarted = false;
        for (PlaceholderReference reference : activeReferences) {
            if (!cycleStarted && reference.equals(repeatedReference)) {
                cycleStarted = true;
            }
            if (cycleStarted) {
                cycle.add(reference);
            }
        }
        cycle.add(repeatedReference);
        return List.copyOf(cycle);
    }

    /**
     * Formats a reference path.
     *
     * @param references The references to format.
     * @return The formatted path.
     * @since 1.0.0-beta.2
     */
    private static @NonNull String formatReferences(
            @NonNull List<@NonNull PlaceholderReference> references) {
        return references.stream()
                .map(PlaceholderReference::token)
                .reduce((left, right) -> left + " -> " + right)
                .orElse("unknown");
    }
}