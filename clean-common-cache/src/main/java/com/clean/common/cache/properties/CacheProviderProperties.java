package com.clean.common.cache.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "clean.cache")
public class CacheProviderProperties {

    private Provider provider = Provider.INFINISPAN;
    private List<String> cacheNames = new ArrayList<>();
    private Infinispan infinispan = new Infinispan();
    private Hazelcast hazelcast = new Hazelcast();

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
}
