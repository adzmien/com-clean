package com.clean.common.web.base.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseEnvelopeDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    private ResponseStatusDTO status;

    @Valid
    private TraceMetadataDTO trace;

    @Valid
    private T payload;

    public static <T> ResponseEnvelopeDTO<T> ok(T payload) {
        return ResponseEnvelopeDTO.<T>builder()
                .status(ResponseStatusDTO.builder()
                        .success(true)
                        .build())
                .payload(payload)
                .build();
    }

    public static <T> ResponseEnvelopeDTO<T> fail(String statusCode, String statusDescription) {
        return fail(statusCode, statusDescription, null);
    }

    public static <T> ResponseEnvelopeDTO<T> fail(
            String statusCode,
            String statusDescription,
            String message) {
        return ResponseEnvelopeDTO.<T>builder()
                .status(ResponseStatusDTO.builder()
                        .success(false)
                        .statusCode(statusCode)
                        .statusDescription(statusDescription)
                        .message(message)
                        .build())
                .build();
    }
}
