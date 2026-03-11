package com.clean.common.proxy.support;

import com.clean.common.proxy.contract.Transport;
import com.clean.common.proxy.logger.ProxyExchangeLogger;
import com.clean.common.proxy.registry.ProxyTransportRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Convenience base class for SOAP-specific proxy implementations.
 *
 * <p><strong>Preferred constructor</strong> — registry-based, for named endpoints:
 * <pre>
 * {@literal @}Component
 * public class MessageProxy extends BaseSoapProxy {
 *
 *     public MessageProxy(ProxyTransportRegistry registry,
 *                         ProxyExchangeLogger logger,
 *                         ObjectMapper objectMapper) {
 *         super(registry, "message", logger, objectMapper);
 *     }
 * }
 * </pre>
 *
 * <p>The endpoint name must match a key under {@code clean.proxy.soap.endpoints.*} in
 * {@code application.properties}. An unrecognised name throws {@link IllegalArgumentException}
 * at Spring context startup (fail-fast).
 *
 * <p>SOAP proxies pass a raw XML envelope string as the request body and receive a
 * raw XML string in the response. Marshalling/unmarshalling is the responsibility of
 * the concrete subclass.
 *
 * <p><strong>Deprecated constructor</strong> — kept for backward compatibility with existing
 * subclasses that inject {@code @Qualifier("soapTransport")} directly. Remove after migration.
 *
 * <p>Pattern: Template Method (extends {@link BaseProxy}).
 * LSP: substitutable for {@code BaseProxy} everywhere.
 */
public abstract class BaseSoapProxy extends BaseProxy {

    /**
     * Registry-based constructor — preferred for all new proxy implementations.
     *
     * <p>Resolves the transport for {@code endpointName} from the registry at construction
     * time. Fails fast at startup if the name is not configured.
     *
     * @param registry     the transport registry populated by auto-configuration
     * @param endpointName the named endpoint (must match a key in {@code clean.proxy.soap.endpoints.*})
     * @param exchangeLogger exchange logger
     * @param objectMapper JSON mapper
     */
    protected BaseSoapProxy(ProxyTransportRegistry registry,
                             String endpointName,
                             ProxyExchangeLogger exchangeLogger,
                             ObjectMapper objectMapper) {
        super(registry.getSoapTransport(endpointName), exchangeLogger, objectMapper);
    }

    /**
     * @deprecated Use the registry-based constructor instead.
     *             Kept for backward compatibility with existing proxy subclasses
     *             that inject {@code @Qualifier("soapTransport")} directly.
     *             Will be removed in a future release.
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    protected BaseSoapProxy(@Qualifier("soapTransport") Transport transport,
                             ProxyExchangeLogger exchangeLogger,
                             ObjectMapper objectMapper) {
        super(transport, exchangeLogger, objectMapper);
    }
}
