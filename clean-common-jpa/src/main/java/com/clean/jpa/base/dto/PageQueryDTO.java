package com.clean.jpa.base.dto;

import com.clean.jpa.base.contract.PaginationRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageQueryDTO<T> implements PaginationRequest, Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private Integer page = PaginationRequest.DEFAULT_PAGE;

    @Builder.Default
    private Integer size = PaginationRequest.DEFAULT_SIZE;

    @Builder.Default
    private List<SortOrderDTO> sort = new ArrayList<>();

    private T filter;
}
