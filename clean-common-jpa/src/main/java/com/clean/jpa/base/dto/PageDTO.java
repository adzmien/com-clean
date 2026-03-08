package com.clean.jpa.base.dto;

import com.clean.jpa.base.contract.PaginationRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private PageMetaDTO pagination = new PageMetaDTO();

    @Builder.Default
    private List<T> items = new ArrayList<>();

    public static <T> PageDTO<T> fromPage(Page<T> page, PaginationRequest request) {
        List<SortOrderDTO> sort = request != null
                ? request.normalizedSort()
                : mapSpringSort(page.getSort());
        return PageDTO.<T>builder()
                .pagination(PageMetaDTO.builder()
                        .totalRecords(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .currentPage(page.getNumber() + 1)
                        .pageSize(page.getSize())
                        .sort(sort)
                        .build())
                .items(page.getContent() == null ? List.of() : page.getContent())
                .build();
    }

    public static <T> PageDTO<T> fromContent(
            List<T> content,
            long totalRecords,
            PaginationRequest request) {
        int pageSize = request == null
                ? PaginationRequest.DEFAULT_SIZE
                : request.normalizedSize(Integer.MAX_VALUE);
        int currentPage = request == null
                ? PaginationRequest.DEFAULT_PAGE
                : request.normalizedPage();
        int totalPages = pageSize <= 0 ? 0 : (int) Math.ceil((double) totalRecords / pageSize);
        return PageDTO.<T>builder()
                .pagination(PageMetaDTO.builder()
                        .totalRecords(totalRecords)
                        .totalPages(totalPages)
                        .currentPage(currentPage)
                        .pageSize(pageSize)
                        .sort(request == null ? List.of() : request.normalizedSort())
                        .build())
                .items(content == null ? List.of() : content)
                .build();
    }

    private static List<SortOrderDTO> mapSpringSort(Sort springSort) {
        if (springSort == null || springSort.isUnsorted()) {
            return List.of();
        }
        List<SortOrderDTO> sortOrders = new ArrayList<>();
        for (Sort.Order order : springSort) {
            sortOrders.add(SortOrderDTO.builder()
                    .field(order.getProperty())
                    .direction(order.getDirection().isDescending() ? SortDirection.DESC : SortDirection.ASC)
                    .build());
        }
        return sortOrders;
    }
}
