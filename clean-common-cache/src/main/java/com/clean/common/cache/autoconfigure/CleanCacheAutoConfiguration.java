package com.clean.common.cache.autoconfigure;

import com.clean.common.cache.properties.CacheProviderProperties;
import com.clean.common.cache.provider.InfinispanSpringCacheManager;
import com.clean.common.cache.reader.CacheReader;
import com.clean.common.cache.reader.GenericCacheReader;
import com.clean.common.cache.store.GenericCacheStore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.util.Set;

@AutoConfiguration(before = CacheAutoConfiguration.class)
@EnableCaching
@EnableConfigurationProperties(CacheProviderProperties.class)
public class CleanCacheAutoConfiguration {

    // -------------------------------------------------------------------------
    // Redis
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnProperty(prefix = "clean.cache", name = "provider", havingValue = "redis")
    public CacheManager redisCacheManager(
            RedisConnectionFactory redisConnectionFactory,
            CacheProviderProperties properties) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues();

        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration);
        if (!properties.getCacheNames().isEmpty()) {
            builder.initialCacheNames(Set.copyOf(properties.getCacheNames()));
        }
        return builder.build();
    }

    // -------------------------------------------------------------------------
    // Infinispan (default)
    // -------------------------------------------------------------------------

    @Bean(destroyMethod = "stop")
    @ConditionalOnMissingBean(RemoteCacheManager.class)
    @ConditionalOnProperty(prefix = "clean.cache", name = "provider",
            havingValue = "infinispan", matchIfMissing = true)
    public RemoteCacheManager infinispanRemoteCacheManager(CacheProviderProperties properties) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServers(properties.getInfinispan().getServerList());
        builder.clientIntelligence(ClientIntelligence.BASIC);

        String username = properties.getInfinispan().getUsername();
        String password = properties.getInfinispan().getPassword();
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            builder.security().authentication().enable().username(username).password(password);
        }

        // Delay remote connection attempts until cache operations are invoked.
        return new RemoteCacheManager(builder.build(), false);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnProperty(prefix = "clean.cache", name = "provider",
            havingValue = "infinispan", matchIfMissing = true)
    public CacheManager infinispanCacheManager(
            RemoteCacheManager remoteCacheManager,
            CacheProviderProperties properties) {
        return new InfinispanSpringCacheManager(
                remoteCacheManager,
                properties.getCacheNames());
    }

    // -------------------------------------------------------------------------
    // Hazelcast
    // -------------------------------------------------------------------------

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(HazelcastInstance.class)
    @ConditionalOnProperty(prefix = "clean.cache", name = "provider", havingValue = "hazelcast")
    public HazelcastInstance hazelcastInstance(CacheProviderProperties properties) {
        ClientConfig config = new ClientConfig();
        config.setClusterName(properties.getHazelcast().getClusterName());
        properties.getHazelcast().getServerAddresses()
                  .forEach(addr -> config.getNetworkConfig().addAddress(addr));
        return HazelcastClient.newHazelcastClient(config);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnProperty(prefix = "clean.cache", name = "provider", havingValue = "hazelcast")
    public CacheManager hazelcastCacheManager(HazelcastInstance hazelcastInstance) {
        return new HazelcastCacheManager(hazelcastInstance);
    }

    // -------------------------------------------------------------------------
    // NoOp
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnProperty(prefix = "clean.cache", name = "provider", havingValue = "none")
    public CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }

    // -------------------------------------------------------------------------
    // Generic cache infrastructure — auto-configured alongside CacheManager
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean(GenericCacheStore.class)
    public GenericCacheStore genericCacheStore(CacheManager cacheManager, ObjectMapper objectMapper) {
        return new GenericCacheStore(cacheManager, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(CacheReader.class)
    public CacheReader genericCacheReader(GenericCacheStore genericCacheStore) {
        return new GenericCacheReader(genericCacheStore);
    }
}
