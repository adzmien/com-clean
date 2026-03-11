package com.clean.common.proxy.support;

import com.clean.common.proxy.contract.Transport;
import com.clean.common.proxy.dto.ProxyRequest;
import com.clean.common.proxy.dto.ProxyResponse;
import com.clean.common.proxy.logger.ProxyExchangeLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Template Method base class for all concrete proxy implementations in consuming modules.
 *
 * <p>Defines the invariant execution skeleton:
 * <ol>
 *   <li>Delegate to the injected {@link Transport} (already trace-decorated by auto-config)</li>
 *   <li>Log the request and response via {@link ProxyExchangeLogger}</li>
 * </ol>
 *
 * <p>Subclasses provide only the domain-specific {@link ProxyRequest} construction and
 * response deserialization. They do NOT override {@link #execute(ProxyRequest)}.
 *
 * <p>Pattern: Template Method (GoF).
 * OCP: subclasses extend without modifying this class.
 * DIP: depends on {@link Transport} interface, not concrete transports.
 * LSP: all subclasses are substitutable for {@code BaseProxy}.
 *
 * <p>Usage example:
 * <pre>{@code
 * @Component
 * public class PaymentServiceProxy extends BaseRestProxy {
 *
 *     public PaymentServiceProxy(@Qualifier("restTransport") Transport transport,
 *                                 ProxyExchangeLogger logger,
 *                                 ObjectMapper objectMapper) {
 *         super(transport, logger, objectMapper);
 *     }
 *
 *     public PaymentResponse pay(PaymentRequest req) {
 *         ProxyResponse res = execute(ProxyRequest.builder()
 *             .uri("/payment")
 *             .method(ProxyRequest.HttpMethod.POST)
 *             .body(json(req))
 *             .build());
 *         return fromJson(res.getBody(), PaymentResponse.class);
 *     }
 * }
 * }</pre>
 */
public abstract class BaseProxy {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Transport transport;
    private final ProxyExchangeLogger exchangeLogger;
    private final ObjectMapper objectMapper;

    protected BaseProxy(Transport transport,
                        ProxyExchangeLogger exchangeLogger,
                        ObjectMapper objectMapper) {
        this.transport = transport;
        this.exchangeLogger = exchangeLogger;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes the outbound call via the configured transport and logs the exchange.
     *
     * <p>The {@link Transport} injected here is already wrapped by
     * {@link com.clean.common.proxy.interceptor.TraceHeaderInterceptor} by the auto-configuration.
     */
    protected ProxyResponse execute(ProxyRequest request) {
        exchangeLogger.logRequest(log, request);
        ProxyResponse response = transport.execute(request);
        exchangeLogger.logResponse(log, response);
        return response;
    }

    /**
     * Serializes an object to a compact JSON string.
     */
    protected String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON serialization failed for: " + value, ex);
        }
    }

    /**
     * Deserializes a JSON string to the given type.
     *
     * @throws com.clean.common.proxy.exception.ProxyException if deserialization fails
     */
    protected <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new com.clean.common.proxy.exception.ProxyException(
                    "Failed to deserialize response body to " + type.getSimpleName() + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Deserializes a JSON string to a generic type using {@link TypeReference}.
     *
     * <p>Use this overload when the target type has type parameters (e.g. {@code List<Foo>}).
     *
     * @throws com.clean.common.proxy.exception.ProxyException if deserialization fails
     */
    protected <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException ex) {
            throw new com.clean.common.proxy.exception.ProxyException(
                    "Failed to deserialize response body: " + ex.getMessage(), ex);
        }
    }
}
