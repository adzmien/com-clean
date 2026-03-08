package com.clean.jpa.base.query.strategy;

import com.clean.jpa.util.FilterSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

/**
 * {@link FilterStrategy} implementation that applies case-insensitive LIKE matching
 * for {@code String} fields and exact equality for all other types.
 *
 * <p>Suitable for search/list operations where partial text matching is desired.
 */
public class LikeFilterStrategy implements FilterStrategy {

    @Override
    public <E> Specification<E> toSpecification(Object filterDto) {
        return FilterSpecificationUtil.toSearchSpecification(filterDto);
    }
}
