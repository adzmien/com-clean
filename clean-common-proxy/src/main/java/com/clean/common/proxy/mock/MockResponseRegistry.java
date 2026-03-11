package com.clean.common.proxy.mock;

import com.clean.common.proxy.dto.ProxyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.PatternSyntaxException;

/**
 * Registry that holds {@link MockResponseDefinition} entries and resolves the first
 * matching entry for a given {@link ProxyRequest}.
 *
 * <p>Definitions are matched by regex against the request URI.
 * The first matching definition wins (order of registration preserved).
 *
 * <p>Thread-safe for concurrent test scenarios via {@link CopyOnWriteArrayList}.
 *
 * <p>Pattern: Registry. SRP: only owns lookup and registration logic.
 */
public class MockResponseRegistry {

    private static final Logger log = LoggerFactory.getLogger(MockResponseRegistry.class);

    private final List<MockResponseDefinition> definitions = new CopyOnWriteArrayList<>();

    /**
     * Registers a mock definition. Definitions added later are evaluated last.
     */
    public void register(MockResponseDefinition definition) {
        definitions.add(definition);
        log.debug("Registered mock definition: pattern='{}' status={}", definition.getUriPattern(), definition.getStatusCode());
    }

    /**
     * Loads a list of definitions (typically from {@link com.clean.common.proxy.properties.CleanProxyProperties.MockConfig}).
     */
    public void registerAll(List<MockResponseDefinition> defs) {
        defs.forEach(this::register);
    }

    /**
     * Returns the first definition whose URI pattern matches the request URI, or empty.
     */
    public Optional<MockResponseDefinition> match(ProxyRequest request) {
        String uri = request.getUri() != null ? request.getUri() : "";
        return definitions.stream()
                .filter(def -> matchesPattern(def.getUriPattern(), uri))
                .findFirst();
    }

    /**
     * Clears all registered definitions. Useful for test teardown.
     */
    public void clear() {
        definitions.clear();
    }

    private boolean matchesPattern(String pattern, String uri) {
        if (pattern == null || pattern.isBlank()) return false;
        try {
            return uri.matches(pattern);
        } catch (PatternSyntaxException ex) {
            log.warn("Invalid mock URI pattern '{}': {}", pattern, ex.getMessage());
            return false;
        }
    }
}
