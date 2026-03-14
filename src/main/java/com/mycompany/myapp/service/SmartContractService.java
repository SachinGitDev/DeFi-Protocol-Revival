package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.SmartContract;
import com.mycompany.myapp.repository.SmartContractRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.SmartContract}.
 */
@Service
@Transactional
public class SmartContractService {

    private static final Logger LOG = LoggerFactory.getLogger(SmartContractService.class);

    private final SmartContractRepository smartContractRepository;

    public SmartContractService(SmartContractRepository smartContractRepository) {
        this.smartContractRepository = smartContractRepository;
    }

    /**
     * Save a smartContract.
     *
     * @param smartContract the entity to save.
     * @return the persisted entity.
     */
    public SmartContract save(SmartContract smartContract) {
        LOG.debug("Request to save SmartContract : {}", smartContract);
        return smartContractRepository.save(smartContract);
    }

    /**
     * Update a smartContract.
     *
     * @param smartContract the entity to save.
     * @return the persisted entity.
     */
    public SmartContract update(SmartContract smartContract) {
        LOG.debug("Request to update SmartContract : {}", smartContract);
        return smartContractRepository.save(smartContract);
    }

    /**
     * Partially update a smartContract.
     *
     * @param smartContract the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SmartContract> partialUpdate(SmartContract smartContract) {
        LOG.debug("Request to partially update SmartContract : {}", smartContract);

        return smartContractRepository
            .findById(smartContract.getId())
            .map(existingSmartContract -> {
                if (smartContract.getName() != null) {
                    existingSmartContract.setName(smartContract.getName());
                }
                if (smartContract.getGithubUrl() != null) {
                    existingSmartContract.setGithubUrl(smartContract.getGithubUrl());
                }
                if (smartContract.getOriginalCode() != null) {
                    existingSmartContract.setOriginalCode(smartContract.getOriginalCode());
                }
                if (smartContract.getResurrectedCode() != null) {
                    existingSmartContract.setResurrectedCode(smartContract.getResurrectedCode());
                }
                if (smartContract.getIsValidated() != null) {
                    existingSmartContract.setIsValidated(smartContract.getIsValidated());
                }

                return existingSmartContract;
            })
            .map(smartContractRepository::save);
    }

    /**
     * Get all the smartContracts.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<SmartContract> findAll() {
        LOG.debug("Request to get all SmartContracts");
        return smartContractRepository.findAll();
    }

    /**
     * Get one smartContract by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SmartContract> findOne(Long id) {
        LOG.debug("Request to get SmartContract : {}", id);
        return smartContractRepository.findById(id);
    }

    /**
     * Delete the smartContract by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete SmartContract : {}", id);
        smartContractRepository.deleteById(id);
    }
}
