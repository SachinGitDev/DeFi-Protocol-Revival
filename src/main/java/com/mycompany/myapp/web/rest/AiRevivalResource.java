package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.SmartContract;
import com.mycompany.myapp.service.AiRevivalService;
import com.mycompany.myapp.service.GithubScannerService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AiRevivalResource {

    private final AiRevivalService aiRevivalService;
    private final GithubScannerService githubScannerService;

    public AiRevivalResource(AiRevivalService aiRevivalService, GithubScannerService githubScannerService) {
        this.aiRevivalService = aiRevivalService;
        this.githubScannerService = githubScannerService;
    }

    // Existing manual endpoint
    @PostMapping("/smart-contracts/{id}/resurrect")
    public ResponseEntity<SmartContract> triggerResurrection(@PathVariable Long id) {
        return ResponseEntity.ok(aiRevivalService.resurrectContract(id));
    }

    // NEW URL-based endpoint!
    public record RepoRequest(String githubUrl) {}

    @PostMapping("/scan-repository")
    public ResponseEntity<List<SmartContract>> scanGithubRepo(@RequestBody RepoRequest request) {
        List<SmartContract> results = githubScannerService.fetchAndScanRepository(request.githubUrl());
        return ResponseEntity.ok(results);
    }
}
