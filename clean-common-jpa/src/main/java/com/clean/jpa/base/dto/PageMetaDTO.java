package com.clean.jpa.base.dto;

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
public class PageMetaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalRecords;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    @Builder.Default
    private List<SortOrderDTO> sort = new ArrayList<>();
}
