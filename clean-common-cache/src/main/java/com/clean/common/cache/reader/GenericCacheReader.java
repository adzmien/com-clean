package com.clean.common.cache.reader;

import com.clean.common.cache.exception.MissingCacheValueException;
import com.clean.common.cache.store.GenericCacheStore;

import java.util.Optional;

public class GenericCacheReader implements CacheReader {

    private final GenericCacheStore cacheStore;

    public GenericCacheReader(GenericCacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    @Override
    public <D> Optional<D> find(String cacheName, String key, Class<D> type) {
        return cacheStore.find(cacheName, key, type);
    }

    @Override
    public <D> D getRequired(String cacheName, String key, Class<D> type) {
        return find(cacheName, key, type)
                .orElseThrow(() -> new MissingCacheValueException(
                        "Missing required cache value — cache=" + cacheName + " key=" + key));
    }

    @Override
    public Optional<String> findRaw(String cacheName, String key) {
        return cacheStore.findRaw(cacheName, key);
    }
}
