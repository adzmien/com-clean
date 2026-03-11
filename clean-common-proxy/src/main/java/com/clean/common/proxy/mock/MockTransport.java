package com.clean.common.proxy.mock;

import com.clean.common.proxy.contract.Transport;
import com.clean.common.proxy.dto.ProxyRequest;
import com.clean.common.proxy.dto.ProxyResponse;
import com.clean.common.proxy.exception.ProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock {@link Transport} that returns pre-configured responses without making real calls.
 *
 * <p>Active only when {@code clean.proxy.mock.enabled=true}. Resolves responses from
 * {@link MockResponseRegistry} by matching the request URI against registered patterns.
 *
 * <p>Behaviour when no match is found:
 * <ul>
 *   <li>If {@code failOnNoMatch=true} (default): throws {@link ProxyException}</li>
 *   <li>If {@code failOnNoMatch=false}: returns a 404 response with an empty body</li>
 * </ul>
 *
 * <p>Pattern: Strategy (GoF). SRP: only resolves and returns mock responses.
 */
public class MockTransport implements Transport {

    private static final Logger log = LoggerFactory.getLogger(MockTransport.class);

    private final MockResponseRegistry registry;
    private final boolean failOnNoMatch;

    public MockTransport(MockResponseRegistry registry, boolean failOnNoMatch) {
        this.registry = registry;
        this.failOnNoMatch = failOnNoMatch;
    }

    @Override
    public ProxyResponse execute(ProxyRequest request) {
        long start = System.currentTimeMillis();

        return registry.match(request)
                .map(def -> {
                    if (def.getDelayMs() > 0) {
                        simulateDelay(def.getDelayMs());
                    }
                    long duration = System.currentTimeMillis() - start;
                    log.debug("Mock matched uri='{}' pattern='{}' status={}", request.getUri(), def.getUriPattern(), def.getStatusCode());
                    return ProxyResponse.builder()
                            .statusCode(def.getStatusCode())
                            .body(def.getBody())
                            .headers(def.getHeaders())
                            .durationMillis(duration)
                            .build();
                })
                .orElseGet(() -> handleNoMatch(request));
    }

    private ProxyResponse handleNoMatch(ProxyRequest request) {
        String msg = "No mock definition matched uri='" + request.getUri() + "'";
        if (failOnNoMatch) {
            throw new ProxyException(404, msg);
        }
        log.warn("{} — returning 404 (failOnNoMatch=false)", msg);
        return ProxyResponse.builder()
                .statusCode(404)
                .body("")
                .durationMillis(0)
                .build();
    }

    private void simulateDelay(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
