package com.clean.jpa.base.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageDTOTest {

    @Test
    void fromPage_mapsMetadataAndItems() {
        PageQueryDTO<Void> request = PageQueryDTO.<Void>builder()
                .page(2)
                .size(2)
                .sort(List.of(SortOrderDTO.builder().field("name").direction(SortDirection.ASC).build()))
                .build();
        Page<String> page = new PageImpl<>(
                List.of("A", "B"),
                PageRequest.of(1, 2, Sort.by("name").ascending()),
                5
        );

        PageDTO<String> dto = PageDTO.fromPage(page, request);

        assertThat(dto.getItems()).containsExactly("A", "B");
        assertThat(dto.getPagination().getTotalRecords()).isEqualTo(5);
        assertThat(dto.getPagination().getTotalPages()).isEqualTo(3);
        assertThat(dto.getPagination().getCurrentPage()).isEqualTo(2);
        assertThat(dto.getPagination().getPageSize()).isEqualTo(2);
        assertThat(dto.getPagination().getSort()).hasSize(1);
        assertThat(dto.getPagination().getSort().get(0).getField()).isEqualTo("name");
        assertThat(dto.getPagination().getSort().get(0).getDirection()).isEqualTo(SortDirection.ASC);
    }

    @Test
    void fromContent_neverReturnsNullItems() {
        PageQueryDTO<Void> request = PageQueryDTO.<Void>builder()
                .page(1)
                .size(20)
                .build();

        PageDTO<String> dto = PageDTO.fromContent(null, 0, request);

        assertThat(dto.getItems()).isNotNull().isEmpty();
        assertThat(dto.getPagination().getTotalRecords()).isZero();
        assertThat(dto.getPagination().getCurrentPage()).isEqualTo(1);
        assertThat(dto.getPagination().getPageSize()).isEqualTo(20);
        assertThat(dto.getPagination().getTotalPages()).isZero();
    }
}
