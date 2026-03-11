package com.clean.common.proxy.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Immutable value object representing a transport response.
 *
 * <p>Built by transport implementations after executing an outbound call.
 * {@link #isSuccess()} returns {@code true} for 2xx status codes.
 */
@Getter
@Builder
public class ProxyResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;
    private final long durationMillis;

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}
