package com.clean.jpa.base.contract;

import com.clean.jpa.base.dto.SortDirection;
import com.clean.jpa.base.dto.SortOrderDTO;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface PaginationRequest {

    int DEFAULT_PAGE = 1;
    int DEFAULT_SIZE = 20;

    Integer getPage();

    Integer getSize();

    List<SortOrderDTO> getSort();

    default int normalizedPage() {
        Integer page = getPage();
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    default int normalizedSize(int maxSize) {
        Integer size = getSize();
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, maxSize);
    }

    default List<SortOrderDTO> normalizedSort() {
        List<SortOrderDTO> sortList = getSort();
        if (sortList == null || sortList.isEmpty()) {
            return Collections.emptyList();
        }
        return sortList.stream()
                .filter(Objects::nonNull)
                .filter(sort -> sort.getField() != null && !sort.getField().isBlank())
                .map(sort -> SortOrderDTO.builder()
                        .field(sort.getField().trim())
                        .direction(sort.getDirection() == null ? SortDirection.ASC : sort.getDirection())
                        .build())
                .toList();
    }
}
