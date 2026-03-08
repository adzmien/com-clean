package com.clean.jpa.base.service;

import com.clean.jpa.base.repository.BaseJpaRepository;
import com.clean.jpa.entity.BaseEntity;
import com.clean.jpa.mapper.BaseMapper;
import com.clean.jpa.mapper.WriteMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Abstract base service providing full CRUD operations for JPA entities.
 *
 * <p>For read-only services, extend {@link BaseJpaReadService} instead.
 *
 * @param <E>  JPA entity type
 * @param <ID> primary key type
 * @param <D>  DTO type
 */
@Slf4j
public abstract class BaseJpaCrudService<E, ID, D> extends BaseJpaReadService<E, ID, D>
        implements CrudService<ID, D> {

    private final WriteMapper<E, D> writeMapper;

    /**
     * @param repository     JPA repository
     * @param mapper         full mapper (read + write); stored as ReadMapper in the parent
     *                       and as WriteMapper here
     * @param entityClass    entity class
     * @param allowedColumns columns permitted in projected queries
     */
    protected BaseJpaCrudService(BaseJpaRepository<E, ID> repository, BaseMapper<E, D> mapper,
                                  Class<E> entityClass, Set<String> allowedColumns) {
        super(repository, mapper, entityClass, allowedColumns);
        this.writeMapper = mapper;
    }

    @Transactional
    public D create(D dto) {
        E entity = writeMapper.toEntity(dto);
        E saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Transactional
    public D update(ID id, D dto) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
        writeMapper.updateEntity(dto, entity);
        E saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    /**
     * Soft-deletes the entity: sets {@code deleted = true} instead of removing the row.
     * Falls back to hard delete for entities that do not extend {@link BaseEntity}.
     */
    @Transactional
    public void deleteById(ID id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
        if (entity instanceof BaseEntity baseEntity) {
            baseEntity.setDeleted(true);
            repository.save(entity);
        } else {
            repository.deleteById(id);
        }
    }

    /**
     * Permanently removes the entity from the database (hard delete).
     * Use with caution — this bypasses soft-delete.
     */
    @Transactional
    public void hardDeleteById(ID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Entity not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
