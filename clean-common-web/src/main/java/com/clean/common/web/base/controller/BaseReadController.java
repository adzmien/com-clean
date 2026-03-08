package com.clean.common.web.base.controller;

import com.clean.jpa.base.dto.PageDTO;
import com.clean.jpa.base.dto.PageQueryDTO;
import com.clean.jpa.base.service.ReadService;
import com.clean.common.web.base.dto.ProjectedRequestDTO;
import com.clean.common.web.base.dto.RequestEnvelopeDTO;
import com.clean.common.web.base.dto.ResponseEnvelopeDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Abstract read-only REST controller providing standard query endpoints.
 *
 * <p>Subclasses annotate with {@code @RestController} and {@code @RequestMapping("api/v1/...")}
 * and supply their service via the constructor — zero method overrides needed for standard reads.
 *
 * @param <ID> primary key type (typically {@code Long})
 * @param <D>  response DTO type
 * @param <F>  filter DTO type (used in criteria queries)
 */
@Slf4j
public abstract class BaseReadController<ID, D, F> {

    protected final ReadService<ID, D> service;

    protected BaseReadController(ReadService<ID, D> service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseEnvelopeDTO<D>> findById(@PathVariable("id") ID id) {
        log.debug("findById: request received for id [{}]", id);
        D dto = service.findById(id);
        return ResponseEntity.ok(ResponseEnvelopeDTO.ok(dto));
    }

    @PostMapping("/findByCriteria")
    public ResponseEntity<ResponseEnvelopeDTO<D>> findOneBySpecification(
            @Valid @RequestBody RequestEnvelopeDTO<F> request) {
        log.debug("findOneBySpecification: request received");
        D dto = service.findOneBySpecification(request.getPayload())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No entity found matching the provided criteria"));
        return ResponseEntity.ok(ResponseEnvelopeDTO.ok(dto));
    }

    @PostMapping("/findByCriteriaProjected")
    public ResponseEntity<ResponseEnvelopeDTO<Map<String, Object>>> findOneProjected(
            @Valid @RequestBody ProjectedRequestDTO<F> request) {
        log.debug("findOneProjected: request received");
        Map<String, Object> result = service
                .findOneProjected(request.getPayload(), request.getColumns(), request.getMode())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No entity found matching the provided criteria"));
        return ResponseEntity.ok(ResponseEnvelopeDTO.ok(result));
    }

    @PostMapping("/findListByCriteria")
    public ResponseEntity<ResponseEnvelopeDTO<List<D>>> findListBySpecification(
            @Valid @RequestBody RequestEnvelopeDTO<F> request) {
        log.debug("findListBySpecification: request received");
        List<D> dtoList = service.findListBySpecification(request.getPayload());
        return ResponseEntity.ok(ResponseEnvelopeDTO.ok(dtoList));
    }

    @PostMapping("/findListByCriteriaProjected")
    public ResponseEntity<ResponseEnvelopeDTO<List<Map<String, Object>>>> findListProjected(
            @Valid @RequestBody ProjectedRequestDTO<F> request) {
        log.debug("findListProjected: request received");
        List<Map<String, Object>> result = service
                .findListProjected(request.getPayload(), request.getColumns(), request.getMode());
        return ResponseEntity.ok(ResponseEnvelopeDTO.ok(result));
    }

    @PostMapping("/findPageByCriteria")
    public ResponseEntity<ResponseEnvelopeDTO<PageDTO<D>>> findPageBySpecification(
            @Valid @RequestBody RequestEnvelopeDTO<PageQueryDTO<F>> request) {
        log.debug("findPageBySpecification: request received");
        PageQueryDTO<F> pageRequest = request.getPayload();
        PageDTO<D> page = service.findPageBySpecification(pageRequest.getFilter(), pageRequest);
        return ResponseEntity.ok(ResponseEnvelopeDTO.ok(page));
    }

    @PostMapping("/findPageByCriteriaProjected")
    public ResponseEntity<ResponseEnvelopeDTO<PageDTO<Map<String, Object>>>> findPageProjected(
            @Valid @RequestBody ProjectedRequestDTO<PageQueryDTO<F>> request) {
        log.debug("findPageProjected: request received");
        PageQueryDTO<F> pageRequest = request.getPayload();
        PageDTO<Map<String, Object>> page = service
                .findPageProjected(pageRequest.getFilter(), request.getColumns(), request.getMode(), pageRequest);
        return ResponseEntity.ok(ResponseEnvelopeDTO.ok(page));
    }
}
