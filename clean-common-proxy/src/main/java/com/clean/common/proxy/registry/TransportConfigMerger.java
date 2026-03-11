package com.clean.common.proxy.registry;

import com.clean.common.proxy.properties.CleanProxyProperties.PoolConfig;
import com.clean.common.proxy.properties.CleanProxyProperties.RestConfig;
import com.clean.common.proxy.properties.CleanProxyProperties.SoapConfig;
import com.clean.common.proxy.properties.CleanProxyProperties.TlsConfig;
import com.clean.common.proxy.properties.EndpointRestConfig;
import com.clean.common.proxy.properties.EndpointRestConfig.EndpointPoolConfig;
import com.clean.common.proxy.properties.EndpointRestConfig.EndpointTlsConfig;
import com.clean.common.proxy.properties.EndpointSoapConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Pure utility that produces effective {@link RestConfig} / {@link SoapConfig} instances by
 * overlaying a named endpoint's nullable field overrides on top of the global defaults.
 *
 * <p>Rules:
 * <ul>
 *   <li>A {@code null} override field means "inherit the global default".</li>
 *   <li>Default headers are merged: global first, endpoint-specific overrides last.</li>
 *   <li>Connection pool names auto-derive as {@code "{name}-rest-pool"} / {@code "{name}-soap-pool"}
 *       when not explicitly set, ensuring each named endpoint gets an isolated pool.</li>
 * </ul>
 *
 * <p>No Spring dependency — pure Java utility. SRP: merge logic only.
 */
public final class TransportConfigMerger {

    private TransportConfigMerger() {}

    /**
     * Returns an effective {@link RestConfig} with global defaults overridden by any
     * non-null fields in {@code ep}.
     *
     * @param global global REST config (source of defaults)
     * @param ep     per-endpoint overrides (nullable fields)
     * @param name   endpoint name — used to derive pool name if not explicitly set
     * @return new merged RestConfig ready to pass to ReactorNettyClientFactory
     */
    public static RestConfig mergeRest(RestConfig global, EndpointRestConfig ep, String name) {
        RestConfig eff = new RestConfig();
        eff.setEnabled(global.isEnabled());
        eff.setBaseUrl(ep.getBaseUrl() != null ? ep.getBaseUrl() : global.getBaseUrl());
        eff.setConnectTimeoutMs(coalesce(ep.getConnectTimeoutMs(), global.getConnectTimeoutMs()));
        eff.setReadTimeoutMs(coalesce(ep.getReadTimeoutMs(), global.getReadTimeoutMs()));
        eff.setResponseTimeoutMs(coalesce(ep.getResponseTimeoutMs(), global.getResponseTimeoutMs()));
        eff.setWriteTimeoutMs(coalesce(ep.getWriteTimeoutMs(), global.getWriteTimeoutMs()));
        eff.setMaxInMemorySizeMb(coalesce(ep.getMaxInMemorySizeMb(), global.getMaxInMemorySizeMb()));
        eff.setDefaultHeaders(mergeHeaders(global.getDefaultHeaders(), ep.getDefaultHeaders()));
        eff.setPool(mergeRestPool(global.getPool(), ep.getPool(), name));
        eff.setTls(mergeTls(global.getTls(), ep.getTls()));
        return eff;
    }

    /**
     * Returns an effective {@link SoapConfig} with global defaults overridden by any
     * non-null fields in {@code ep}.
     *
     * @param global global SOAP config (source of defaults)
     * @param ep     per-endpoint overrides (nullable fields)
     * @param name   endpoint name — used to derive pool name if not explicitly set
     * @return new merged SoapConfig ready to pass to ReactorNettyClientFactory
     */
    public static SoapConfig mergeSoap(SoapConfig global, EndpointSoapConfig ep, String name) {
        SoapConfig eff = new SoapConfig();
        eff.setEnabled(global.isEnabled());
        eff.setBaseUrl(ep.getBaseUrl() != null ? ep.getBaseUrl() : global.getBaseUrl());
        eff.setConnectTimeoutMs(coalesce(ep.getConnectTimeoutMs(), global.getConnectTimeoutMs()));
        eff.setReadTimeoutMs(coalesce(ep.getReadTimeoutMs(), global.getReadTimeoutMs()));
        eff.setResponseTimeoutMs(coalesce(ep.getResponseTimeoutMs(), global.getResponseTimeoutMs()));
        eff.setDefaultHeaders(mergeHeaders(global.getDefaultHeaders(), ep.getDefaultHeaders()));
        eff.setPool(mergeSoapPool(global.getPool(), ep.getPool(), name));
        return eff;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static PoolConfig mergeRestPool(PoolConfig global, EndpointPoolConfig ep, String endpointName) {
        PoolConfig p = new PoolConfig();
        // Auto-derive an isolated pool name to avoid sharing pools across different endpoints.
        // Prefix with "rest-" to avoid collision with SOAP endpoints of the same name.
        String derivedName = endpointName + "-rest-pool";
        if (ep == null) {
            p.setName(derivedName);
            p.setMaxConnections(global.getMaxConnections());
            p.setPendingAcquireTimeoutMs(global.getPendingAcquireTimeoutMs());
            p.setMaxIdleTimeMs(global.getMaxIdleTimeMs());
            p.setMaxLifeTimeMs(global.getMaxLifeTimeMs());
        } else {
            p.setName(ep.getName() != null ? ep.getName() : derivedName);
            p.setMaxConnections(coalesce(ep.getMaxConnections(), global.getMaxConnections()));
            p.setPendingAcquireTimeoutMs(coalesce(ep.getPendingAcquireTimeoutMs(), global.getPendingAcquireTimeoutMs()));
            p.setMaxIdleTimeMs(coalesce(ep.getMaxIdleTimeMs(), global.getMaxIdleTimeMs()));
            p.setMaxLifeTimeMs(coalesce(ep.getMaxLifeTimeMs(), global.getMaxLifeTimeMs()));
        }
        return p;
    }

    private static PoolConfig mergeSoapPool(PoolConfig global, EndpointSoapConfig.EndpointPoolConfig ep, String endpointName) {
        PoolConfig p = new PoolConfig();
        String derivedName = endpointName + "-soap-pool";
        if (ep == null) {
            p.setName(derivedName);
            p.setMaxConnections(global.getMaxConnections());
            p.setPendingAcquireTimeoutMs(global.getPendingAcquireTimeoutMs());
            p.setMaxIdleTimeMs(global.getMaxIdleTimeMs());
            p.setMaxLifeTimeMs(global.getMaxLifeTimeMs());
        } else {
            p.setName(ep.getName() != null ? ep.getName() : derivedName);
            p.setMaxConnections(coalesce(ep.getMaxConnections(), global.getMaxConnections()));
            p.setPendingAcquireTimeoutMs(coalesce(ep.getPendingAcquireTimeoutMs(), global.getPendingAcquireTimeoutMs()));
            p.setMaxIdleTimeMs(coalesce(ep.getMaxIdleTimeMs(), global.getMaxIdleTimeMs()));
            p.setMaxLifeTimeMs(coalesce(ep.getMaxLifeTimeMs(), global.getMaxLifeTimeMs()));
        }
        return p;
    }

    private static TlsConfig mergeTls(TlsConfig global, EndpointTlsConfig ep) {
        TlsConfig t = new TlsConfig();
        t.setEnabled(global.isEnabled());  // TLS enabled/disabled not overridable per-endpoint
        if (ep == null) {
            t.setInsecure(global.isInsecure());
            t.setTrustStorePath(global.getTrustStorePath());
            t.setTrustStorePassword(global.getTrustStorePassword());
        } else {
            t.setInsecure(ep.getInsecure() != null ? ep.getInsecure() : global.isInsecure());
            t.setTrustStorePath(ep.getTrustStorePath() != null ? ep.getTrustStorePath() : global.getTrustStorePath());
            t.setTrustStorePassword(ep.getTrustStorePassword() != null ? ep.getTrustStorePassword() : global.getTrustStorePassword());
        }
        return t;
    }

    private static Map<String, String> mergeHeaders(Map<String, String> globalHeaders,
                                                     Map<String, String> endpointHeaders) {
        Map<String, String> merged = new HashMap<>(globalHeaders);
        if (endpointHeaders != null) {
            merged.putAll(endpointHeaders);
        }
        return merged;
    }

    private static <T> T coalesce(T override, T fallback) {
        return override != null ? override : fallback;
    }
}
