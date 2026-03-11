package com.clean.common.proxy.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-named-endpoint SOAP configuration.
 *
 * <p>All timeout and pool fields are boxed (nullable). A {@code null} value means
 * "inherit from the global {@link CleanProxyProperties.SoapConfig}". Only {@code baseUrl}
 * is effective per endpoint.
 *
 * <p>Example {@code application.properties}:
 * <pre>
 * clean.proxy.soap.endpoints.message.base-url=http://kkk.soap/message
 * clean.proxy.soap.endpoints.shop.base-url=http://jjj.soap/shop
 * clean.proxy.soap.endpoints.shop.connect-timeout-ms=8000
 * </pre>
 */
public class EndpointSoapConfig {

    private String baseUrl;
    private Integer connectTimeoutMs;
    private Integer readTimeoutMs;
    private Integer responseTimeoutMs;
    private Map<String, String> defaultHeaders = new HashMap<>();
    private EndpointPoolConfig pool;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public Integer getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(Integer connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public Integer getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(Integer readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

    public Integer getResponseTimeoutMs() { return responseTimeoutMs; }
    public void setResponseTimeoutMs(Integer responseTimeoutMs) { this.responseTimeoutMs = responseTimeoutMs; }

    public Map<String, String> getDefaultHeaders() { return defaultHeaders; }
    public void setDefaultHeaders(Map<String, String> defaultHeaders) { this.defaultHeaders = defaultHeaders; }

    public EndpointPoolConfig getPool() { return pool; }
    public void setPool(EndpointPoolConfig pool) { this.pool = pool; }

    // -------------------------------------------------------------------------
    // Inner config class
    // -------------------------------------------------------------------------

    /**
     * Per-endpoint connection pool overrides.
     * All fields nullable — {@code null} inherits from global {@link CleanProxyProperties.PoolConfig}.
     * Pool name defaults to {@code "{endpointName}-soap-pool"} if not set.
     */
    public static class EndpointPoolConfig {

        private String name;
        private Integer maxConnections;
        private Long pendingAcquireTimeoutMs;
        private Long maxIdleTimeMs;
        private Long maxLifeTimeMs;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getMaxConnections() { return maxConnections; }
        public void setMaxConnections(Integer maxConnections) { this.maxConnections = maxConnections; }

        public Long getPendingAcquireTimeoutMs() { return pendingAcquireTimeoutMs; }
        public void setPendingAcquireTimeoutMs(Long pendingAcquireTimeoutMs) { this.pendingAcquireTimeoutMs = pendingAcquireTimeoutMs; }

        public Long getMaxIdleTimeMs() { return maxIdleTimeMs; }
        public void setMaxIdleTimeMs(Long maxIdleTimeMs) { this.maxIdleTimeMs = maxIdleTimeMs; }

        public Long getMaxLifeTimeMs() { return maxLifeTimeMs; }
        public void setMaxLifeTimeMs(Long maxLifeTimeMs) { this.maxLifeTimeMs = maxLifeTimeMs; }
    }
}
