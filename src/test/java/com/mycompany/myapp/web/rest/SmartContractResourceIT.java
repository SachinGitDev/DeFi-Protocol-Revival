package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.SmartContractAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.SmartContract;
import com.mycompany.myapp.repository.SmartContractRepository;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SmartContractResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SmartContractResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_GITHUB_URL = "AAAAAAAAAA";
    private static final String UPDATED_GITHUB_URL = "BBBBBBBBBB";

    private static final String DEFAULT_ORIGINAL_CODE = "AAAAAAAAAA";
    private static final String UPDATED_ORIGINAL_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_RESURRECTED_CODE = "AAAAAAAAAA";
    private static final String UPDATED_RESURRECTED_CODE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_VALIDATED = false;
    private static final Boolean UPDATED_IS_VALIDATED = true;

    private static final String ENTITY_API_URL = "/api/smart-contracts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SmartContractRepository smartContractRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSmartContractMockMvc;

    private SmartContract smartContract;

    private SmartContract insertedSmartContract;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SmartContract createEntity() {
        return new SmartContract()
            .name(DEFAULT_NAME)
            .githubUrl(DEFAULT_GITHUB_URL)
            .originalCode(DEFAULT_ORIGINAL_CODE)
            .resurrectedCode(DEFAULT_RESURRECTED_CODE)
            .isValidated(DEFAULT_IS_VALIDATED);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SmartContract createUpdatedEntity() {
        return new SmartContract()
            .name(UPDATED_NAME)
            .githubUrl(UPDATED_GITHUB_URL)
            .originalCode(UPDATED_ORIGINAL_CODE)
            .resurrectedCode(UPDATED_RESURRECTED_CODE)
            .isValidated(UPDATED_IS_VALIDATED);
    }

    @BeforeEach
    void initTest() {
        smartContract = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSmartContract != null) {
            smartContractRepository.delete(insertedSmartContract);
            insertedSmartContract = null;
        }
    }

    @Test
    @Transactional
    void createSmartContract() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the SmartContract
        var returnedSmartContract = om.readValue(
            restSmartContractMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(smartContract)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SmartContract.class
        );

        // Validate the SmartContract in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSmartContractUpdatableFieldsEquals(returnedSmartContract, getPersistedSmartContract(returnedSmartContract));

        insertedSmartContract = returnedSmartContract;
    }

    @Test
    @Transactional
    void createSmartContractWithExistingId() throws Exception {
        // Create the SmartContract with an existing ID
        smartContract.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSmartContractMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(smartContract)))
            .andExpect(status().isBadRequest());

        // Validate the SmartContract in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        smartContract.setName(null);

        // Create the SmartContract, which fails.

        restSmartContractMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(smartContract)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSmartContracts() throws Exception {
        // Initialize the database
        insertedSmartContract = smartContractRepository.saveAndFlush(smartContract);

        // Get all the smartContractList
        restSmartContractMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(smartContract.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].githubUrl").value(hasItem(DEFAULT_GITHUB_URL)))
            .andExpect(jsonPath("$.[*].originalCode").value(hasItem(DEFAULT_ORIGINAL_CODE)))
            .andExpect(jsonPath("$.[*].resurrectedCode").value(hasItem(DEFAULT_RESURRECTED_CODE)))
            .andExpect(jsonPath("$.[*].isValidated").value(hasItem(DEFAULT_IS_VALIDATED)));
    }

    @Test
    @Transactional
    void getSmartContract() throws Exception {
        // Initialize the database
        insertedSmartContract = smartContractRepository.saveAndFlush(smartContract);

        // Get the smartContract
        restSmartContractMockMvc
            .perform(get(ENTITY_API_URL_ID, smartContract.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(smartContract.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.githubUrl").value(DEFAULT_GITHUB_URL))
            .andExpect(jsonPath("$.originalCode").value(DEFAULT_ORIGINAL_CODE))
            .andExpect(jsonPath("$.resurrectedCode").value(DEFAULT_RESURRECTED_CODE))
            .andExpect(jsonPath("$.isValidated").value(DEFAULT_IS_VALIDATED));
    }

    @Test
    @Transactional
    void getNonExistingSmartContract() throws Exception {
        // Get the smartContract
        restSmartContractMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSmartContract() throws Exception {
        // Initialize the database
        insertedSmartContract = smartContractRepository.saveAndFlush(smartContract);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the smartContract
        SmartContract updatedSmartContract = smartContractRepository.findById(smartContract.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSmartContract are not directly saved in db
        em.detach(updatedSmartContract);
        updatedSmartContract
            .name(UPDATED_NAME)
            .githubUrl(UPDATED_GITHUB_URL)
            .originalCode(UPDATED_ORIGINAL_CODE)
            .resurrectedCode(UPDATED_RESURRECTED_CODE)
            .isValidated(UPDATED_IS_VALIDATED);

        restSmartContractMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSmartContract.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedSmartContract))
            )
            .andExpect(status().isOk());

        // Validate the SmartContract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSmartContractToMatchAllProperties(updatedSmartContract);
    }

    @Test
    @Transactional
    void putNonExistingSmartContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        smartContract.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSmartContractMockMvc
            .perform(
                put(ENTITY_API_URL_ID, smartContract.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(smartContract))
            )
            .andExpect(status().isBadRequest());

        // Validate the SmartContract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSmartContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        smartContract.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSmartContractMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(smartContract))
            )
            .andExpect(status().isBadRequest());

        // Validate the SmartContract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSmartContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        smartContract.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSmartContractMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(smartContract)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SmartContract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSmartContractWithPatch() throws Exception {
        // Initialize the database
        insertedSmartContract = smartContractRepository.saveAndFlush(smartContract);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the smartContract using partial update
        SmartContract partialUpdatedSmartContract = new SmartContract();
        partialUpdatedSmartContract.setId(smartContract.getId());

        partialUpdatedSmartContract.resurrectedCode(UPDATED_RESURRECTED_CODE);

        restSmartContractMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSmartContract.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSmartContract))
            )
            .andExpect(status().isOk());

        // Validate the SmartContract in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSmartContractUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSmartContract, smartContract),
            getPersistedSmartContract(smartContract)
        );
    }

    @Test
    @Transactional
    void fullUpdateSmartContractWithPatch() throws Exception {
        // Initialize the database
        insertedSmartContract = smartContractRepository.saveAndFlush(smartContract);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the smartContract using partial update
        SmartContract partialUpdatedSmartContract = new SmartContract();
        partialUpdatedSmartContract.setId(smartContract.getId());

        partialUpdatedSmartContract
            .name(UPDATED_NAME)
            .githubUrl(UPDATED_GITHUB_URL)
            .originalCode(UPDATED_ORIGINAL_CODE)
            .resurrectedCode(UPDATED_RESURRECTED_CODE)
            .isValidated(UPDATED_IS_VALIDATED);

        restSmartContractMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSmartContract.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSmartContract))
            )
            .andExpect(status().isOk());

        // Validate the SmartContract in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSmartContractUpdatableFieldsEquals(partialUpdatedSmartContract, getPersistedSmartContract(partialUpdatedSmartContract));
    }

    @Test
    @Transactional
    void patchNonExistingSmartContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        smartContract.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSmartContractMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, smartContract.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(smartContract))
            )
            .andExpect(status().isBadRequest());

        // Validate the SmartContract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSmartContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        smartContract.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSmartContractMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(smartContract))
            )
            .andExpect(status().isBadRequest());

        // Validate the SmartContract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSmartContract() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        smartContract.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSmartContractMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(smartContract)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SmartContract in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSmartContract() throws Exception {
        // Initialize the database
        insertedSmartContract = smartContractRepository.saveAndFlush(smartContract);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the smartContract
        restSmartContractMockMvc
            .perform(delete(ENTITY_API_URL_ID, smartContract.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return smartContractRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected SmartContract getPersistedSmartContract(SmartContract smartContract) {
        return smartContractRepository.findById(smartContract.getId()).orElseThrow();
    }

    protected void assertPersistedSmartContractToMatchAllProperties(SmartContract expectedSmartContract) {
        assertSmartContractAllPropertiesEquals(expectedSmartContract, getPersistedSmartContract(expectedSmartContract));
    }

    protected void assertPersistedSmartContractToMatchUpdatableProperties(SmartContract expectedSmartContract) {
        assertSmartContractAllUpdatablePropertiesEquals(expectedSmartContract, getPersistedSmartContract(expectedSmartContract));
    }
}
