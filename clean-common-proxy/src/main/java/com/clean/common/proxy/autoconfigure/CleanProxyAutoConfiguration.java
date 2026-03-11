package com.clean.common.proxy.autoconfigure;

import com.clean.common.proxy.contract.Transport;
import com.clean.common.proxy.interceptor.TraceHeaderInterceptor;
import com.clean.common.proxy.logger.ProxyExchangeLogger;
import com.clean.common.proxy.mock.MockResponseDefinition;
import com.clean.common.proxy.mock.MockResponseRegistry;
import com.clean.common.proxy.mock.MockTransport;
import com.clean.common.proxy.pool.ProxyConnectionPoolManager;
import com.clean.common.proxy.properties.CleanProxyProperties;
import com.clean.common.proxy.properties.CleanProxyProperties.MockDefinitionConfig;
import com.clean.common.proxy.properties.CleanProxyProperties.RestConfig;
import com.clean.common.proxy.properties.CleanProxyProperties.SoapConfig;
import com.clean.common.proxy.properties.CleanProxyProperties.TraceConfig;
import com.clean.common.proxy.registry.ProxyTransportRegistry;
import com.clean.common.proxy.registry.TransportConfigMerger;
import com.clean.common.proxy.rest.ReactorNettyClientFactory;
import com.clean.common.proxy.rest.RestTransport;
import com.clean.common.proxy.soap.SoapTransport;
import com.clean.common.proxy.trace.ProxyTraceSupplier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto-configuration for {@code clean-common-proxy}.
 *
 * <p>Registers all beans for the transport abstraction:
 * <ul>
 *   <li>{@link ProxyConnectionPoolManager} — shared named connection pool registry</li>
 *   <li>{@link ProxyExchangeLogger} — stateless request/response exchange logger</li>
 *   <li>{@link MockResponseRegistry} — registry for mock response definitions</li>
 *   <li>{@link ProxyTransportRegistry} — named REST + SOAP transport registry;
 *       populated from {@code clean.proxy.rest.endpoints.*} and
 *       {@code clean.proxy.soap.endpoints.*} properties</li>
 *   <li>{@code restTransport} — backward-compat alias for the "default" REST transport;
 *       resolves when {@code clean.proxy.rest.base-url} is set</li>
 *   <li>{@code soapTransport} — backward-compat alias for the "default" SOAP transport;
 *       resolves when {@code clean.proxy.soap.base-url} is set</li>
 *   <li>{@code mockTransport} — mock transport (opt-in via {@code clean.proxy.mock.enabled=true})</li>
 * </ul>
 *
 * <p>Each transport is wrapped with {@link TraceHeaderInterceptor} so all outbound calls
 * automatically propagate distributed trace headers.
 *
 * <p>All beans are guarded with {@code @ConditionalOnMissingBean} — consuming applications
 * can override any component by declaring their own bean.
 *
 * <p>Auto-discovered via:
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 */
@AutoConfiguration
@AutoConfigurationPackage(basePackages = "com.clean.common.proxy")
@EnableConfigurationProperties(CleanProxyProperties.class)
public class CleanProxyAutoConfiguration {

    // -------------------------------------------------------------------------
    // Trace supplier — no-op default; consuming app overrides with real impl
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(ProxyTraceSupplier.class)
    public ProxyTraceSupplier proxyTraceSupplier() {
        return new ProxyTraceSupplier() {
            public String getServerTraceId() { return null; }
            public String getClientTraceId() { return null; }
        };
    }

    // -------------------------------------------------------------------------
    // Shared infrastructure beans
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean(ProxyConnectionPoolManager.class)
    public ProxyConnectionPoolManager proxyConnectionPoolManager() {
        return new ProxyConnectionPoolManager();
    }

    @Bean
    @ConditionalOnMissingBean(ProxyExchangeLogger.class)
    public ProxyExchangeLogger proxyExchangeLogger(ObjectMapper objectMapper) {
        return new ProxyExchangeLogger(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(MockResponseRegistry.class)
    public MockResponseRegistry mockResponseRegistry(CleanProxyProperties properties) {
        MockResponseRegistry registry = new MockResponseRegistry();
        List<MockDefinitionConfig> defs = properties.getMock().getDefinitions();
        if (defs != null && !defs.isEmpty()) {
            defs.stream()
                    .map(d -> MockResponseDefinition.builder()
                            .uriPattern(d.getUriPattern())
                            .statusCode(d.getStatusCode())
                            .body(d.getBody())
                            .delayMs(d.getDelayMs())
                            .headers(d.getHeaders())
                            .build())
                    .forEach(registry::register);
        }
        return registry;
    }

    // -------------------------------------------------------------------------
    // Named transport registry — primary multi-endpoint bean
    // -------------------------------------------------------------------------

    /**
     * Builds the {@link ProxyTransportRegistry} at startup.
     *
     * <p>For each named endpoint under {@code clean.proxy.rest.endpoints.*} and
     * {@code clean.proxy.soap.endpoints.*}, an isolated {@link WebClient} and
     * {@link Transport} are created using per-endpoint config merged over global defaults.
     *
     * <p>When {@code clean.proxy.rest.base-url} is set, a {@code "default"} REST entry is
     * also registered for backward compatibility with
     * {@code @Qualifier("restTransport")} injection.
     */
    @Bean
    @ConditionalOnMissingBean(ProxyTransportRegistry.class)
    public ProxyTransportRegistry proxyTransportRegistry(CleanProxyProperties properties,
                                                          ProxyConnectionPoolManager poolManager,
                                                          ProxyTraceSupplier traceSupplier) {
        ReactorNettyClientFactory factory = new ReactorNettyClientFactory(poolManager);
        TraceConfig traceConfig = properties.getTrace();

        Map<String, Transport> restMap = new LinkedHashMap<>();
        Map<String, Transport> soapMap = new LinkedHashMap<>();

        // REST: "default" from global config (only when base-url is explicitly set)
        RestConfig globalRest = properties.getRest();
        if (globalRest.isEnabled() && StringUtils.hasText(globalRest.getBaseUrl())) {
            restMap.put("default", buildRestTransport(factory, globalRest, traceConfig, traceSupplier));
        }

        // REST: named endpoints — each gets its own WebClient + isolated connection pool
        globalRest.getEndpoints().forEach((name, ep) -> {
            RestConfig effective = TransportConfigMerger.mergeRest(globalRest, ep, name);
            restMap.put(name, buildRestTransport(factory, effective, traceConfig, traceSupplier));
        });

        // SOAP: "default" from global config (only when enabled + base-url is set)
        SoapConfig globalSoap = properties.getSoap();
        if (globalSoap.isEnabled() && StringUtils.hasText(globalSoap.getBaseUrl())) {
            soapMap.put("default", buildSoapTransport(factory, globalSoap, traceConfig, traceSupplier));
        }

        // SOAP: named endpoints
        globalSoap.getEndpoints().forEach((name, ep) -> {
            SoapConfig effective = TransportConfigMerger.mergeSoap(globalSoap, ep, name);
            soapMap.put(name, buildSoapTransport(factory, effective, traceConfig, traceSupplier));
        });

        return new ProxyTransportRegistry(restMap, soapMap);
    }

    // -------------------------------------------------------------------------
    // Backward-compat alias beans — delegate to the "default" registry entry
    // -------------------------------------------------------------------------

    /**
     * Backward-compatible {@code restTransport} bean.
     *
     * <p>Delegates to {@code registry.getRestTransport("default")} so that existing
     * {@code @Qualifier("restTransport")} and {@code BaseRestProxy(Transport, ...)} usages
     * continue to work without modification.
     *
     * <p>Only active when {@code clean.proxy.rest.enabled=true} (the default) AND
     * a "default" entry exists in the registry (i.e. {@code clean.proxy.rest.base-url} is set).
     */
    @Bean(name = "restTransport")
    @ConditionalOnMissingBean(name = "restTransport")
    @ConditionalOnProperty(prefix = "clean.proxy.rest", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Transport restTransport(ProxyTransportRegistry registry) {
        return registry.hasRestTransport("default") ? registry.getRestTransport("default") : null;
    }

    /**
     * Backward-compatible {@code soapTransport} bean.
     *
     * <p>Delegates to {@code registry.getSoapTransport("default")}.
     */
    @Bean(name = "soapTransport")
    @ConditionalOnMissingBean(name = "soapTransport")
    @ConditionalOnProperty(prefix = "clean.proxy.soap", name = "enabled", havingValue = "true")
    public Transport soapTransport(ProxyTransportRegistry registry) {
        return registry.hasSoapTransport("default") ? registry.getSoapTransport("default") : null;
    }

    // -------------------------------------------------------------------------
    // Mock transport — opt-in, never active by default
    // -------------------------------------------------------------------------

    @Bean(name = "mockTransport")
    @ConditionalOnMissingBean(name = "mockTransport")
    @ConditionalOnProperty(prefix = "clean.proxy.mock", name = "enabled", havingValue = "true")
    public Transport mockTransport(CleanProxyProperties properties,
                                   MockResponseRegistry registry,
                                   ProxyTraceSupplier traceSupplier) {
        MockTransport mockTransport = new MockTransport(registry, properties.getMock().isFailOnNoMatch());
        return new TraceHeaderInterceptor(mockTransport, properties.getTrace(), traceSupplier);
    }

    // -------------------------------------------------------------------------
    // Private transport construction helpers (DRY)
    // -------------------------------------------------------------------------

    private Transport buildRestTransport(ReactorNettyClientFactory factory,
                                          RestConfig config,
                                          TraceConfig traceConfig,
                                          ProxyTraceSupplier traceSupplier) {
        WebClient webClient = factory.build(config);
        return new TraceHeaderInterceptor(new RestTransport(webClient, config), traceConfig, traceSupplier);
    }

    private Transport buildSoapTransport(ReactorNettyClientFactory factory,
                                          SoapConfig config,
                                          TraceConfig traceConfig,
                                          ProxyTraceSupplier traceSupplier) {
        WebClient webClient = factory.build(config);
        return new TraceHeaderInterceptor(new SoapTransport(webClient, config), traceConfig, traceSupplier);
    }
}
