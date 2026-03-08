package com.clean.jpa.util;

import com.clean.jpa.base.contract.PaginationRequest;
import com.clean.jpa.base.dto.PageQueryDTO;
import com.clean.jpa.base.dto.SortDirection;
import com.clean.jpa.base.dto.SortOrderDTO;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageableUtilTest {

    private static PageQueryDTO<?> req(Integer page, Integer size, List<SortOrderDTO> sort) {
        PageQueryDTO<Object> r = new PageQueryDTO<>();
        r.setPage(page);
        r.setSize(size);
        r.setSort(sort);
        return r;
    }

    private static SortOrderDTO order(String field, SortDirection direction) {
        return SortOrderDTO.builder()
                .field(field)
                .direction(direction)
                .build();
    }

    @Test
    void convertsOneBased_toZeroBased() {
        Pageable p = PageableUtil.toPageable(req(1, 20, List.of()));
        assertThat(p.getPageNumber()).isZero();
    }

    @Test
    void nullRequest_usesDefaults() {
        Pageable p = PageableUtil.toPageable(null);
        assertThat(p.getPageNumber()).isZero();
        assertThat(p.getPageSize()).isEqualTo(PaginationRequest.DEFAULT_SIZE);
        assertThat(p.getSort().isUnsorted()).isTrue();
    }

    @Test
    void page3_becomesOffset2() {
        Pageable p = PageableUtil.toPageable(req(3, 10, List.of()));
        assertThat(p.getPageNumber()).isEqualTo(2);
    }

    @Test
    void pageZeroOrNegative_treatedAsPageOne() {
        assertThat(PageableUtil.toPageable(req(0, 10, List.of())).getPageNumber()).isZero();
        assertThat(PageableUtil.toPageable(req(-5, 10, List.of())).getPageNumber()).isZero();
    }

    @Test
    void nullPage_usesDefault() {
        assertThat(PageableUtil.toPageable(req(null, 10, List.of())).getPageNumber()).isZero();
    }

    @Test
    void pageSizeCappedAtMax() {
        Pageable p = PageableUtil.toPageable(req(1, 100_000, List.of()));
        assertThat(p.getPageSize()).isEqualTo(PageableUtil.MAX_PAGE_SIZE);
    }

    @Test
    void pageSizeBelowMax_usedAsIs() {
        Pageable p = PageableUtil.toPageable(req(1, 50, List.of()));
        assertThat(p.getPageSize()).isEqualTo(50);
    }

    @Test
    void invalidPageSize_usesDefault() {
        Pageable p = PageableUtil.toPageable(req(1, 0, List.of()));
        assertThat(p.getPageSize()).isEqualTo(PaginationRequest.DEFAULT_SIZE);
    }

    @Test
    void multiSort_appliedWithDirection() {
        Pageable p = PageableUtil.toPageable(req(1, 20, List.of(
                order("propKey", SortDirection.ASC),
                order("createdOn", SortDirection.DESC)
        )));
        assertThat(p.getSort().getOrderFor("propKey")).isNotNull();
        assertThat(p.getSort().getOrderFor("propKey").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(p.getSort().getOrderFor("createdOn")).isNotNull();
        assertThat(p.getSort().getOrderFor("createdOn").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void emptySort_resultIsUnsorted() {
        Pageable p = PageableUtil.toPageable(req(1, 20, List.of()));
        assertThat(p.getSort().isUnsorted()).isTrue();
    }

    @Test
    void blankSortField_resultIsUnsorted() {
        Pageable p = PageableUtil.toPageable(req(1, 20, List.of(order(" ", SortDirection.ASC))));
        assertThat(p.getSort().isUnsorted()).isTrue();
    }
}
