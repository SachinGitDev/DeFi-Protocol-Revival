package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.AnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisResource {

    private final Logger log = LoggerFactory.getLogger(AnalysisResource.class);
    private final AnalysisService analysisService;

    public AnalysisResource(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnalysisService.RepoAnalysisResult> evaluate(@RequestBody AnalysisService.RepoAnalysisRequest request) {
        log.debug("Received analysis request");
        AnalysisService.RepoAnalysisResult result = analysisService.evaluateSync(request);
        return ResponseEntity.ok(result);
    }
}
