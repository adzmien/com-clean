package com.clean.common.proxy.soap;

import com.clean.common.proxy.contract.Transport;
import com.clean.common.proxy.dto.ProxyRequest;
import com.clean.common.proxy.dto.ProxyResponse;
import com.clean.common.proxy.exception.ProxyException;
import com.clean.common.proxy.properties.CleanProxyProperties.SoapConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SOAP {@link Transport} implementation using a lightweight raw HTTP POST approach.
 *
 * <p>SOAP over HTTP is fundamentally a POST request with:
 * <ul>
 *   <li>{@code Content-Type: text/xml; charset=utf-8}</li>
 *   <li>A raw SOAP envelope as the request body</li>
 *   <li>Optional {@code SOAPAction} header</li>
 * </ul>
 *
 * <p>This avoids Spring-WS / JAXB dependencies — the consuming proxy class handles
 * (un)marshalling of the XML envelope. If a full JAXB/Spring-WS stack is required later,
 * a {@code SpringWsSoapTransport} can be added as a new Strategy without modifying this class (OCP).
 *
 * <p>Pattern: Strategy (GoF). SRP: only executes SOAP-over-HTTP calls.
 */
public class SoapTransport implements Transport {

    private static final String SOAP_CONTENT_TYPE = "text/xml;charset=UTF-8";

    private final WebClient webClient;
    private final SoapConfig config;

    public SoapTransport(WebClient webClient, SoapConfig config) {
        this.webClient = webClient;
        this.config = config;
    }

    @Override
    public ProxyResponse execute(ProxyRequest request) {
        long start = System.currentTimeMillis();

        String fullUri = resolveUri(request.getUri());

        if (!StringUtils.hasText(request.getBody())) {
            throw new ProxyException("SOAP request body (envelope) must not be empty for uri=" + fullUri);
        }

        try {
            WebClient.RequestBodySpec requestSpec = webClient
                    .post()
                    .uri(fullUri)
                    .header(HttpHeaders.CONTENT_TYPE, SOAP_CONTENT_TYPE);

            if (request.getHeaders() != null) {
                request.getHeaders().forEach(requestSpec::header);
            }

            return requestSpec.bodyValue(request.getBody())
                    .exchangeToMono(resp -> resp.bodyToMono(String.class)
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
                            }))
                    .block();

        } catch (WebClientRequestException ex) {
            throw new ProxyException("SOAP call failed for uri=" + fullUri + ": " + ex.getMessage(), ex);
        }
    }

    private String resolveUri(String uri) {
        if (uri == null) return "";
        return uri.startsWith("http") ? uri : config.getBaseUrl() + uri;
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
