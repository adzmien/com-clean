package com.clean.jpa.base.service;

/**
 * Full CRUD service contract extending {@link ReadService} with write operations.
 *
 * <p>Read-only controllers depend on {@link ReadService}; CRUD controllers depend on this
 * interface. This mirrors the {@link com.clean.jpa.mapper.ReadMapper} /
 * {@link com.clean.jpa.mapper.WriteMapper} segregation at the service layer.
 *
 * @param <ID> primary key type
 * @param <D>  DTO type
 */
public interface CrudService<ID, D> extends ReadService<ID, D> {

    D create(D dto);

    D update(ID id, D dto);

    void deleteById(ID id);
}
