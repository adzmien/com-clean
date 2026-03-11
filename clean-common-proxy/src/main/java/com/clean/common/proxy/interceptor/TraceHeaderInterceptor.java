package com.clean.common.proxy.interceptor;

import com.clean.common.proxy.contract.Transport;
import com.clean.common.proxy.dto.ProxyRequest;
import com.clean.common.proxy.dto.ProxyResponse;
import com.clean.common.proxy.properties.CleanProxyProperties.TraceConfig;
import com.clean.common.proxy.trace.ProxyTraceSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Decorator that injects distributed trace headers into every outbound {@link ProxyRequest}.
 *
 * <p>Reads trace IDs from the injected {@link ProxyTraceSupplier} and appends the configured
 * header names to the request before delegating to the wrapped {@link Transport}.
 *
 * <p>Pattern: Decorator (GoF). SRP: only injects trace headers.
 * OCP: transport implementations are unaware of trace logic.
 * DIP: wraps the {@link Transport} interface and reads from {@link ProxyTraceSupplier} interface —
 * no dependency on any concrete ThreadLocal holder.
 *
 * <p><strong>Note:</strong> The default {@link ProxyTraceSupplier} is a no-op.
 * Register a {@code @Bean ProxyTraceSupplier} in your application to supply real trace IDs
 * (e.g. from a {@code ThreadLocal}, MDC, or Micrometer context propagation).
 */
public class TraceHeaderInterceptor implements Transport {

    private static final Logger log = LoggerFactory.getLogger(TraceHeaderInterceptor.class);

    private final Transport delegate;
    private final TraceConfig traceConfig;
    private final ProxyTraceSupplier traceSupplier;

    public TraceHeaderInterceptor(Transport delegate, TraceConfig traceConfig, ProxyTraceSupplier traceSupplier) {
        this.delegate = delegate;
        this.traceConfig = traceConfig;
        this.traceSupplier = traceSupplier;
    }

    @Override
    public ProxyResponse execute(ProxyRequest request) {
        Map<String, String> traceHeaders = buildTraceHeaders();
        if (!traceHeaders.isEmpty()) {
            request = request.withAdditionalHeaders(traceHeaders);
        }
        return delegate.execute(request);
    }

    private Map<String, String> buildTraceHeaders() {
        Map<String, String> headers = new HashMap<>();

        String serverTrace = traceSupplier.getServerTraceId();
        if (StringUtils.hasText(serverTrace)) {
            headers.put(traceConfig.getServerTraceHeader(), serverTrace);
        }

        String clientTrace = traceSupplier.getClientTraceId();
        if (StringUtils.hasText(clientTrace)) {
            headers.put(traceConfig.getClientTraceHeader(), clientTrace);
        }

        return headers;
    }
}
