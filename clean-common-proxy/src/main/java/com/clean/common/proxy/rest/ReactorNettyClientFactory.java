package com.clean.common.proxy.rest;

import com.clean.common.proxy.pool.ProxyConnectionPoolManager;
import com.clean.common.proxy.properties.CleanProxyProperties.RestConfig;
import com.clean.common.proxy.properties.CleanProxyProperties.SoapConfig;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Factory that builds {@link WebClient} instances from proxy configuration.
 *
 * <p>Handles connection pooling, TLS, timeouts, and default headers.
 * Replaces the inline HttpClient construction in the legacy {@code RestProxyBase}.
 *
 * <p>Pattern: Factory (GoF). SRP: only constructs WebClient instances.
 */
public class ReactorNettyClientFactory {

    private static final Logger log = LoggerFactory.getLogger(ReactorNettyClientFactory.class);

    private final ProxyConnectionPoolManager poolManager;

    public ReactorNettyClientFactory(ProxyConnectionPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    /**
     * Builds a {@link WebClient} from {@link RestConfig}.
     */
    public WebClient build(RestConfig config) {
        HttpClient httpClient = buildHttpClient(
                config.getBaseUrl(),
                config.getConnectTimeoutMs(),
                config.getResponseTimeoutMs(),
                config.getReadTimeoutMs(),
                config.getWriteTimeoutMs(),
                config.getTls().isEnabled(),
                config.getTls().isInsecure(),
                poolManager.getOrCreate(config.getPool())
        );

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(config.getMaxInMemorySizeMb() * 1024 * 1024))
                        .build());

        config.getDefaultHeaders().forEach(builder::defaultHeader);

        return builder.build();
    }

    /**
     * Builds a {@link WebClient} from {@link SoapConfig}.
     */
    public WebClient build(SoapConfig config) {
        HttpClient httpClient = buildHttpClient(
                config.getBaseUrl(),
                config.getConnectTimeoutMs(),
                config.getResponseTimeoutMs(),
                config.getReadTimeoutMs(),
                config.getReadTimeoutMs(),
                false,
                false,
                poolManager.getOrCreate(config.getPool())
        );

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient));

        config.getDefaultHeaders().forEach(builder::defaultHeader);

        return builder.build();
    }

    private HttpClient buildHttpClient(String baseUrl, int connectTimeoutMs, int responseTimeoutMs,
                                        int readTimeoutMs, int writeTimeoutMs,
                                        boolean tlsEnabled, boolean tlsInsecure,
                                        ConnectionProvider pool) {

        log.debug("Building HttpClient for baseUrl={} pool={}", baseUrl, pool);

        HttpClient client = HttpClient.create(pool)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(responseTimeoutMs))
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(writeTimeoutMs, TimeUnit.MILLISECONDS));
                });

        if (tlsEnabled) {
            if (tlsInsecure) {
                log.warn("TLS insecure mode enabled for {} — trust all certificates (NOT for production)", baseUrl);
                Http11SslContextSpec sslContextSpec = Http11SslContextSpec.forClient()
                        .configure(b -> b.trustManager(InsecureTrustManagerFactory.INSTANCE));
                client = client.secure(spec -> spec.sslContext(sslContextSpec));
            } else {
                client = client.secure();
            }
        }

        return client;
    }
}
