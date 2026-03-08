package com.clean.jpa.base.dto;

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
public class SortOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String field;

    @Builder.Default
    private SortDirection direction = SortDirection.ASC;
}
