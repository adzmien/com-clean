package com.clean.jpa.base.query;

import com.clean.jpa.base.dto.PageDTO;
import com.clean.jpa.base.contract.PaginationRequest;
import com.clean.jpa.base.query.strategy.ExactFilterStrategy;
import com.clean.jpa.base.query.strategy.LikeFilterStrategy;
import com.clean.jpa.base.repository.BaseJpaRepository;
import com.clean.jpa.mapper.ReadMapper;
import com.clean.jpa.util.PageableUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Default JPA Criteria-based implementation of {@link QueryBuilder}.
 *
 * <p><strong>Immutable builder:</strong> each intermediate method ({@code filter}, {@code search},
 * {@code select}) returns a <em>new</em> {@code DefaultEntityQuery} instance with the updated
 * state. The original instance is never modified.
 *
 * <p><strong>Not a Spring bean:</strong> this class is instantiated via {@code new} in
 * {@link com.clean.jpa.base.service.BaseJpaReadService#query()}.
 *
 * @param <E> JPA entity type
 * @param <D> DTO type produced by the mapper
 */
@Slf4j
public class DefaultEntityQuery<E, D> implements QueryBuilder<E, D> {

    // Stateless strategy singletons — avoid per-call allocation
    private static final ExactFilterStrategy EXACT_STRATEGY = new ExactFilterStrategy();
    private static final LikeFilterStrategy LIKE_STRATEGY = new LikeFilterStrategy();

    // Injected collaborators — immutable across all instances in a chain
    private final EntityManager entityManager;
    private final Class<E> entityClass;
    private final ReadMapper<E, D> mapper;
    private final BaseJpaRepository<E, ?> repository;

    // Query state — set once per instance; chains produce new instances
    private final Specification<E> spec;
    private final String[] selectedColumns;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Public entry-point constructor — no query state yet. */
    public DefaultEntityQuery(
            EntityManager entityManager,
            Class<E> entityClass,
            ReadMapper<E, D> mapper,
            BaseJpaRepository<E, ?> repository) {
        this(entityManager, entityClass, mapper, repository, null, null);
    }

    /** Private chaining constructor — carries accumulated state. */
    private DefaultEntityQuery(
            EntityManager entityManager,
            Class<E> entityClass,
            ReadMapper<E, D> mapper,
            BaseJpaRepository<E, ?> repository,
            Specification<E> spec,
            String[] selectedColumns) {
        this.entityManager   = Objects.requireNonNull(entityManager, "entityManager must not be null");
        this.entityClass     = Objects.requireNonNull(entityClass,   "entityClass must not be null");
        this.mapper          = Objects.requireNonNull(mapper,         "mapper must not be null");
        this.repository      = Objects.requireNonNull(repository,     "repository must not be null");
        this.spec            = spec;
        this.selectedColumns = selectedColumns;
    }

    // -------------------------------------------------------------------------
    // Intermediate methods — return new instances (immutable builder)
    // -------------------------------------------------------------------------

    @Override
    public <F> QueryBuilder<E, D> filter(F filterDto) {
        log.debug("DefaultEntityQuery.filter: building exact-equality spec from [{}]",
                filterDto != null ? filterDto.getClass().getSimpleName() : "null");
        return new DefaultEntityQuery<>(entityManager, entityClass, mapper, repository,
                EXACT_STRATEGY.toSpecification(filterDto), this.selectedColumns);
    }

    @Override
    public <F> QueryBuilder<E, D> search(F filterDto) {
        log.debug("DefaultEntityQuery.search: building LIKE spec from [{}]",
                filterDto != null ? filterDto.getClass().getSimpleName() : "null");
        return new DefaultEntityQuery<>(entityManager, entityClass, mapper, repository,
                LIKE_STRATEGY.toSpecification(filterDto), this.selectedColumns);
    }

    @Override
    public QueryBuilder<E, D> select(String... columns) {
        return new DefaultEntityQuery<>(entityManager, entityClass, mapper, repository,
                this.spec, columns);
    }

    // -------------------------------------------------------------------------
    // Full-entity terminal operations
    // -------------------------------------------------------------------------

    @Override

    public Optional<D> findOne() {
        log.debug("DefaultEntityQuery.findOne: executing full-entity query on [{}]",
                entityClass.getSimpleName());
        return repository.findOne(effectiveSpec()).map(mapper::toDto);
    }

    @Override

    public List<D> findList() {
        log.debug("DefaultEntityQuery.findList: executing full-entity query on [{}]",
                entityClass.getSimpleName());
        return repository.findAll(effectiveSpec())
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override

    public PageDTO<D> findPage(PaginationRequest req) {
        log.debug("DefaultEntityQuery.findPage: executing paginated query on [{}]",
                entityClass.getSimpleName());
        Pageable pageable = PageableUtil.toPageable(req);
        Page<D> page = repository.findAll(effectiveSpec(), pageable).map(mapper::toDto);
        return PageDTO.fromPage(page, req);
    }

    // -------------------------------------------------------------------------
    // Projected terminal operations
    // -------------------------------------------------------------------------

    @Override

    public Optional<Map<String, Object>> findOneProjected() {
        assertColumnsSelected("findOneProjected");
        log.debug("DefaultEntityQuery.findOneProjected: selecting {} from [{}]",
                Arrays.toString(selectedColumns), entityClass.getSimpleName());
        List<Map<String, Object>> results = executeProjection();
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override

    public List<Map<String, Object>> findListProjected() {
        assertColumnsSelected("findListProjected");
        log.debug("DefaultEntityQuery.findListProjected: selecting {} from [{}]",
                Arrays.toString(selectedColumns), entityClass.getSimpleName());
        return executeProjection();
    }

    @Override

    public PageDTO<Map<String, Object>> findPageProjected(PaginationRequest req) {
        assertColumnsSelected("findPageProjected");
        log.debug("DefaultEntityQuery.findPageProjected: selecting {} from [{}]",
                Arrays.toString(selectedColumns), entityClass.getSimpleName());

        Pageable pageable = PageableUtil.toPageable(req);

        // Count query — reuses the same spec for consistency
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<E> countRoot = countQuery.from(entityClass);
        countQuery.select(cb.count(countRoot));
        countQuery.where(effectiveSpec().toPredicate(countRoot, countQuery, cb));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        // Data query with projection + pagination
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<E> root = cq.from(entityClass);
        Selection<?>[] selections = Arrays.stream(selectedColumns)
                .map(col -> root.<Object>get(col).alias(col))
                .toArray(Selection[]::new);
        cq.multiselect(selections);
        cq.where(effectiveSpec().toPredicate(root, cq, cb));

        List<Tuple> rows = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<Map<String, Object>> content = rows.stream()
                .map(this::tupleToMap)
                .toList();

        return PageDTO.fromContent(content, total, req);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private List<Map<String, Object>> executeProjection() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<E> root = cq.from(entityClass);

        Selection<?>[] selections = Arrays.stream(selectedColumns)
                .map(col -> root.<Object>get(col).alias(col))
                .toArray(Selection[]::new);
        cq.multiselect(selections);
        cq.where(effectiveSpec().toPredicate(root, cq, cb));

        List<Tuple> rows = entityManager.createQuery(cq).getResultList();
        return rows.stream()
                .map(this::tupleToMap)
                .toList();
    }

    private Map<String, Object> tupleToMap(Tuple tuple) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (String col : selectedColumns) {
            row.put(col, tuple.get(col));
        }
        return row;
    }

    @NonNull
    private Specification<E> effectiveSpec() {
        return spec != null ? spec : (root, query, cb) -> cb.conjunction();
    }

    private void assertColumnsSelected(String method) {
        if (selectedColumns == null || selectedColumns.length == 0) {
            throw new IllegalStateException(
                    "select() must be called before " + method + "()");
        }
    }
}
