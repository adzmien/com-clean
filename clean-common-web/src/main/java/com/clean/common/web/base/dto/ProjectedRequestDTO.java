package com.clean.common.web.base.dto;

import java.util.ArrayList;
import java.util.List;

import com.clean.jpa.constant.QueryMode;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Request DTO for projected (column-selected) queries.
 *
 * <p>Extends {@link RequestEnvelopeDTO} with {@link #mode} and {@link #columns}
 * fields that are only relevant to projection endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectedRequestDTO<T> extends RequestEnvelopeDTO<T> {

    private static final long serialVersionUID = 2L;

    private QueryMode mode;

    @lombok.Builder.Default
    private List<String> columns = new ArrayList<>();
}
