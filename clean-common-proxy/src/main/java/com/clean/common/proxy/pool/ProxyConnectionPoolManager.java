package com.clean.common.proxy.pool;

import com.clean.common.proxy.properties.CleanProxyProperties.PoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory and registry for named {@link ConnectionProvider} instances.
 *
 * <p>Pools are created on first request by name and cached for the lifetime of the
 * application context. This mirrors the behaviour of the legacy
 * {@code RestProxyConnectionPoolManager} but is Spring-managed and config-driven.
 *
 * <p>Pattern: Factory + Registry (GoF).
 * SRP: owns only connection provider lifecycle.
 */
public class ProxyConnectionPoolManager {

    private static final Logger log = LoggerFactory.getLogger(ProxyConnectionPoolManager.class);

    private final ConcurrentHashMap<String, ConnectionProvider> pools = new ConcurrentHashMap<>();

    /**
     * Returns an existing pool by name, or creates a new one from the given config.
     *
     * @param config pool configuration (name, maxConnections, timeouts)
     * @return a shared {@link ConnectionProvider} keyed by pool name
     */
    public ConnectionProvider getOrCreate(PoolConfig config) {
        return pools.computeIfAbsent(config.getName(), name -> {
            log.info("Creating connection pool '{}' maxConnections={}", name, config.getMaxConnections());

            ConnectionProvider.Builder builder = ConnectionProvider.builder(name)
                    .maxConnections(config.getMaxConnections())
                    .pendingAcquireTimeout(Duration.ofMillis(config.getPendingAcquireTimeoutMs()));

            if (config.getMaxIdleTimeMs() > 0) {
                builder.maxIdleTime(Duration.ofMillis(config.getMaxIdleTimeMs()));
            }

            if (config.getMaxLifeTimeMs() > 0) {
                builder.maxLifeTime(Duration.ofMillis(config.getMaxLifeTimeMs()));
            }

            return builder.build();
        });
    }
}
