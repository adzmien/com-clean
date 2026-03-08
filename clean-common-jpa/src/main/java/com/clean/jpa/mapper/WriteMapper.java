package com.clean.jpa.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Write-only mapper contract: DTO → entity conversion.
 *
 * <p>Complements {@link ReadMapper} to satisfy the Interface Segregation Principle:
 * read-only consumers depend on {@code ReadMapper}, write consumers depend on
 * {@code WriteMapper}, and full-access mappers implement both via {@link BaseMapper}.
 *
 * @param <E> JPA entity type
 * @param <D> DTO type
 */
public interface WriteMapper<E, D> {

    E toEntity(D dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(D dto, @MappingTarget E entity);
}
