package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.SmartContractTestSamples.*;
import static com.mycompany.myapp.domain.VulnerabilityTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SmartContractTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SmartContract.class);
        SmartContract smartContract1 = getSmartContractSample1();
        SmartContract smartContract2 = new SmartContract();
        assertThat(smartContract1).isNotEqualTo(smartContract2);

        smartContract2.setId(smartContract1.getId());
        assertThat(smartContract1).isEqualTo(smartContract2);

        smartContract2 = getSmartContractSample2();
        assertThat(smartContract1).isNotEqualTo(smartContract2);
    }

    @Test
    void vulnerabilitiesTest() {
        SmartContract smartContract = getSmartContractRandomSampleGenerator();
        Vulnerability vulnerabilityBack = getVulnerabilityRandomSampleGenerator();

        smartContract.addVulnerabilities(vulnerabilityBack);
        assertThat(smartContract.getVulnerabilities()).containsOnly(vulnerabilityBack);
        assertThat(vulnerabilityBack.getContract()).isEqualTo(smartContract);

        smartContract.removeVulnerabilities(vulnerabilityBack);
        assertThat(smartContract.getVulnerabilities()).doesNotContain(vulnerabilityBack);
        assertThat(vulnerabilityBack.getContract()).isNull();

        smartContract.vulnerabilities(new HashSet<>(Set.of(vulnerabilityBack)));
        assertThat(smartContract.getVulnerabilities()).containsOnly(vulnerabilityBack);
        assertThat(vulnerabilityBack.getContract()).isEqualTo(smartContract);

        smartContract.setVulnerabilities(new HashSet<>());
        assertThat(smartContract.getVulnerabilities()).doesNotContain(vulnerabilityBack);
        assertThat(vulnerabilityBack.getContract()).isNull();
    }
}
