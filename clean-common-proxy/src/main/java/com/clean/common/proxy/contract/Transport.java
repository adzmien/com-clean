package com.clean.common.proxy.contract;

import com.clean.common.proxy.dto.ProxyRequest;
import com.clean.common.proxy.dto.ProxyResponse;

/**
 * Strategy interface for all outbound transport protocols.
 *
 * <p>Each implementation handles a specific transport protocol (REST, SOAP, Mock).
 * Concrete transports are registered as Spring beans and wrapped with
 * {@link com.clean.common.proxy.interceptor.TraceHeaderInterceptor} by the auto-configuration.
 *
 * <p>Extension point: to add a new protocol, implement this interface and register
 * the bean — no existing code needs modification (OCP).
 */
public interface Transport {

    ProxyResponse execute(ProxyRequest request);
}
