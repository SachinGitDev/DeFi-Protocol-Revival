package com.mycompany.myapp.web.rest;

//imports here
import com.mycompany.myapp.domain.SmartContract;
import com.mycompany.myapp.service.AiRevivalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AiRevivalResource {

    private final AiRevivalService aiRevivalService;

    // Spring automatically wires up the service you built earlier
    public AiRevivalResource(AiRevivalService aiRevivalService) {
        this.aiRevivalService = aiRevivalService;
    }

    // This creates an endpoint at POST /api/smart-contracts/{id}/resurrect
    @PostMapping("/smart-contracts/{id}/resurrect")
    public ResponseEntity<SmartContract> triggerResurrection(@PathVariable Long id) {
        // Calls your Gemini AI logic
        SmartContract updatedContract = aiRevivalService.resurrectContract(id);

        // Sends the fixed contract back to the React frontend
        return ResponseEntity.ok(updatedContract);
    }
}
