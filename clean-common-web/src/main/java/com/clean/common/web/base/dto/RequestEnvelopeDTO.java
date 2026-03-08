package com.clean.common.web.base.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestEnvelopeDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    private TraceMetadataDTO trace;

    @Valid
    private T payload;
}
