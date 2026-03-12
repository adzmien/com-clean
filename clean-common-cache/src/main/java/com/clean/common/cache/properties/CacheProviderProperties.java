package com.clean.common.cache.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "clean.cache")
public class CacheProviderProperties {

    private Provider provider = Provider.INFINISPAN;
    private List<String> cacheNames = new ArrayList<>();
    private Infinispan infinispan = new Infinispan();
    private Hazelcast hazelcast = new Hazelcast();
    private Redis redis = new Redis();

    public enum Provider {
        INFINISPAN,
        REDIS,
        HAZELCAST,
        NONE
    }

    @Getter
    @Setter
    public static class Infinispan {
        private String serverList = "127.0.0.1:11222";
        private String username = "infinispan";
        private String password;
    }

    @Getter
    @Setter
    public static class Hazelcast {
        private String clusterName = "dev";
        private List<String> serverAddresses = new ArrayList<>(List.of("127.0.0.1:5701"));
    }

    @Getter
    @Setter
    public static class Redis {
        private long defaultTtlSeconds = 3600;
        private Map<String, CacheConfig> cacheConfigs = new HashMap<>();
    }

    @Getter
    @Setter
    public static class CacheConfig {
        private long ttlSeconds;
    }
}
