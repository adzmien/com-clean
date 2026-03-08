package com.clean.jpa.mapper;

/**
 * Full mapper interface combining read and write operations.
 *
 * <p>Concrete domain mappers extend this interface with typed generics and carry
 * {@code @Mapper(componentModel = "spring")} — MapStruct generates implementations
 * for all three methods automatically.
 *
 * @param <E> JPA entity type
 * @param <D> DTO type
 */
public interface BaseMapper<E, D> extends ReadMapper<E, D>, WriteMapper<E, D> {
}
