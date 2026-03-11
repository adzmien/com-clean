package com.clean.common.proxy.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable value object representing a single outbound proxy call.
 *
 * <p>Built via {@link #builder()} — Lombok {@code @Builder} provides a fluent API.
 * All fields are read-only after construction.
 */
@Getter
@Builder(toBuilder = true)
public class ProxyRequest {

    private final String uri;
    private final HttpMethod method;
    private final String body;

    @Builder.Default
    private final Map<String, String> headers = new HashMap<>();

    /**
     * Supported HTTP methods.
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH
    }

    /**
     * Returns a new {@link ProxyRequest} with the given headers merged on top of the
     * existing ones. Used by {@link com.clean.common.proxy.interceptor.TraceHeaderInterceptor}
     * to inject trace headers without mutating the original request.
     */
    public ProxyRequest withAdditionalHeaders(Map<String, String> additionalHeaders) {
        Map<String, String> merged = new HashMap<>(this.headers != null ? this.headers : Collections.emptyMap());
        merged.putAll(additionalHeaders);
        return this.toBuilder().headers(merged).build();
    }
}
