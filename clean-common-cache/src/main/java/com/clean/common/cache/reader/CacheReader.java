package com.clean.common.cache.reader;

import com.clean.common.cache.exception.MissingCacheValueException;

import java.util.Optional;

/**
 * Read-only view of the generic cache store.
 * <p>
 * Use this interface when a component only needs to read values from
 * any cache by name and key, without owning write or eviction responsibilities.
 * Inject {@link CacheReader} rather than {@link com.clean.common.cache.store.GenericCacheStore}
 * to enforce ISP — only read access is exposed.
 * </p>
 */
public interface CacheReader {

    /**
     * Returns the deserialized value for {@code key} in {@code cacheName},
     * or empty if absent or corrupt (corrupt entry is evicted automatically).
     *
     * @param cacheName target cache name
     * @param key       cache key
     * @param type      target deserialization type
     */
    <D> Optional<D> find(String cacheName, String key, Class<D> type);

    /**
     * Returns the deserialized value for {@code key} in {@code cacheName}.
     *
     * @throws MissingCacheValueException if the key is absent or the entry is corrupt
     */
    <D> D getRequired(String cacheName, String key, Class<D> type);

    /**
     * Returns the raw JSON string stored under {@code key} in the named cache,
     * without any deserialization. Returns empty if the key is absent.
     *
     * @param cacheName target cache name
     * @param key       cache key
     */
    Optional<String> findRaw(String cacheName, String key);
}
