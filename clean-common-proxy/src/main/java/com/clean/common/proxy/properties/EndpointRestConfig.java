package com.clean.common.proxy.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-named-endpoint REST configuration.
 *
 * <p>All timeout and pool fields are boxed (nullable). A {@code null} value means
 * "inherit from the global {@link CleanProxyProperties.RestConfig}". Only {@code baseUrl}
 * is effective per endpoint.
 *
 * <p>Example {@code application.properties}:
 * <pre>
 * clean.proxy.rest.endpoints.payment.base-url=http://aaa.com/payment
 * clean.proxy.rest.endpoints.payment.connect-timeout-ms=3000
 * clean.proxy.rest.endpoints.payment.pool.max-connections=200
 *
 * clean.proxy.rest.endpoints.report.base-url=http://bbb.com/report
 * # report inherits all other settings from global defaults
 * </pre>
 */
public class EndpointRestConfig {

    private String baseUrl;
    private Integer connectTimeoutMs;
    private Integer readTimeoutMs;
    private Integer responseTimeoutMs;
    private Integer writeTimeoutMs;
    private Integer maxInMemorySizeMb;
    private Map<String, String> defaultHeaders = new HashMap<>();
    private EndpointPoolConfig pool;
    private EndpointTlsConfig tls;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public Integer getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(Integer connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public Integer getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(Integer readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

    public Integer getResponseTimeoutMs() { return responseTimeoutMs; }
    public void setResponseTimeoutMs(Integer responseTimeoutMs) { this.responseTimeoutMs = responseTimeoutMs; }

    public Integer getWriteTimeoutMs() { return writeTimeoutMs; }
    public void setWriteTimeoutMs(Integer writeTimeoutMs) { this.writeTimeoutMs = writeTimeoutMs; }

    public Integer getMaxInMemorySizeMb() { return maxInMemorySizeMb; }
    public void setMaxInMemorySizeMb(Integer maxInMemorySizeMb) { this.maxInMemorySizeMb = maxInMemorySizeMb; }

    public Map<String, String> getDefaultHeaders() { return defaultHeaders; }
    public void setDefaultHeaders(Map<String, String> defaultHeaders) { this.defaultHeaders = defaultHeaders; }

    public EndpointPoolConfig getPool() { return pool; }
    public void setPool(EndpointPoolConfig pool) { this.pool = pool; }

    public EndpointTlsConfig getTls() { return tls; }
    public void setTls(EndpointTlsConfig tls) { this.tls = tls; }

    // -------------------------------------------------------------------------
    // Inner config classes
    // -------------------------------------------------------------------------

    /**
     * Per-endpoint connection pool overrides.
     * All fields nullable — {@code null} inherits from global {@link CleanProxyProperties.PoolConfig}.
     * Pool name defaults to {@code "{endpointName}-rest-pool"} if not set.
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

    /**
     * Per-endpoint TLS overrides.
     * All fields nullable — {@code null} inherits from global {@link CleanProxyProperties.TlsConfig}.
     */
    public static class EndpointTlsConfig {

        private Boolean insecure;
        private String trustStorePath;
        private String trustStorePassword;

        public Boolean getInsecure() { return insecure; }
        public void setInsecure(Boolean insecure) { this.insecure = insecure; }

        public String getTrustStorePath() { return trustStorePath; }
        public void setTrustStorePath(String trustStorePath) { this.trustStorePath = trustStorePath; }

        public String getTrustStorePassword() { return trustStorePassword; }
        public void setTrustStorePassword(String trustStorePassword) { this.trustStorePassword = trustStorePassword; }
    }
}
