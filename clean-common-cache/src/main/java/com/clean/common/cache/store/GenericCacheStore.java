package com.clean.common.cache.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Provider-agnostic, type-safe cache store that serializes values as JSON strings.
 * <p>
 * Any DTO type can be stored and retrieved using any combination of cacheName + key.
 * The store delegates provider selection entirely to the injected {@link CacheManager},
 * which may be backed by Infinispan, Redis, Hazelcast, or NoOp.
 * </p>
 *
 * <p><b>Thread safety:</b> This class is stateless beyond the injected CacheManager;
 * thread safety is delegated to the CacheManager and its underlying provider.</p>
 */
@Slf4j
public class GenericCacheStore {

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    public GenericCacheStore(CacheManager cacheManager, ObjectMapper objectMapper) {
        this.cacheManager = cacheManager;
        this.objectMapper = objectMapper;
    }

    /**
     * Serializes {@code value} to a JSON string and stores it under {@code key} in the named cache.
     *
     * @param cacheName target cache name
     * @param key       cache key
     * @param value     value to store; must be JSON-serializable
     */
    public <D> void put(String cacheName, String key, D value) {
        String json = serialize(value);
        requireCache(cacheName).put(key, json);
    }

    /**
     * Reads and deserializes the cached JSON string for {@code key} from the named cache.
     * Returns empty if the key is absent. Evicts and returns empty if the stored JSON is corrupt.
     *
     * @param cacheName target cache name
     * @param key       cache key
     * @param type      target deserialization type
     */
    public <D> Optional<D> find(String cacheName, String key, Class<D> type) {
        Cache cache = requireCache(cacheName);
        String json = cache.get(key, String.class);
        if (json == null) {
            return Optional.empty();
        }
        return deserialize(cache, key, json, type);
    }

    /**
     * Returns the cached value if present; otherwise invokes {@code loader},
     * stores the result if present, and returns it.
     * Evicts and falls back to loader if the stored JSON is corrupt.
     *
     * @param cacheName target cache name
     * @param key       cache key
     * @param type      target deserialization type
     * @param loader    supplier invoked on cache miss; result is stored if non-empty
     */
    public <D> Optional<D> findOrLoad(String cacheName, String key, Class<D> type,
                                      Supplier<Optional<D>> loader) {
        Optional<D> cached = find(cacheName, key, type);
        if (cached.isPresent()) {
            return cached;
        }
        Optional<D> loaded = loader.get();
        loaded.ifPresent(value -> put(cacheName, key, value));
        return loaded;
    }

    /**
     * Batch-writes all entries to the named cache. Each value is serialized independently.
     * Entries are written atomically per key (no cross-key atomicity guarantee).
     *
     * @param cacheName target cache name
     * @param entries   map of key → value to store
     */
    public <D> void putAll(String cacheName, Map<String, D> entries) {
        Cache cache = requireCache(cacheName);
        entries.forEach((key, value) -> cache.put(key, serialize(value)));
    }

    /**
     * Removes a single key from the named cache.
     *
     * @param cacheName target cache name
     * @param key       key to evict
     */
    public void evict(String cacheName, String key) {
        requireCache(cacheName).evict(key);
    }

    /**
     * Removes a collection of keys from the named cache.
     *
     * @param cacheName target cache name
     * @param keys      keys to evict
     */
    public void evictAll(String cacheName, Collection<String> keys) {
        Cache cache = requireCache(cacheName);
        keys.forEach(cache::evict);
    }

    /**
     * Clears all entries in the named cache.
     *
     * @param cacheName target cache name
     */
    public void clear(String cacheName) {
        requireCache(cacheName).clear();
    }

    /**
     * Returns the raw JSON string stored under {@code key} in the named cache,
     * without any deserialization. Returns empty if the key is absent.
     *
     * @param cacheName target cache name
     * @param key       cache key
     */
    public Optional<String> findRaw(String cacheName, String key) {
        return Optional.ofNullable(requireCache(cacheName).get(key, String.class));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Cache requireCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("Cache not configured or unavailable: " + cacheName);
        }
        return cache;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON serialization failed for: " + value, ex);
        }
    }

    private <D> Optional<D> deserialize(Cache cache, String key, String json, Class<D> type) {
        try {
            return Optional.of(objectMapper.readValue(json, type));
        } catch (JsonProcessingException ex) {
            log.warn("Corrupt JSON in cache={} key={}, evicting entry", cache.getName(), key, ex);
            cache.evict(key);
            return Optional.empty();
        }
    }
}
