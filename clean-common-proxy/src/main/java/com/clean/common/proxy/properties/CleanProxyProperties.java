package com.clean.common.proxy.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for the proxy module.
 *
 * <p>Example {@code application.properties}:
 * <pre>
 * clean.proxy.rest.enabled=true
 * clean.proxy.rest.base-url=https://api.example.com
 * clean.proxy.rest.connect-timeout-ms=5000
 * clean.proxy.rest.read-timeout-ms=30000
 * clean.proxy.rest.pool.name=default-proxy-pool
 * clean.proxy.rest.pool.max-connections=500
 * clean.proxy.rest.tls.insecure=false
 *
 * # Named REST endpoints (per-endpoint overrides on top of global defaults):
 * clean.proxy.rest.endpoints.payment.base-url=http://aaa.com/payment
 * clean.proxy.rest.endpoints.payment.connect-timeout-ms=3000
 * clean.proxy.rest.endpoints.report.base-url=http://bbb.com/report
 *
 * clean.proxy.soap.enabled=false
 * clean.proxy.soap.base-url=https://soap.example.com
 *
 * # Named SOAP endpoints:
 * clean.proxy.soap.endpoints.message.base-url=http://kkk.soap/message
 * clean.proxy.soap.endpoints.shop.base-url=http://jjj.soap/shop
 *
 * clean.proxy.mock.enabled=false
 * clean.proxy.mock.fail-on-no-match=true
 * clean.proxy.mock.definitions[0].uri-pattern=.*&#47;example.*
 * clean.proxy.mock.definitions[0].status-code=200
 * clean.proxy.mock.definitions[0].body={"ok":true}
 *
 * clean.proxy.trace.server-trace-header=X-Trace-Id
 * clean.proxy.trace.client-trace-header=Client-Trace-Id
 * </pre>
 */
@ConfigurationProperties(prefix = "clean.proxy")
public class CleanProxyProperties {

    private RestConfig rest = new RestConfig();
    private SoapConfig soap = new SoapConfig();
    private MockConfig mock = new MockConfig();
    private TraceConfig trace = new TraceConfig();

    public RestConfig getRest() { return rest; }
    public void setRest(RestConfig rest) { this.rest = rest; }

    public SoapConfig getSoap() { return soap; }
    public void setSoap(SoapConfig soap) { this.soap = soap; }

    public MockConfig getMock() { return mock; }
    public void setMock(MockConfig mock) { this.mock = mock; }

    public TraceConfig getTrace() { return trace; }
    public void setTrace(TraceConfig trace) { this.trace = trace; }

    // -------------------------------------------------------------------------
    // REST config
    // -------------------------------------------------------------------------

    public static class RestConfig {

        private boolean enabled = true;
        private String baseUrl = "";
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 30000;
        private int responseTimeoutMs = 30000;
        private int writeTimeoutMs = 30000;
        private int maxInMemorySizeMb = 5;
        private Map<String, String> defaultHeaders = new HashMap<>();
        private PoolConfig pool = new PoolConfig();
        private TlsConfig tls = new TlsConfig();
        private Map<String, EndpointRestConfig> endpoints = new LinkedHashMap<>();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

        public int getResponseTimeoutMs() { return responseTimeoutMs; }
        public void setResponseTimeoutMs(int responseTimeoutMs) { this.responseTimeoutMs = responseTimeoutMs; }

        public int getWriteTimeoutMs() { return writeTimeoutMs; }
        public void setWriteTimeoutMs(int writeTimeoutMs) { this.writeTimeoutMs = writeTimeoutMs; }

        public int getMaxInMemorySizeMb() { return maxInMemorySizeMb; }
        public void setMaxInMemorySizeMb(int maxInMemorySizeMb) { this.maxInMemorySizeMb = maxInMemorySizeMb; }

        public Map<String, String> getDefaultHeaders() { return defaultHeaders; }
        public void setDefaultHeaders(Map<String, String> defaultHeaders) { this.defaultHeaders = defaultHeaders; }

        public PoolConfig getPool() { return pool; }
        public void setPool(PoolConfig pool) { this.pool = pool; }

        public TlsConfig getTls() { return tls; }
        public void setTls(TlsConfig tls) { this.tls = tls; }

        public Map<String, EndpointRestConfig> getEndpoints() { return endpoints; }
        public void setEndpoints(Map<String, EndpointRestConfig> endpoints) { this.endpoints = endpoints; }
    }

    // -------------------------------------------------------------------------
    // Connection pool config
    // -------------------------------------------------------------------------

    public static class PoolConfig {

        private String name = "clean-proxy-pool";
        private int maxConnections = 500;
        private long pendingAcquireTimeoutMs = 45000;
        private long maxIdleTimeMs = 20000;
        private long maxLifeTimeMs = 0;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

        public long getPendingAcquireTimeoutMs() { return pendingAcquireTimeoutMs; }
        public void setPendingAcquireTimeoutMs(long pendingAcquireTimeoutMs) { this.pendingAcquireTimeoutMs = pendingAcquireTimeoutMs; }

        public long getMaxIdleTimeMs() { return maxIdleTimeMs; }
        public void setMaxIdleTimeMs(long maxIdleTimeMs) { this.maxIdleTimeMs = maxIdleTimeMs; }

        public long getMaxLifeTimeMs() { return maxLifeTimeMs; }
        public void setMaxLifeTimeMs(long maxLifeTimeMs) { this.maxLifeTimeMs = maxLifeTimeMs; }
    }

    // -------------------------------------------------------------------------
    // TLS config
    // -------------------------------------------------------------------------

    public static class TlsConfig {

        private boolean enabled = false;
        private boolean insecure = false;
        private String trustStorePath = "";
        private String trustStorePassword = "";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public boolean isInsecure() { return insecure; }
        public void setInsecure(boolean insecure) { this.insecure = insecure; }

        public String getTrustStorePath() { return trustStorePath; }
        public void setTrustStorePath(String trustStorePath) { this.trustStorePath = trustStorePath; }

        public String getTrustStorePassword() { return trustStorePassword; }
        public void setTrustStorePassword(String trustStorePassword) { this.trustStorePassword = trustStorePassword; }
    }

    // -------------------------------------------------------------------------
    // SOAP config
    // -------------------------------------------------------------------------

    public static class SoapConfig {

        private boolean enabled = false;
        private String baseUrl = "";
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 30000;
        private int responseTimeoutMs = 30000;
        private Map<String, String> defaultHeaders = new HashMap<>();
        private PoolConfig pool = new PoolConfig();
        private Map<String, EndpointSoapConfig> endpoints = new LinkedHashMap<>();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

        public int getResponseTimeoutMs() { return responseTimeoutMs; }
        public void setResponseTimeoutMs(int responseTimeoutMs) { this.responseTimeoutMs = responseTimeoutMs; }

        public Map<String, String> getDefaultHeaders() { return defaultHeaders; }
        public void setDefaultHeaders(Map<String, String> defaultHeaders) { this.defaultHeaders = defaultHeaders; }

        public PoolConfig getPool() { return pool; }
        public void setPool(PoolConfig pool) { this.pool = pool; }

        public Map<String, EndpointSoapConfig> getEndpoints() { return endpoints; }
        public void setEndpoints(Map<String, EndpointSoapConfig> endpoints) { this.endpoints = endpoints; }
    }

    // -------------------------------------------------------------------------
    // Mock config
    // -------------------------------------------------------------------------

    public static class MockConfig {

        private boolean enabled = false;
        private boolean failOnNoMatch = true;
        private List<MockDefinitionConfig> definitions = new ArrayList<>();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public boolean isFailOnNoMatch() { return failOnNoMatch; }
        public void setFailOnNoMatch(boolean failOnNoMatch) { this.failOnNoMatch = failOnNoMatch; }

        public List<MockDefinitionConfig> getDefinitions() { return definitions; }
        public void setDefinitions(List<MockDefinitionConfig> definitions) { this.definitions = definitions; }
    }

    public static class MockDefinitionConfig {

        private String uriPattern = "";
        private int statusCode = 200;
        private String body = "";
        private long delayMs = 0;
        private Map<String, String> headers = new HashMap<>();

        public String getUriPattern() { return uriPattern; }
        public void setUriPattern(String uriPattern) { this.uriPattern = uriPattern; }

        public int getStatusCode() { return statusCode; }
        public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }

        public long getDelayMs() { return delayMs; }
        public void setDelayMs(long delayMs) { this.delayMs = delayMs; }

        public Map<String, String> getHeaders() { return headers; }
        public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    }

    // -------------------------------------------------------------------------
    // Trace config
    // -------------------------------------------------------------------------

    public static class TraceConfig {

        private String serverTraceHeader = "X-Trace-Id";
        private String clientTraceHeader = "Client-Trace-Id";

        public String getServerTraceHeader() { return serverTraceHeader; }
        public void setServerTraceHeader(String serverTraceHeader) { this.serverTraceHeader = serverTraceHeader; }

        public String getClientTraceHeader() { return clientTraceHeader; }
        public void setClientTraceHeader(String clientTraceHeader) { this.clientTraceHeader = clientTraceHeader; }
    }
}
