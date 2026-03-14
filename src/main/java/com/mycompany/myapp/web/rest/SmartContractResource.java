package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.SmartContract;
import com.mycompany.myapp.repository.SmartContractRepository;
import com.mycompany.myapp.service.SmartContractService;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.SmartContract}.
 */
@RestController
@RequestMapping("/api/smart-contracts")
public class SmartContractResource {

    private static final Logger LOG = LoggerFactory.getLogger(SmartContractResource.class);

    private static final String ENTITY_NAME = "smartContract";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SmartContractService smartContractService;

    private final SmartContractRepository smartContractRepository;

    public SmartContractResource(SmartContractService smartContractService, SmartContractRepository smartContractRepository) {
        this.smartContractService = smartContractService;
        this.smartContractRepository = smartContractRepository;
    }

    /**
     * {@code POST  /smart-contracts} : Create a new smartContract.
     *
     * @param smartContract the smartContract to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new smartContract, or with status {@code 400 (Bad Request)} if the smartContract has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SmartContract> createSmartContract(@Valid @RequestBody SmartContract smartContract) throws URISyntaxException {
        LOG.debug("REST request to save SmartContract : {}", smartContract);
        if (smartContract.getId() != null) {
            throw new BadRequestAlertException("A new smartContract cannot already have an ID", ENTITY_NAME, "idexists");
        }
        smartContract = smartContractService.save(smartContract);
        return ResponseEntity.created(new URI("/api/smart-contracts/" + smartContract.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, smartContract.getId().toString()))
            .body(smartContract);
    }

    /**
     * {@code PUT  /smart-contracts/:id} : Updates an existing smartContract.
     *
     * @param id the id of the smartContract to save.
     * @param smartContract the smartContract to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated smartContract,
     * or with status {@code 400 (Bad Request)} if the smartContract is not valid,
     * or with status {@code 500 (Internal Server Error)} if the smartContract couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SmartContract> updateSmartContract(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SmartContract smartContract
    ) throws URISyntaxException {
        LOG.debug("REST request to update SmartContract : {}, {}", id, smartContract);
        if (smartContract.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, smartContract.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!smartContractRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        smartContract = smartContractService.update(smartContract);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, smartContract.getId().toString()))
            .body(smartContract);
    }

    /**
     * {@code PATCH  /smart-contracts/:id} : Partial updates given fields of an existing smartContract, field will ignore if it is null
     *
     * @param id the id of the smartContract to save.
     * @param smartContract the smartContract to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated smartContract,
     * or with status {@code 400 (Bad Request)} if the smartContract is not valid,
     * or with status {@code 404 (Not Found)} if the smartContract is not found,
     * or with status {@code 500 (Internal Server Error)} if the smartContract couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SmartContract> partialUpdateSmartContract(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SmartContract smartContract
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update SmartContract partially : {}, {}", id, smartContract);
        if (smartContract.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, smartContract.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!smartContractRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SmartContract> result = smartContractService.partialUpdate(smartContract);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, smartContract.getId().toString())
        );
    }

    /**
     * {@code GET  /smart-contracts} : get all the smartContracts.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of smartContracts in body.
     */
    @GetMapping("")
    public List<SmartContract> getAllSmartContracts() {
        LOG.debug("REST request to get all SmartContracts");
        return smartContractService.findAll();
    }

    /**
     * {@code GET  /smart-contracts/:id} : get the "id" smartContract.
     *
     * @param id the id of the smartContract to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the smartContract, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SmartContract> getSmartContract(@PathVariable("id") Long id) {
        LOG.debug("REST request to get SmartContract : {}", id);
        Optional<SmartContract> smartContract = smartContractService.findOne(id);
        return ResponseUtil.wrapOrNotFound(smartContract);
    }

    /**
     * {@code DELETE  /smart-contracts/:id} : delete the "id" smartContract.
     *
     * @param id the id of the smartContract to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSmartContract(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete SmartContract : {}", id);
        smartContractService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
