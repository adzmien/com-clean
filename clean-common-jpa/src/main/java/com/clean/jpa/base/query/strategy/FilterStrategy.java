package com.clean.jpa.base.query.strategy;

import org.springframework.data.jpa.domain.Specification;

/**
 * Strategy contract for building a JPA {@link Specification} from a filter POJO.
 *
 * <p>Implementations define how field values are matched (exact equality, LIKE, range, IN, etc.).
 * New matching modes can be added by implementing this interface without modifying existing code
 * (Open/Closed Principle).
 */
public interface FilterStrategy {

    /**
     * Converts a filter POJO into a {@link Specification} for entity type {@code E}.
     *
     * @param filterDto POJO whose non-null properties drive the WHERE clause; must not be null
     * @param <E>       JPA entity type
     * @return a {@link Specification} combining all non-null property predicates with AND
     */
    <E> Specification<E> toSpecification(Object filterDto);
}
