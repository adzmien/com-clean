package com.clean.jpa.base.service;

import com.clean.jpa.base.dto.PageDTO;
import com.clean.jpa.base.contract.PaginationRequest;
import com.clean.jpa.base.query.DefaultEntityQuery;
import com.clean.jpa.base.query.QueryBuilder;
import com.clean.jpa.base.repository.BaseJpaRepository;
import com.clean.jpa.constant.QueryMode;
import com.clean.jpa.mapper.ReadMapper;
import com.clean.jpa.util.FilterSpecificationUtil;
import com.clean.jpa.util.PageableUtil;
import com.clean.jpa.util.QueryBuilderUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Abstract base service providing standard read-only operations for JPA entities.
 *
 * <p>For CRUD operations, extend {@link BaseJpaCrudService} instead.
 *
 * @param <E>  JPA entity type
 * @param <ID> primary key type
 * @param <D>  DTO type produced by the mapper
 */
@Slf4j
public abstract class BaseJpaReadService<E, ID, D> implements ReadService<ID, D> {

    /** Maximum number of records returned by unbounded list queries. */
    protected static final int DEFAULT_MAX_LIST_SIZE = 1000;
    protected static final int DEFAULT_BATCH_SIZE = 1000;
    protected static final int MAX_BATCH_SIZE = 1000;

    protected final BaseJpaRepository<E, ID> repository;
    protected final ReadMapper<E, D> mapper;
    private final Class<E> entityClass;
    private final Set<String> allowedColumns;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * @param repository     JPA repository for the entity
     * @param mapper         read-only mapper (entity → DTO)
     * @param entityClass    the entity class (avoids reflective generic resolution)
     * @param allowedColumns columns permitted in projected queries; pass {@code Set.of()} if
     *                       projection is not used by this service
     */
    protected BaseJpaReadService(BaseJpaRepository<E, ID> repository, ReadMapper<E, D> mapper,
                                  Class<E> entityClass, Set<String> allowedColumns) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityClass = entityClass;
        this.allowedColumns = allowedColumns;
    }

    /**
     * Creates a fresh fluent {@link QueryBuilder} scoped to this service's entity type and mapper.
     */
    public QueryBuilder<E, D> query() {
        return new DefaultEntityQuery<>(entityManager, entityClass, mapper, repository);
    }

    @Transactional(readOnly = true)
    public D findById(ID id) {
        log.debug("findById: looking up entity by id [{}]", id);
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public <F> Optional<D> findOneBySpecification(F filterDto) {
        log.debug("findOneBySpecification: building specification from filter [{}]",
                filterDto != null ? filterDto.getClass().getSimpleName() : "null");
        Specification<E> spec = FilterSpecificationUtil.toSpecification(filterDto);
        return repository.findOne(spec).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public <F> List<D> findListBySpecification(F filterDto) {
        log.debug("findListBySpecification: building specification from filter [{}]",
                filterDto != null ? filterDto.getClass().getSimpleName() : "null");
        Specification<E> spec = FilterSpecificationUtil.toSpecification(filterDto);
        Pageable limit = PageRequest.of(0, DEFAULT_MAX_LIST_SIZE);
        return repository.findAll(spec, limit).getContent().stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public <F> PageDTO<D> findPageBySpecification(F filterDto, PaginationRequest paginationRequest) {
        log.debug("findPageBySpecification: building specification from filter [{}]",
                filterDto != null ? filterDto.getClass().getSimpleName() : "null");
        Specification<E> spec = FilterSpecificationUtil.toSpecification(filterDto);
        Pageable pageable = PageableUtil.toPageable(paginationRequest);
        Page<D> page = repository.findAll(spec, pageable).map(mapper::toDto);
        return PageDTO.fromPage(page, paginationRequest);
    }

    @Transactional(readOnly = true)
    public <F> void forEachBatchBySpecification(F filterDto, Integer batchSize, Consumer<List<D>> consumer) {
        Objects.requireNonNull(consumer, "consumer must not be null");

        int resolvedBatchSize = normalizeBatchSize(batchSize);
        Specification<E> spec = resolveBatchSpecification(filterDto);
        int pageIndex = 0;

        while (true) {
            Pageable pageable = PageRequest.of(pageIndex, resolvedBatchSize, Sort.by("id").ascending());
            Page<E> page = repository.findAll(spec, pageable);
            List<E> entities = page.getContent();
            if (entities.isEmpty()) {
                break;
            }

            List<D> batch = entities.stream().map(mapper::toDto).toList();
            consumer.accept(batch);

            if (!page.hasNext()) {
                break;
            }
            pageIndex++;
        }
    }

    @Transactional(readOnly = true)
    public void forEachBatch(Integer batchSize, Consumer<List<D>> consumer) {
        forEachBatchBySpecification(null, batchSize, consumer);
    }

    @Transactional(readOnly = true)
    public <F> List<D> findAllBySpecificationInBatches(F filterDto, Integer batchSize) {
        List<D> result = new ArrayList<>();
        forEachBatchBySpecification(filterDto, batchSize, result::addAll);
        return result;
    }

    @Transactional(readOnly = true)
    public List<D> findAllInBatches(Integer batchSize) {
        return findAllBySpecificationInBatches(null, batchSize);
    }

    // -------------------------------------------------------------------------
    // Projected queries — column-selected results returned as Map<String, Object>
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public <F> Optional<Map<String, Object>> findOneProjected(F filterDto, List<String> columns, QueryMode mode) {
        QueryBuilderUtil.validateColumns(columns, allowedColumns);
        return resolveMode(filterDto, mode)
                .select(columns.toArray(String[]::new))
                .findOneProjected();
    }

    @Transactional(readOnly = true)
    public <F> List<Map<String, Object>> findListProjected(F filterDto, List<String> columns, QueryMode mode) {
        QueryBuilderUtil.validateColumns(columns, allowedColumns);
        return resolveMode(filterDto, mode)
                .select(columns.toArray(String[]::new))
                .findListProjected();
    }

    @Transactional(readOnly = true)
    public <F> PageDTO<Map<String, Object>> findPageProjected(F filterDto, List<String> columns, QueryMode mode,
                                                               PaginationRequest paginationRequest) {
        QueryBuilderUtil.validateColumns(columns, allowedColumns);
        return resolveMode(filterDto, mode)
                .select(columns.toArray(String[]::new))
                .findPageProjected(paginationRequest);
    }

    protected <F> QueryBuilder<E, D> resolveMode(F filterDto, QueryMode mode) {
        return mode == QueryMode.SEARCH
                ? query().search(filterDto)
                : query().filter(filterDto);
    }

    private int normalizeBatchSize(Integer requested) {
        if (requested == null || requested <= 0) {
            return DEFAULT_BATCH_SIZE;
        }
        return Math.min(requested, MAX_BATCH_SIZE);
    }

    private <F> Specification<E> resolveBatchSpecification(F filterDto) {
        if (filterDto == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return FilterSpecificationUtil.toSpecification(filterDto);
    }
}
