package com.clean.common.proxy.exception;

/**
 * Unchecked exception wrapping all transport failures.
 *
 * <p>Thrown by transport implementations when an outbound call fails
 * (connection refused, timeout, no mock match, etc.).
 */
public class ProxyException extends RuntimeException {

    private final int statusCode;

    public ProxyException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public ProxyException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public ProxyException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public ProxyException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
