package com.clean.jpa.exception;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public class ColumnValidationException extends RuntimeException {

    private final @NonNull HttpStatus status;

    public ColumnValidationException(String message, @NonNull HttpStatus status) {
        super(message);
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    @NonNull
    public HttpStatus getStatus() {
        return status;
    }
}
