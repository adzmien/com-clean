package com.clean.jpa.base.service;

import com.clean.jpa.base.dto.PageDTO;
import com.clean.jpa.base.contract.PaginationRequest;
import com.clean.jpa.constant.QueryMode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Read-only service contract for entity queries.
 *
 * <p>Controllers and other consumers depend on this interface rather than concrete
 * service implementations, satisfying the Dependency Inversion Principle (DIP).
 * This also enables the Decorator pattern for cross-cutting concerns such as
 * caching, logging, and authorization.
 *
 * @param <ID> primary key type
 * @param <D>  DTO type
 */
public interface ReadService<ID, D> {

    D findById(ID id);

    <F> Optional<D> findOneBySpecification(F filterDto);

    /**
     * Returns entities matching all non-null filter properties using exact equality.
     * Results are capped at a maximum size defined by the implementation.
     */
    <F> List<D> findListBySpecification(F filterDto);

    /**
     * Returns a paginated result of entities matching all non-null filter properties
     * using exact equality.
     */
    <F> PageDTO<D> findPageBySpecification(F filterDto, PaginationRequest paginationRequest);

    <F> void forEachBatchBySpecification(F filterDto, Integer batchSize, Consumer<List<D>> consumer);

    void forEachBatch(Integer batchSize, Consumer<List<D>> consumer);

    <F> List<D> findAllBySpecificationInBatches(F filterDto, Integer batchSize);

    List<D> findAllInBatches(Integer batchSize);

    <F> Optional<Map<String, Object>> findOneProjected(F filterDto, List<String> columns, QueryMode mode);

    <F> List<Map<String, Object>> findListProjected(F filterDto, List<String> columns, QueryMode mode);

    <F> PageDTO<Map<String, Object>> findPageProjected(F filterDto, List<String> columns, QueryMode mode,
                                                        PaginationRequest paginationRequest);
}
