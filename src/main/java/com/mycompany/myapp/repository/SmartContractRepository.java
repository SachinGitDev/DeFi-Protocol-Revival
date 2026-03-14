package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.SmartContract;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SmartContract entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SmartContractRepository extends JpaRepository<SmartContract, Long> {}
