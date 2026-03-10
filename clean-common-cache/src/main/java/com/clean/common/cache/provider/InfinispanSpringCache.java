package com.clean.common.cache.provider;

import org.infinispan.client.hotrod.RemoteCache;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;

public class InfinispanSpringCache implements Cache {

    private final String name;
    private final RemoteCache<Object, Object> remoteCache;

    public InfinispanSpringCache(String name, RemoteCache<Object, Object> remoteCache) {
        this.name = name;
        this.remoteCache = remoteCache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return remoteCache;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object value = remoteCache.get(key);
        return value != null ? new SimpleValueWrapper(value) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        Object value = remoteCache.get(key);
        if (value == null) {
            return null;
        }
        if (type != null && !type.isInstance(value)) {
            throw new IllegalStateException("Cached value type mismatch for key: " + key);
        }
        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = remoteCache.get(key);
        if (value != null) {
            return (T) value;
        }

        try {
            T loaded = valueLoader.call();
            if (loaded != null) {
                remoteCache.put(key, loaded);
            }
            return loaded;
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    @Override
    public void put(Object key, Object value) {
        remoteCache.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        Object existing = remoteCache.putIfAbsent(key, value);
        return existing != null ? new SimpleValueWrapper(existing) : null;
    }

    @Override
    public void evict(Object key) {
        remoteCache.remove(key);
    }

    @Override
    public void clear() {
        remoteCache.clear();
    }
}
