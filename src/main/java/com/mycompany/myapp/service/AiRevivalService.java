package com.mycompany.myapp.service;

// add imports here
import com.mycompany.myapp.domain.SmartContract;
import com.mycompany.myapp.repository.SmartContractRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AiRevivalService {

    private final ChatClient chatClient;
    private final SmartContractRepository smartContractRepository;

    // Spring automatically injects the Gemini ChatClient and your database repository
    public AiRevivalService(ChatClient.Builder chatClientBuilder, SmartContractRepository smartContractRepository) {
        this.chatClient = chatClientBuilder.build();
        this.smartContractRepository = smartContractRepository;
    }

    public SmartContract resurrectContract(Long contractId) {
        // 1. Fetch the vulnerable contract from your database
        SmartContract contract = smartContractRepository
            .findById(contractId)
            .orElseThrow(() -> new RuntimeException("Contract not found!"));

        // 2. The Prompt Engineering (The secret sauce)
        String prompt =
            "You are an expert Web3 Security Auditor. " +
            "Review the following Solidity smart contract. " +
            "Identify any vulnerabilities (like reentrancy) and rewrite the entire contract " +
            "using modern, secure Solidity 0.8.20 standards. " +
            "Only output the fixed Solidity code, no markdown or explanations.\n\n" +
            "Code:\n" +
            contract.getOriginalCode();

        // 3. Send to Gemini and get the response
        String patchedCode = chatClient.prompt().user(prompt).call().content();

        // 4. Save the new code back to the database
        contract.setResurrectedCode(patchedCode);
        return smartContractRepository.save(contract);
    }
}
