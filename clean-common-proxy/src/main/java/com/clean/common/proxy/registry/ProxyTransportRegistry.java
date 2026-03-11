package com.clean.common.proxy.registry;

import com.clean.common.proxy.contract.Transport;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Registry of named {@link Transport} instances, keyed by endpoint name.
 *
 * <p>Holds separate maps for REST and SOAP transports so endpoint names can be
 * reused across transport types without collision (e.g. a "payment" REST endpoint
 * and a hypothetical "payment" SOAP endpoint are independent entries).
 *
 * <p>The registry is built at Spring context startup by {@code CleanProxyAutoConfiguration}
 * and populated from {@code clean.proxy.rest.endpoints.*} /
 * {@code clean.proxy.soap.endpoints.*} properties.
 *
 * <p>A "default" entry is also registered when the global {@code clean.proxy.rest.base-url} /
 * {@code clean.proxy.soap.base-url} is set, ensuring backward compatibility with
 * {@code @Qualifier("restTransport")} / {@code @Qualifier("soapTransport")} injection.
 *
 * <p>Lookup is fail-fast: an unrecognised endpoint name throws {@link IllegalArgumentException}
 * at Spring context startup — misconfigured names are caught before the application serves traffic.
 *
 * <p>Pattern: Registry (structural). SRP: named transport lookup only.
 */
public class ProxyTransportRegistry {

    private final Map<String, Transport> restTransports;
    private final Map<String, Transport> soapTransports;

    public ProxyTransportRegistry(Map<String, Transport> restTransports,
                                  Map<String, Transport> soapTransports) {
        this.restTransports = Collections.unmodifiableMap(restTransports);
        this.soapTransports = Collections.unmodifiableMap(soapTransports);
    }

    /**
     * Returns the REST {@link Transport} registered under {@code name}.
     *
     * @param name endpoint name (e.g. "payment", "report", "default")
     * @return the transport — never {@code null}
     * @throws IllegalArgumentException if no transport is registered under that name
     */
    public Transport getRestTransport(String name) {
        Transport t = restTransports.get(name);
        if (t == null) {
            throw new IllegalArgumentException(
                "No REST transport registered for endpoint '" + name + "'. " +
                "Available endpoints: " + restTransports.keySet() + ". " +
                "Check clean.proxy.rest.endpoints.{name}.base-url in your application properties.");
        }
        return t;
    }

    /**
     * Returns the SOAP {@link Transport} registered under {@code name}.
     *
     * @param name endpoint name (e.g. "message", "shop", "default")
     * @return the transport — never {@code null}
     * @throws IllegalArgumentException if no transport is registered under that name
     */
    public Transport getSoapTransport(String name) {
        Transport t = soapTransports.get(name);
        if (t == null) {
            throw new IllegalArgumentException(
                "No SOAP transport registered for endpoint '" + name + "'. " +
                "Available endpoints: " + soapTransports.keySet() + ". " +
                "Check clean.proxy.soap.endpoints.{name}.base-url in your application properties.");
        }
        return t;
    }

    /** Returns {@code true} if a REST transport is registered under {@code name}. */
    public boolean hasRestTransport(String name) {
        return restTransports.containsKey(name);
    }

    /** Returns {@code true} if a SOAP transport is registered under {@code name}. */
    public boolean hasSoapTransport(String name) {
        return soapTransports.containsKey(name);
    }

    /** Returns an immutable view of all registered REST endpoint names. */
    public Set<String> restEndpointNames() {
        return restTransports.keySet();
    }

    /** Returns an immutable view of all registered SOAP endpoint names. */
    public Set<String> soapEndpointNames() {
        return soapTransports.keySet();
    }
}
