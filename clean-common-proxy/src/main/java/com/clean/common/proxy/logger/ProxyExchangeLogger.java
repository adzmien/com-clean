package com.clean.common.proxy.logger;

import com.clean.common.proxy.dto.ProxyRequest;
import com.clean.common.proxy.dto.ProxyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.util.stream.Collectors;

/**
 * Stateless bean responsible for logging outbound proxy request/response exchanges.
 *
 * <p>Extracted from the legacy {@code RestProxyBase.printRequest()} and {@code printResponse()}
 * to restore SRP — the base proxy class no longer has a logging concern.
 *
 * <p>Log output is written to the {@link Logger} instance of the calling proxy class,
 * so log entries appear under the consuming class name in log files.
 */
public class ProxyExchangeLogger {

    private final ObjectMapper objectMapper;

    public ProxyExchangeLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Logs the outbound request details at INFO level.
     */
    public void logRequest(Logger log, ProxyRequest request) {
        if (!log.isInfoEnabled()) return;

        StringBuilder sb = new StringBuilder("Proxy Request");
        sb.append("\r\n--------------------------------------------------------------------\r\n");
        sb.append("URI=[").append(request.getUri()).append("]");
        sb.append(" method=[").append(request.getMethod()).append("]");
        sb.append("\r\n");

        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            String headersLog = request.getHeaders().entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));
            sb.append("Headers: {").append(headersLog).append("}");
            sb.append("\r\n");
        }

        if (request.getBody() != null) {
            sb.append(prettyPrint(request.getBody()));
        }

        sb.append("\r\n--------------------------------------------------------------------");
        log.info(sb.toString());
    }

    /**
     * Logs the inbound response details at INFO level.
     */
    public void logResponse(Logger log, ProxyResponse response) {
        if (!log.isInfoEnabled()) return;

        StringBuilder sb = new StringBuilder("Proxy Response");
        sb.append("\r\n--------------------------------------------------------------------\r\n");
        sb.append("status=[").append(response.getStatusCode()).append("]");
        sb.append(" durationMs=[").append(response.getDurationMillis()).append("]");
        sb.append("\r\n");

        if (response.getBody() != null) {
            sb.append(prettyPrint(response.getBody()));
        }

        sb.append("\r\n--------------------------------------------------------------------");
        log.info(sb.toString());
    }

    private String prettyPrint(String json) {
        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            return objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(parsed);
        } catch (Exception e) {
            return json;
        }
    }
}
