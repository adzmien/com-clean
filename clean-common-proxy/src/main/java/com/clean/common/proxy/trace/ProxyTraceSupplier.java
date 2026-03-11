package com.clean.common.proxy.trace;

/**
 * SPI for supplying distributed trace IDs to outbound proxy requests.
 *
 * <p>Register a {@code @Bean} implementing this interface in your application to inject
 * trace IDs from your context propagation mechanism (ThreadLocal, MDC, Micrometer, etc.).
 *
 * <p>A no-op default (returns {@code null} for both IDs) is registered by
 * {@code CleanProxyAutoConfiguration} via {@code @ConditionalOnMissingBean}.
 *
 * <p>Pattern: DIP — {@link com.clean.common.proxy.interceptor.TraceHeaderInterceptor}
 * depends on this interface, not on any concrete ThreadLocal holder.
 */
public interface ProxyTraceSupplier {

    /**
     * Returns the current server-side trace ID, or {@code null} if not available.
     */
    String getServerTraceId();

    /**
     * Returns the current client-side trace ID, or {@code null} if not available.
     */
    String getClientTraceId();
}
