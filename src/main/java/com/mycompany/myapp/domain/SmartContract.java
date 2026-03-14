package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A SmartContract.
 */
@Entity
@Table(name = "smart_contract")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SmartContract implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "github_url")
    private String githubUrl;

    @Lob
    @Column(name = "original_code", nullable = false)
    private String originalCode;

    @Lob
    @Column(name = "resurrected_code")
    private String resurrectedCode;

    @Column(name = "is_validated")
    private Boolean isValidated;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contract")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "contract" }, allowSetters = true)
    private Set<Vulnerability> vulnerabilities = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SmartContract id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public SmartContract name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGithubUrl() {
        return this.githubUrl;
    }

    public SmartContract githubUrl(String githubUrl) {
        this.setGithubUrl(githubUrl);
        return this;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getOriginalCode() {
        return this.originalCode;
    }

    public SmartContract originalCode(String originalCode) {
        this.setOriginalCode(originalCode);
        return this;
    }

    public void setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
    }

    public String getResurrectedCode() {
        return this.resurrectedCode;
    }

    public SmartContract resurrectedCode(String resurrectedCode) {
        this.setResurrectedCode(resurrectedCode);
        return this;
    }

    public void setResurrectedCode(String resurrectedCode) {
        this.resurrectedCode = resurrectedCode;
    }

    public Boolean getIsValidated() {
        return this.isValidated;
    }

    public SmartContract isValidated(Boolean isValidated) {
        this.setIsValidated(isValidated);
        return this;
    }

    public void setIsValidated(Boolean isValidated) {
        this.isValidated = isValidated;
    }

    public Set<Vulnerability> getVulnerabilities() {
        return this.vulnerabilities;
    }

    public void setVulnerabilities(Set<Vulnerability> vulnerabilities) {
        if (this.vulnerabilities != null) {
            this.vulnerabilities.forEach(i -> i.setContract(null));
        }
        if (vulnerabilities != null) {
            vulnerabilities.forEach(i -> i.setContract(this));
        }
        this.vulnerabilities = vulnerabilities;
    }

    public SmartContract vulnerabilities(Set<Vulnerability> vulnerabilities) {
        this.setVulnerabilities(vulnerabilities);
        return this;
    }

    public SmartContract addVulnerabilities(Vulnerability vulnerability) {
        this.vulnerabilities.add(vulnerability);
        vulnerability.setContract(this);
        return this;
    }

    public SmartContract removeVulnerabilities(Vulnerability vulnerability) {
        this.vulnerabilities.remove(vulnerability);
        vulnerability.setContract(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SmartContract)) {
            return false;
        }
        return getId() != null && getId().equals(((SmartContract) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SmartContract{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", githubUrl='" + getGithubUrl() + "'" +
            ", originalCode='" + getOriginalCode() + "'" +
            ", resurrectedCode='" + getResurrectedCode() + "'" +
            ", isValidated='" + getIsValidated() + "'" +
            "}";
    }
}
