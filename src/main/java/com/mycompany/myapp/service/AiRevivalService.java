package com.mycompany.myapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.domain.SmartContract;
import com.mycompany.myapp.domain.Vulnerability;
import com.mycompany.myapp.repository.SmartContractRepository;
import com.mycompany.myapp.repository.VulnerabilityRepository;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AiRevivalService {

    private final ChatClient chatClient;
    private final SmartContractRepository smartContractRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiRevivalService(
        ChatClient.Builder chatClientBuilder,
        SmartContractRepository smartContractRepository,
        VulnerabilityRepository vulnerabilityRepository
    ) {
        this.chatClient = chatClientBuilder.build();
        this.smartContractRepository = smartContractRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
    }

    public record RevivalResult(String resurrectedCode, List<VulnDetail> vulnerabilities) {}

    public record VulnDetail(String name, String description, String severity) {}

    private String sanitizeJson(String raw) {
        if (raw == null) return null;

        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("(?s)^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
        }

        StringBuilder sb = new StringBuilder(cleaned.length());
        boolean inString = false;
        boolean escape = false;

        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);

            if (escape) {
                sb.append(c);
                escape = false;
                continue;
            }

            if (c == '\\') {
                sb.append(c);
                escape = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                sb.append(c);
                continue;
            }

            if (inString) {
                if (c == '\n') {
                    sb.append("\\n");
                } else if (c == '\r') {
                    sb.append("\\r");
                } else if (c == '\t') {
                    sb.append("\\t");
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public SmartContract resurrectContract(Long contractId) {
        SmartContract contract = smartContractRepository
            .findById(contractId)
            .orElseThrow(() -> new RuntimeException("Contract not found!"));

        String prompt =
            "You are an expert Web3 Security Auditor. " +
            "Review the following Solidity smart contract. " +
            "Identify any vulnerabilities (like reentrancy, overflow, etc) and rewrite the entire contract " +
            "using modern, secure Solidity 0.8.20 standards. " +
            "IMPORTANT: Return ONLY a valid JSON object with exactly two fields: " +
            "\"resurrectedCode\" (string containing the full rewritten Solidity contract) and " +
            "\"vulnerabilities\" (array of objects each with fields: \"name\", \"description\", \"severity\"). " +
            "All newlines inside string values MUST be escaped as \\n. " +
            "Do NOT include literal newline characters inside JSON string values. " +
            "Do NOT wrap your response in markdown code fences. Output raw JSON only.\n\n" +
            "Code:\n" +
            contract.getOriginalCode();

        String rawJson = chatClient.prompt().user(prompt).call().content();
        String sanitizedJson = sanitizeJson(rawJson);

        try {
            JsonNode root = objectMapper.readTree(sanitizedJson);

            String resurrectedCode = root.path("resurrectedCode").asText("");
            contract.setResurrectedCode(resurrectedCode);
            contract = smartContractRepository.save(contract);

            JsonNode vulns = root.path("vulnerabilities");
            if (vulns.isArray()) {
                for (JsonNode v : vulns) {
                    Vulnerability vuln = new Vulnerability();
                    vuln.setName(v.path("name").asText("Unknown"));
                    vuln.setDescription(v.path("description").asText(""));
                    vuln.setSeverity(v.path("severity").asText("Medium"));
                    vuln.setContract(contract);
                    vulnerabilityRepository.save(vuln);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
        }

        return contract;
    }
}
