package com.clean.jpa.mapper;

/**
 * Read-only mapper contract: entity → DTO conversion only.
 *
 * <p>Read-only consumers such as {@code BaseJpaReadService} and {@code DefaultEntityQuery}
 * depend on this narrower interface instead of the full {@link BaseMapper}, satisfying
 * the Interface Segregation Principle (ISP-01).
 *
 * @param <E> JPA entity type
 * @param <D> DTO type
 */
public interface ReadMapper<E, D> {

    D toDto(E entity);
}
