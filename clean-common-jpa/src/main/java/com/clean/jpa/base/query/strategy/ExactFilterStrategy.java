package com.clean.jpa.base.query.strategy;

import com.clean.jpa.util.FilterSpecificationUtil;
import org.springframework.data.jpa.domain.Specification;

/**
 * {@link FilterStrategy} implementation that uses exact equality for all field types.
 *
 * <p>Suitable for unique-lookup operations where partial String matching would risk
 * returning multiple rows (e.g. {@code findOneBySpecification}).
 */
public class ExactFilterStrategy implements FilterStrategy {

    @Override
    public <E> Specification<E> toSpecification(Object filterDto) {
        return FilterSpecificationUtil.toSpecification(filterDto);
    }
}
