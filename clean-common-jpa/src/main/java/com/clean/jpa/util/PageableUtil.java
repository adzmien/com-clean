package com.clean.jpa.util;

import com.clean.jpa.base.contract.PaginationRequest;
import com.clean.jpa.base.dto.SortDirection;
import com.clean.jpa.base.dto.SortOrderDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for converting a {@link PaginationRequest} into a Spring Data {@link Pageable}.
 *
 * <p>Centralises pagination construction to avoid duplication across service and query classes.
 */
public final class PageableUtil {

    /** Maximum allowed page size — prevents unbounded result-set queries. */
    public static final int MAX_PAGE_SIZE = 500;

    private PageableUtil() {
        // utility class — no instantiation
    }

    /**
     * Converts a {@link PaginationRequest} into a {@link Pageable}.
     *
     * <p>Page numbers are 1-based in the request and converted to 0-based for Spring Data.
     * If {@code page} is less than 1 it is treated as 1.
     * {@code size} is capped at {@value #MAX_PAGE_SIZE} to prevent unbounded queries.
     *
     * @param req the pagination parameters; must not be {@code null}
     * @return a {@link Pageable} ready for repository use; never {@code null}
     */
    public static Pageable toPageable(PaginationRequest req) {
        if (req == null) {
            return PageRequest.of(0, PaginationRequest.DEFAULT_SIZE, Sort.unsorted());
        }
        int page = Math.max(0, req.normalizedPage() - 1);
        int size = req.normalizedSize(MAX_PAGE_SIZE);
        Sort sort = toSpringSort(req.normalizedSort());
        return PageRequest.of(page, size, sort);
    }

    private static Sort toSpringSort(List<SortOrderDTO> sortOrders) {
        if (sortOrders == null || sortOrders.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> springOrders = new ArrayList<>();
        for (SortOrderDTO sortOrder : sortOrders) {
            if (sortOrder == null || sortOrder.getField() == null || sortOrder.getField().isBlank()) {
                continue;
            }
            Sort.Direction direction = sortOrder.getDirection() == SortDirection.DESC
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            springOrders.add(new Sort.Order(direction, sortOrder.getField().trim()));
        }
        return springOrders.isEmpty() ? Sort.unsorted() : Sort.by(springOrders);
    }
}
