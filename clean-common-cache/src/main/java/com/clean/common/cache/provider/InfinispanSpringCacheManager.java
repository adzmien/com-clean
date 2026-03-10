package com.clean.common.cache.provider;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InfinispanSpringCacheManager implements CacheManager {

    private final RemoteCacheManager remoteCacheManager;
    private final Collection<String> configuredCacheNames;
    private final Map<String, Cache> caches = new ConcurrentHashMap<>();

    public InfinispanSpringCacheManager(RemoteCacheManager remoteCacheManager,
                                        Collection<String> configuredCacheNames) {
        this.remoteCacheManager = remoteCacheManager;
        this.configuredCacheNames = configuredCacheNames;
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, this::resolveCache);
    }

    @Override
    public Collection<String> getCacheNames() {
        LinkedHashSet<String> names = new LinkedHashSet<>(configuredCacheNames);
        names.addAll(caches.keySet());
        return names;
    }

    private Cache resolveCache(String name) {
        if (!remoteCacheManager.isStarted()) {
            remoteCacheManager.start();
        }
        RemoteCache<Object, Object> remoteCache = remoteCacheManager.getCache(name);
        if (remoteCache == null) {
            throw new IllegalStateException(
                    "Infinispan cache not found: " + name
                    + ". Ensure the K8s cache-create Job has run successfully before application startup.");
        }
        return new InfinispanSpringCache(name, remoteCache);
    }
}
