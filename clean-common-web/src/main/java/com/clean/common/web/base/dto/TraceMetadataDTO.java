package com.clean.common.web.base.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class TraceMetadataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String clientTraceNo;
    private String serverTraceNo;
    private String referenceNo;
    private String transactionType;
    private String transactionCode;
}
