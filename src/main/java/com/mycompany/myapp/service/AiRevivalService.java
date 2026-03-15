package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.SmartContract;
import com.mycompany.myapp.domain.Vulnerability;
import com.mycompany.myapp.repository.SmartContractRepository;
import com.mycompany.myapp.repository.VulnerabilityRepository;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AiRevivalService {

    private final ChatClient chatClient;
    private final SmartContractRepository smartContractRepository;
    private final VulnerabilityRepository vulnerabilityRepository;

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

    /**
     * Sanitizes a raw JSON string from Gemini by escaping any literal newlines
     * that appear inside JSON string values. Gemini sometimes returns real \n
     * characters inside strings instead of the escaped \\n, which breaks Jackson.
     */
    private String sanitizeJson(String raw) {
        if (raw == null) return null;

        // Strip markdown code fences if Gemini wrapped the JSON in ```json ... ```
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("(?s)^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
        }

        // Walk through the string character by character.
        // Inside a JSON string value, replace literal newline/tab/carriage-return
        // with their escaped equivalents so Jackson can parse correctly.
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
                // Replace literal control characters with their JSON escape sequences
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

        BeanOutputConverter<RevivalResult> converter = new BeanOutputConverter<>(RevivalResult.class);

        String prompt =
            "You are an expert Web3 Security Auditor. " +
            "Review the following Solidity smart contract. " +
            "Identify any vulnerabilities (like reentrancy, overflow, etc) and rewrite the entire contract " +
            "using modern, secure Solidity 0.8.20 standards. " +
            "IMPORTANT: You MUST return valid JSON. All newlines inside string values MUST be escaped as \\n. " +
            "Do NOT include literal newline characters inside JSON string values. " +
            "Do NOT wrap your response in markdown code fences. Output raw JSON only.\n\n" +
            "Code:\n" +
            contract.getOriginalCode() +
            "\n\n" +
            converter.getFormat();

        String rawJson = chatClient.prompt().user(prompt).call().content();

        // Sanitize before parsing to handle any literal newlines Gemini snuck in
        String sanitizedJson = sanitizeJson(rawJson);

        RevivalResult result = converter.convert(sanitizedJson);

        contract.setResurrectedCode(result.resurrectedCode());
        contract = smartContractRepository.save(contract);

        if (result.vulnerabilities() != null) {
            for (VulnDetail v : result.vulnerabilities()) {
                Vulnerability vuln = new Vulnerability();
                vuln.setName(v.name());
                vuln.setDescription(v.description());
                vuln.setSeverity(v.severity());
                vuln.setContract(contract);
                vulnerabilityRepository.save(vuln);
            }
        }

        return contract;
    }
}
