package com.mycompany.myapp.service;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DetectMisinformation {

    @Value("${GITHUB_TOKEN}")
    private String githubToken;

    @Value("${GEMINI_TOKEN_VAS}")
    private String geminiToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GITHUB_API = "https://api.github.com";

    public String analyse(String repoUrl) {
        String[] parts = repoUrl.split("/");
        if (parts.length < 5) return "Invalid GitHub URL";

        String owner = parts[3];
        String repo = parts[4];
        String startPath = parts.length > 7 ? parts[7] : "";

        String readme = getREADMERecursive(owner, repo, startPath);
        List<String> solFiles = getSolFilesRecursive(owner, repo, startPath);
        String solContents = String.join("\n---\n", solFiles);

        String prompt =
            """
            You are a smart contract security auditor and misinformation detector specializing in DeFi protocols.

            Analyze the following README and Solidity code from a DeFi project.

            README:
            %s

            Solidity Code:
            %s

            Perform the following analysis and respond ONLY in valid JSON format with no extra text:

            1. Compare the README claims against the actual code implementation
            2. Identify any vulnerabilities in the Solidity code
            3. Detect any rug pull patterns or malicious admin functions
            4. Score the accuracy of the README against the code

            Respond in this exact JSON format:
            {
                "accuracyScore": <number 0-100, how accurate the README is to the actual code>,
                "accuracyVerdict": "<TRUSTWORTHY | MISLEADING | FRAUDULENT>",
                "misinformationSummary": "<overall summary of misinformation found>",
                "misinformationDetails": [
                    {
                        "claim": "<what the README claims>",
                        "reality": "<what the code actually does>",
                        "severity": "<HIGH | MEDIUM | LOW>"
                    }
                ],
                "vulnerabilities": [
                    {
                        "name": "<vulnerability name>",
                        "description": "<what it is>",
                        "severity": "<CRITICAL | HIGH | MEDIUM | LOW>",
                        "lineReference": "<relevant code snippet>"
                    }
                ],
                "rugPullRisks": [
                    {
                        "pattern": "<rug pull pattern name>",
                        "description": "<how it could be exploited>"
                    }
                ],
                "overallRiskScore": <number 0-100, 100 being most dangerous>,
                "overallRiskVerdict": "<SAFE | LOW RISK | MEDIUM RISK | HIGH RISK | CRITICAL>

                do NOT include any emojis in the response.
                "
            }
            """.formatted(readme, solContents);

        return askGemini(prompt);
    }

    public String getREADMERecursive(String owner, String repo, String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = GITHUB_API + "/repos/" + owner + "/" + repo + "/contents/" + path;
        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            List<Map> files = response.getBody();

            // First pass - look for README.md at this level
            for (Map file : files) {
                String name = (String) file.get("name");
                String type = (String) file.get("type");
                if (type.equals("file") && name.equalsIgnoreCase("README.md")) {
                    // Switch to raw accept header to get content directly
                    HttpHeaders rawHeaders = new HttpHeaders();
                    rawHeaders.set("Authorization", "Bearer " + githubToken);
                    rawHeaders.set("Accept", "application/vnd.github.v3.raw");
                    HttpEntity<String> rawEntity = new HttpEntity<>(rawHeaders);
                    ResponseEntity<String> rawResponse = restTemplate.exchange(url + "/README.md", HttpMethod.GET, rawEntity, String.class);
                    return rawResponse.getBody();
                }
            }

            // Second pass - recurse into subdirectories
            for (Map file : files) {
                String type = (String) file.get("type");
                String name = (String) file.get("name");
                if (type.equals("dir")) {
                    String subPath = path.isEmpty() ? name : path + "/" + name;
                    String result = getREADMERecursive(owner, repo, subPath);
                    if (!result.equals("No README found")) return result;
                }
            }
        } catch (Exception e) {
            return "Error fetching README: " + e.getMessage();
        }
        return "No README found";
    }

    public List<String> getSolFilesRecursive(String owner, String repo, String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = GITHUB_API + "/repos/" + owner + "/" + repo + "/contents/" + path;
        List<String> solFiles = new ArrayList<>();

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            List<Map> files = response.getBody();

            for (Map file : files) {
                String name = (String) file.get("name");
                String type = (String) file.get("type");

                if (type.equals("dir")) {
                    // Recurse into subdirectory
                    String subPath = path.isEmpty() ? name : path + "/" + name;
                    solFiles.addAll(getSolFilesRecursive(owner, repo, subPath));
                } else if (name.endsWith(".sol")) {
                    String downloadUrl = (String) file.get("download_url");
                    String content = restTemplate.getForObject(downloadUrl, String.class);
                    solFiles.add("// " + name + "\n" + content);
                }
            }
        } catch (Exception e) {
            solFiles.add("Error fetching sol files: " + e.getMessage());
        }
        return solFiles;
    }

    private String askGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiToken;

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            List<Map> candidates = (List<Map>) response.getBody().get("candidates");
            Map firstCandidate = candidates.get(0);
            Map responseContent = (Map) firstCandidate.get("content");
            List<Map> parts = (List<Map>) responseContent.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "Gemini error: " + e.getMessage();
        }
    }
}
