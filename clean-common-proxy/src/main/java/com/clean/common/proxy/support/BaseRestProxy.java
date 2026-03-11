package com.clean.common.proxy.support;

import com.clean.common.proxy.contract.Transport;
import com.clean.common.proxy.logger.ProxyExchangeLogger;
import com.clean.common.proxy.registry.ProxyTransportRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Convenience base class for REST-specific proxy implementations.
 *
 * <p><strong>Preferred constructor</strong> — registry-based, for named endpoints:
 * <pre>
 * {@literal @}Component
 * public class PaymentProxy extends BaseRestProxy {
 *
 *     public PaymentProxy(ProxyTransportRegistry registry,
 *                         ProxyExchangeLogger logger,
 *                         ObjectMapper objectMapper) {
 *         super(registry, "payment", logger, objectMapper);
 *     }
 * }
 * </pre>
 *
 * <p>The endpoint name must match a key under {@code clean.proxy.rest.endpoints.*} in
 * {@code application.properties}. An unrecognised name throws {@link IllegalArgumentException}
 * at Spring context startup (fail-fast).
 *
 * <p><strong>Deprecated constructor</strong> — kept for backward compatibility with existing
 * subclasses that inject {@code @Qualifier("restTransport")} directly. Remove after migration.
 *
 * <p>Pattern: Template Method (extends {@link BaseProxy}).
 * LSP: substitutable for {@code BaseProxy} everywhere.
 */
public abstract class BaseRestProxy extends BaseProxy {

    /**
     * Registry-based constructor — preferred for all new proxy implementations.
     *
     * <p>Resolves the transport for {@code endpointName} from the registry at construction
     * time. Fails fast at startup if the name is not configured.
     *
     * @param registry     the transport registry populated by auto-configuration
     * @param endpointName the named endpoint (must match a key in {@code clean.proxy.rest.endpoints.*})
     * @param exchangeLogger exchange logger
     * @param objectMapper JSON mapper
     */
    protected BaseRestProxy(ProxyTransportRegistry registry,
                             String endpointName,
                             ProxyExchangeLogger exchangeLogger,
                             ObjectMapper objectMapper) {
        super(registry.getRestTransport(endpointName), exchangeLogger, objectMapper);
    }

    /**
     * @deprecated Use the registry-based constructor instead.
     *             Kept for backward compatibility with existing proxy subclasses
     *             that inject {@code @Qualifier("restTransport")} directly.
     *             Will be removed in a future release.
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    protected BaseRestProxy(@Qualifier("restTransport") Transport transport,
                             ProxyExchangeLogger exchangeLogger,
                             ObjectMapper objectMapper) {
        super(transport, exchangeLogger, objectMapper);
    }
}
