package com.mycompany.myapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class AnalysisService {

    // AnalysisService: builds prompts, calls LLM, parses response and computes scores.

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String llmApiUrl;
    // API key for the LLM provider (read from environment)
    private final String llmApiKey;

    /**
     * @param webClientBuilder  WebClient builder
     * @param objectMapper  Jackson ObjectMapper
     * @param llmApiUrl configured LLM endpoint URL
     * @param llmApiKey configured LLM API key
     */
    public AnalysisService(
        WebClient.Builder webClientBuilder,
        ObjectMapper objectMapper,
        @Value("${llm.api.url}") String llmApiUrl,
        @Value("${llm.api.key}") String llmApiKey
    ) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.llmApiUrl = llmApiUrl;
        this.llmApiKey = llmApiKey;
    }

    /**
     * 1. Build a concise prompt containing old/new code.
     * 2. Call the configured LLM endpoint and obtain the raw response.
     * 3. Parse the LLM response to extract numeric scores and a short rationale.
     * 4. Compute an overall score using the configured weighting.
     *
     * The method returns a `RepoAnalysisResult` with normalized fields suitable
     * for sending to the frontend.
     */
    public RepoAnalysisResult evaluateSync(RepoAnalysisRequest req) {
        if (req == null) throw new IllegalArgumentException("Request cannot be null");

        String prompt = buildPrompt(req);
        String rawResponse = callLlm(prompt);

        // Truncate raw response to keep payloads reasonable for frontend/storage
        String truncatedRaw = rawResponse == null
            ? ""
            : (rawResponse.length() > 10000 ? rawResponse.substring(0, 10000) + "..." : rawResponse);

        ParsedLlm parsed = parseLlmResponse(rawResponse);

        int oldScore = clamp(parsed.oldScore);
        int newScore = clamp(parsed.newScore);
        String rationale = sanitizedRationale(parsed.rationale);

        int misinformation = req.misinformationScore;
        int overall = Math.round(newScore * 0.75f + misinformation * 0.25f);
        return new RepoAnalysisResult(oldScore, newScore, overall, rationale, truncatedRaw, null);
    }

    private static class ParsedLlm {

        int oldScore = 0;
        int newScore = 0;
        String rationale = "";
    }

    /**
     * Parse the raw LLM response into a small structured object.
     * Attempts two shapes:
     * - direct JSON with `oldScore`/`newScore` at the root
     * - Google Gemini style: `candidates[0].content.parts[0].text` containing JSON
     * If parsing fails the method returns defaults (0/0/empty) to keep the
     * frontend contract stable.
     */
    private ParsedLlm parseLlmResponse(String raw) {
        ParsedLlm out = new ParsedLlm();
        if (raw == null || raw.isBlank()) return out;

        try {
            // Try direct JSON parse first
            JsonNode root = objectMapper.readTree(raw);
            // If root contains oldScore/newScore directly
            if (root.has("oldScore") || root.has("newScore")) {
                out.oldScore = root.path("oldScore").asInt(0);
                out.newScore = root.path("newScore").asInt(0);
                out.rationale = root.path("rationale").asText("");
                return out;
            }

            // Google Gemini-like response: candidates[0].content.parts[0].text
            JsonNode cand = root.path("candidates");
            if (cand.isArray() && cand.size() > 0) {
                JsonNode parts = cand.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String aiText = parts.get(0).path("text").asText("");
                    String clean = stripMarkdownJsonFence(aiText);
                    // Try parse the embedded JSON
                    try {
                        JsonNode data = objectMapper.readTree(clean);
                        out.oldScore = data.path("oldScore").asInt(0);
                        out.newScore = data.path("newScore").asInt(0);
                        out.rationale = data.path("rationale").asText("");
                        return out;
                    } catch (Exception ignored) {
                        // fallthrough to other attempts
                    }
                }
            }
        } catch (Exception e) {
            // parsing attempts failed — we'll attempt fallback heuristics below
        }

        // Fallback 1: try to extract the first JSON object substring and parse it
        try {
            String candidate = extractFirstJsonObject(raw);
            if (candidate != null) {
                try {
                    JsonNode data = objectMapper.readTree(candidate);
                    out.oldScore = data.path("oldScore").asInt(out.oldScore);
                    out.newScore = data.path("newScore").asInt(out.newScore);
                    out.rationale = data.path("rationale").asText(out.rationale);
                    return out;
                } catch (Exception ignored) {
                    // continue to looser heuristics
                }
            }
        } catch (Exception ignored) {}

        // Fallback 2: loose regex-based heuristic (look for "old score: X ... new score: Y")
        try {
            Pattern p = Pattern.compile("(?i)old\s*score\s*[:=]\s*(\\d{1,3}).*?new\s*score\s*[:=]\s*(\\d{1,3})", Pattern.DOTALL);
            Matcher m = p.matcher(raw);
            if (m.find()) {
                try {
                    out.oldScore = clamp(Integer.parseInt(m.group(1)));
                    out.newScore = clamp(Integer.parseInt(m.group(2)));
                    int idx = Math.max(0, m.start() - 200);
                    int end = Math.min(raw.length(), m.end() + 200);
                    out.rationale = sanitizedRationale(raw.substring(idx, end));
                    return out;
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        // Last resort: return defaults (0/0/empty) so frontend gets a clean, predictable object
        return out;
    }

    /**
     * Remove markdown JSON fences (```json ... ``` ) if present.
     */
    private String stripMarkdownJsonFence(String s) {
        if (s == null) return "";
        return s.replaceAll("(?i)```json", "").replaceAll("```", "").strip();
    }

    private String extractFirstJsonObject(String s) {
        if (s == null) return null;
        int start = s.indexOf('{');
        if (start < 0) return null;
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return s.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    /**
     * Clamp a score into the inclusive 0..100 range.
     */
    private int clamp(int v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }

    /**
     * Sanitize the rationale string returned by the LLM so it is safe and
     * reasonably sized for frontend display.
     */
    private String sanitizedRationale(String r) {
        if (r == null) return "";
        String s = r.strip();
        if (s.length() > 500) return s.substring(0, 500) + "...";
        return s;
    }

    /**
     * Build the prompt that will be sent to the LLM. We instruct the model to
     * return a strict JSON object containing `oldScore`, `newScore`, and `rationale`.
     * The preview of code is truncated to keep prompts within reasonable size.
     */
    private String buildPrompt(RepoAnalysisRequest r) {
        return (
            "You are a security reviewer. " +
            "Compare the OLD and NEW code below and assign an integer score 0-100 to each. " +
            "Respond with JSON only, exactly containing: {\"oldScore\":<int>,\"newScore\":<int>,\"rationale\":\"...\"}. " +
            "Do not include markdown formatting.\n\n" +
            "OLD CODE:\n" +
            safePreview(r.oldCode) +
            "\n\n" +
            "NEW CODE:\n" +
            safePreview(r.newCode)
        );
    }

    /**
     * Return a truncated preview of a potentially-long code string.
     * Keeps the prompt payload size bounded.
     */
    private String safePreview(String s) {
        if (s == null) return "";
        return s.length() <= 2000 ? s : s.substring(0, 2000) + "...";
    }

    /**
     * Perform an HTTP POST to the configured LLM URL using the provided prompt.
     * The method builds a minimal provider-specific payload (Google Gemini shape
     * in this POC) and returns the raw response body as a string. Any exception
     * is converted to a small JSON error string so the caller can handle it.
     */
    private String callLlm(String prompt) {
        // Conservative retry/backoff for transient errors (429/503 etc.).
        var payload = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        String fullUrl = llmApiUrl + "?key=" + llmApiKey;
        int maxAttempts = 3;
        long baseDelayMs = 1000L; // 1s base

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String resp =
                    this.webClient.post()
                        .uri(fullUrl)
                        .header("Content-Type", "application/json")
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(Duration.ofSeconds(60));
                return resp;
            } catch (WebClientResponseException wex) {
                int status = wex.getRawStatusCode();
                if ((status == 429 || status == 503) && attempt < maxAttempts) {
                    long jitter = ThreadLocalRandom.current().nextLong(200, 1000);
                    long sleep = baseDelayMs * (1L << (attempt - 1)) + jitter;
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }
                String msg = wex.getResponseBodyAsString();
                if (msg == null || msg.isBlank()) msg = wex.getMessage();
                return "{\"error\":\"LLM call failed: " + msg.replace("\"", "'") + "\"}";
            } catch (Exception e) {
                if (attempt < maxAttempts) {
                    long jitter = ThreadLocalRandom.current().nextLong(200, 1000);
                    long sleep = baseDelayMs * (1L << (attempt - 1)) + jitter;
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }
                return "{\"error\":\"LLM call failed: " + e.getMessage().replace("\"", "'") + "\"}";
            }
        }

        return "{\"error\":\"LLM call failed: unknown\"}";
    }

    // Inner classes (Keep these as they were)
    public static class RepoAnalysisRequest {

        public int misinformationScore;
        public String oldCode;
        public String newCode;
    }

    public static class RepoAnalysisResult {

        public final int oldScore;
        public final int newScore;
        public final int overallScore;
        public final String rationale;
        public final String rawLlmResponse;
        public final String repoUrl;
        public final String jobId;
        public final Instant analyzedAt;

        public RepoAnalysisResult(int oldScore, int newScore, int overallScore, String rationale, String rawLlmResponse, String repoUrl) {
            this.oldScore = oldScore;
            this.newScore = newScore;
            this.overallScore = overallScore;
            this.rationale = rationale;
            this.rawLlmResponse = rawLlmResponse;
            this.repoUrl = repoUrl;
            this.jobId = UUID.randomUUID().toString();
            this.analyzedAt = Instant.now();
        }
    }
}
