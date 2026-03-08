package com.clean.common.web.base.controller;

import com.clean.jpa.base.service.CrudService;
import com.clean.common.web.base.dto.RequestEnvelopeDTO;
import com.clean.common.web.base.dto.ResponseEnvelopeDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Abstract CRUD REST controller extending {@link BaseReadController} with write operations.
 *
 * <p>Read-only domains extend {@code BaseReadController}; CRUD domains extend this class.
 *
 * @param <ID> primary key type (typically {@code Long})
 * @param <D>  response DTO type
 * @param <F>  filter DTO type (used in criteria queries)
 */
@Slf4j
public abstract class BaseCrudController<ID, D, F> extends BaseReadController<ID, D, F> {

    private final CrudService<ID, D> crudService;

    protected BaseCrudController(CrudService<ID, D> service) {
        super(service);
        this.crudService = service;
    }

    @PostMapping
    public ResponseEntity<ResponseEnvelopeDTO<D>> create(
            @Valid @RequestBody RequestEnvelopeDTO<D> request) {
        log.debug("create: request received");
        D created = crudService.create(request.getPayload());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseEnvelopeDTO.ok(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseEnvelopeDTO<D>> update(
            @PathVariable("id") ID id,
            @Valid @RequestBody RequestEnvelopeDTO<D> request) {
        log.debug("update: request received for id [{}]", id);
        D updated = crudService.update(id, request.getPayload());
        return ResponseEntity.ok(ResponseEnvelopeDTO.ok(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseEnvelopeDTO<Void>> deleteById(@PathVariable("id") ID id) {
        log.debug("deleteById: request received for id [{}]", id);
        crudService.deleteById(id);
        return ResponseEntity.ok(ResponseEnvelopeDTO.ok(null));
    }
}
