package com.mycompany.myapp.service;

// add imports here
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

    // 1. Define the exact JSON structure we want Gemini to return
    public record RevivalResult(String resurrectedCode, List<VulnDetail> vulnerabilities) {}

    public record VulnDetail(String name, String description, String severity) {}

    public SmartContract resurrectContract(Long contractId) {
        SmartContract contract = smartContractRepository
            .findById(contractId)
            .orElseThrow(() -> new RuntimeException("Contract not found!"));

        // 2. Setup the JSON Converter
        BeanOutputConverter<RevivalResult> converter = new BeanOutputConverter<>(RevivalResult.class);

        // 3. The Upgraded Prompt (Injecting the JSON format instructions)
        String prompt =
            "You are an expert Web3 Security Auditor. " +
            "Review the following Solidity smart contract. " +
            "Identify any vulnerabilities (like reentrancy, overflow, etc) and rewrite the entire contract " +
            "using modern, secure Solidity 0.8.20 standards. " +
            "You MUST output the result exactly in the requested JSON format.\n\n" +
            "Code:\n" +
            contract.getOriginalCode() +
            "\n\n" +
            converter.getFormat();

        // 4. Send to Gemini
        String rawJson = chatClient.prompt().user(prompt).call().content();

        // 5. Convert JSON string back to Java Records
        RevivalResult result = converter.convert(rawJson);

        // 6. Save the new code
        contract.setResurrectedCode(result.resurrectedCode());
        contract = smartContractRepository.save(contract);

        // 7. Loop through the found vulnerabilities and save them to the database!
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
