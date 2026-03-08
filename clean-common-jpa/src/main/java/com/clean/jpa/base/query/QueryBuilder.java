package com.clean.jpa.base.query;

import com.clean.jpa.base.dto.PageDTO;
import com.clean.jpa.base.contract.PaginationRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fluent query builder contract for JPA entity queries.
 *
 * <p>Intermediate methods ({@code filter}, {@code search}, {@code select}) configure the query
 * and return a new builder instance for chaining — each call is non-destructive (immutable
 * builder pattern). Terminal methods ({@code findOne}, {@code findList}, {@code findPage},
 * {@code findOneProjected}, {@code findListProjected}) execute the query.
 *
 * <p>Obtain a fresh instance per logical query via
 * {@link com.clean.jpa.base.service.BaseJpaReadService#query()}.
 *
 * @param <E> JPA entity type
 * @param <D> DTO type produced by the mapper
 */
public interface QueryBuilder<E, D> {

    /** Builds an exact-equality {@code Specification} from all non-null properties of the filter. */
    <F> QueryBuilder<E, D> filter(F filterDto);

    /** Builds a case-insensitive LIKE {@code Specification} for {@code String} fields and exact equality for others. */
    <F> QueryBuilder<E, D> search(F filterDto);

    /** Restricts the projected {@code SELECT} to the named entity attribute names. */
    QueryBuilder<E, D> select(String... columns);

    // --- Full-entity terminal operations ---

    Optional<D> findOne();

    List<D> findList();

    PageDTO<D> findPage(PaginationRequest req);

    // --- Projected terminal operations (require prior select()) ---

    Optional<Map<String, Object>> findOneProjected();

    List<Map<String, Object>> findListProjected();

    PageDTO<Map<String, Object>> findPageProjected(PaginationRequest req);
}
