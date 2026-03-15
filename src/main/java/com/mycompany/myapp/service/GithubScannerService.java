package com.mycompany.myapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.myapp.domain.SmartContract;
import com.mycompany.myapp.repository.SmartContractRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class GithubScannerService {

    @Value("${hackathon.github.token}")
    private String githubToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AiRevivalService aiRevivalService;
    private final SmartContractRepository smartContractRepository;

    public GithubScannerService(AiRevivalService aiRevivalService, SmartContractRepository smartContractRepository) {
        this.aiRevivalService = aiRevivalService;
        this.smartContractRepository = smartContractRepository;
    }

    public List<SmartContract> fetchAndScanRepository(String githubUrl) {
        List<SmartContract> processedContracts = new ArrayList<>();

        try {
            // 1. Clean the URL and extract owner/repo
            String cleanUrl = githubUrl.replace("https://github.com/", "").replace(".git", "").trim();

            // Handle trailing slash
            if (cleanUrl.endsWith("/")) {
                cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 1);
            }

            // Only take first two parts (owner/repo) — ignore /tree/main etc.
            String[] parts = cleanUrl.split("/");
            String owner = parts[0];
            String repo = parts[1];
            // 2. Set up GitHub Authentication Headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + githubToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 3. Get the Default Branch (main or master)
            String repoApiUrl = "https://api.github.com/repos/" + owner + "/" + repo;
            ResponseEntity<JsonNode> repoResponse = restTemplate.exchange(repoApiUrl, HttpMethod.GET, entity, JsonNode.class);
            String defaultBranch = repoResponse.getBody().get("default_branch").asText();

            // 4. Get the entire file tree recursively
            String treeApiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/git/trees/" + defaultBranch + "?recursive=1";
            ResponseEntity<JsonNode> treeResponse = restTemplate.exchange(treeApiUrl, HttpMethod.GET, entity, JsonNode.class);

            // 5. Hunt for Solidity files
            for (JsonNode node : treeResponse.getBody().get("tree")) {
                String path = node.get("path").asText();
                String type = node.get("type").asText();

                if ("blob".equals(type) && path.endsWith(".sol")) {
                    // 6. Fetch the raw code of the .sol file
                    String rawUrl = "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + defaultBranch + "/" + path;

                    // Note: Raw content doesn't need the JSON accept header
                    HttpHeaders rawHeaders = new HttpHeaders();
                    rawHeaders.set("Authorization", "Bearer " + githubToken);
                    HttpEntity<String> rawEntity = new HttpEntity<>(rawHeaders);

                    ResponseEntity<String> rawCodeResponse = restTemplate.exchange(rawUrl, HttpMethod.GET, rawEntity, String.class);
                    String rawCode = rawCodeResponse.getBody();

                    // 7. Save original to DB
                    SmartContract contract = new SmartContract();
                    contract.setName(path);
                    contract.setGithubUrl("https://github.com/" + owner + "/" + repo + "/blob/" + defaultBranch + "/" + path);
                    contract.setOriginalCode(rawCode);
                    contract = smartContractRepository.save(contract);

                    // 8. Trigger Gemini to resurrect the code and find bugs!
                    SmartContract resurrected = aiRevivalService.resurrectContract(contract.getId());
                    processedContracts.add(resurrected);

                    // HACKATHON SURVIVAL TIP: Break after the first file.
                    // If a repo has 50 files, waiting for Gemini to process all 50 sequentially will make your frontend timeout!
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("GitHub API Error: " + e.getMessage());
        }

        return processedContracts;
    }
}
