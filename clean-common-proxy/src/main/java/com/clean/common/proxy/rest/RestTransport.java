package com.clean.common.proxy.rest;

import com.clean.common.proxy.contract.Transport;
import com.clean.common.proxy.dto.ProxyRequest;
import com.clean.common.proxy.dto.ProxyResponse;
import com.clean.common.proxy.exception.ProxyException;
import com.clean.common.proxy.properties.CleanProxyProperties.RestConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST {@link Transport} implementation using Spring {@link WebClient}.
 *
 * <p>Replaces the core logic of the legacy {@code RestProxyBase.doSend()} method
 * with a clean, config-driven, enum-based approach.
 *
 * <p>Pattern: Strategy (GoF). SRP: only executes REST calls.
 * OCP: adding retry, circuit breaker decorates via Decorator pattern, not here.
 */
public class RestTransport implements Transport {

    private final WebClient webClient;
    private final RestConfig config;

    public RestTransport(WebClient webClient, RestConfig config) {
        this.webClient = webClient;
        this.config = config;
    }

    @Override
    public ProxyResponse execute(ProxyRequest request) {
        long start = System.currentTimeMillis();

        String fullUri = resolveUri(request.getUri());
        HttpMethod httpMethod = resolveHttpMethod(request.getMethod());

        try {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient
                    .method(httpMethod)
                    .uri(fullUri);

            if (request.getHeaders() != null) {
                request.getHeaders().forEach(requestSpec::header);
            }

            if (StringUtils.hasText(request.getBody())
                    && (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH)) {
                requestSpec = ((WebClient.RequestBodySpec) requestSpec).bodyValue(request.getBody());
            }

            return requestSpec.exchangeToMono(resp -> {
                return resp.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(body -> {
                            Map<String, String> responseHeaders = flattenHeaders(resp.headers().asHttpHeaders());
                            long duration = System.currentTimeMillis() - start;
                            return ProxyResponse.builder()
                                    .statusCode(resp.statusCode().value())
                                    .body(body)
                                    .headers(responseHeaders)
                                    .durationMillis(duration)
                                    .build();
                        });
            }).block();

        } catch (WebClientRequestException ex) {
            throw new ProxyException("REST call failed for uri=" + fullUri + ": " + ex.getMessage(), ex);
        }
    }

    private String resolveUri(String uri) {
        if (uri == null) return "";
        return uri.startsWith("http") ? uri : config.getBaseUrl() + uri;
    }

    private HttpMethod resolveHttpMethod(ProxyRequest.HttpMethod method) {
        if (method == null) return HttpMethod.GET;
        return switch (method) {
            case GET    -> HttpMethod.GET;
            case POST   -> HttpMethod.POST;
            case PUT    -> HttpMethod.PUT;
            case DELETE -> HttpMethod.DELETE;
            case PATCH  -> HttpMethod.PATCH;
        };
    }

    private Map<String, String> flattenHeaders(HttpHeaders headers) {
        if (headers == null) return new HashMap<>();
        return headers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() != null ? String.join(",", e.getValue()) : ""
                ));
    }
}
