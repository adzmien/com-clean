package com.clean.common.proxy.mock;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Value object describing a canned response for a URI pattern.
 *
 * <p>Used by {@link MockResponseRegistry} to match against incoming {@link com.clean.common.proxy.dto.ProxyRequest}
 * URIs and return pre-configured responses.
 *
 * <p>Pattern: Builder (GoF via Lombok). SRP: carries only mock definition data.
 */
@Getter
@Builder
public class MockResponseDefinition {

    /**
     * Regex pattern matched against the request URI.
     * Example: {@code ".*[/]payment.*"} or {@code "https://api.example.com/v1/pay"}
     */
    private final String uriPattern;

    @Builder.Default
    private final int statusCode = 200;

    @Builder.Default
    private final String body = "";

    @Builder.Default
    private final Map<String, String> headers = new HashMap<>();

    /**
     * Optional simulated latency in milliseconds. Useful for testing timeout handling.
     */
    @Builder.Default
    private final long delayMs = 0;
}
