package com.clean.jpa.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FilterSpecificationUtilTest {

    @Getter
    @Setter
    static class Filter {
        private String name;
        private Integer age;
    }

    @SuppressWarnings("unchecked")
    private static Root<Object> mockRoot() {
        Root<Object> root = mock(Root.class);
        Path<Object> path = mock(Path.class);
        when(root.get(anyString())).thenReturn(path);
        when(root.getJavaType()).thenReturn((Class) Object.class);
        return root;
    }

    private static CriteriaBuilder mockCb(Predicate conjunction, Predicate and) {
        Predicate equalPredicate = mock(Predicate.class);
        Predicate likePredicate  = mock(Predicate.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class, invocation -> {
            String name = invocation.getMethod().getName();
            return switch (name) {
                case "conjunction" -> conjunction;
                case "and"         -> and;
                case "equal"       -> equalPredicate;
                case "like"        -> likePredicate;
                case "lower"       -> invocation.getArgument(0); // pass-through
                default            -> null;
            };
        });
        return cb;
    }

    @Test
    void toSpecification_nullFilterDto_throwsIllegalArgument() {
        assertThatThrownBy(() -> FilterSpecificationUtil.toSpecification(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toSearchSpecification_nullFilterDto_throwsIllegalArgument() {
        assertThatThrownBy(() -> FilterSpecificationUtil.toSearchSpecification(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void toSpecification_allNullProperties_returnsConjunction() {
        Filter f = new Filter(); // all null
        Specification<Object> spec = FilterSpecificationUtil.toSpecification(f);

        Predicate conjunction = mock(Predicate.class);
        CriteriaBuilder cb = mockCb(conjunction, mock(Predicate.class));
        Root<Object> root = mockRoot();
        CriteriaQuery<Object> cq = mock(CriteriaQuery.class);

        Predicate result = spec.toPredicate(root, cq, cb);
        assertThat(result).isEqualTo(conjunction);
    }

    @Test
    @SuppressWarnings("unchecked")
    void toSpecification_withValues_callsEqual() {
        Filter f = new Filter();
        f.setName("alice");
        f.setAge(30);
        Specification<Object> spec = FilterSpecificationUtil.toSpecification(f);

        CriteriaBuilder cb = mockCb(mock(Predicate.class), mock(Predicate.class));
        Root<Object> root = mockRoot();
        CriteriaQuery<Object> cq = mock(CriteriaQuery.class);

        spec.toPredicate(root, cq, cb);
        verify(cb).and(any(Predicate[].class));   // combined with AND
        verify(cb, never()).like(any(), anyString()); // exact mode — no LIKE
    }

    @Test
    @SuppressWarnings("unchecked")
    void toSearchSpecification_withStringValue_callsLike() {
        Filter f = new Filter();
        f.setName("ali");
        Specification<Object> spec = FilterSpecificationUtil.toSearchSpecification(f);

        Predicate and = mock(Predicate.class);
        CriteriaBuilder cb = mockCb(mock(Predicate.class), and);
        Root<Object> root = mockRoot();
        CriteriaQuery<Object> cq = mock(CriteriaQuery.class);

        Predicate result = spec.toPredicate(root, cq, cb);
        assertThat(result).isEqualTo(and);
        verify(cb).like(any(), anyString()); // search mode — uses LIKE
    }

    @Test
    @SuppressWarnings("unchecked")
    void toSearchSpecification_blankString_treatedAsNull() {
        Filter f = new Filter();
        f.setName("   "); // blank
        Specification<Object> spec = FilterSpecificationUtil.toSearchSpecification(f);

        Predicate conjunction = mock(Predicate.class);
        CriteriaBuilder cb = mockCb(conjunction, mock(Predicate.class));
        Root<Object> root = mockRoot();
        CriteriaQuery<Object> cq = mock(CriteriaQuery.class);

        Predicate result = spec.toPredicate(root, cq, cb);
        assertThat(result).isEqualTo(conjunction); // blank treated as no filter
    }
}
