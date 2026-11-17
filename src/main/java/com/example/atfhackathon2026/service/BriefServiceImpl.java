package com.example.atfhackathon2026.service;

import com.example.atfhackathon2026.helper.Helper;
import com.example.atfhackathon2026.request.BriefRequest;
import com.example.atfhackathon2026.response.BriefResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BriefServiceImpl implements BriefService {
    private final RestClient exa;
    private final ObjectMapper json;
    private final String apiKey;

    public BriefServiceImpl(@Value("${exa.api-key:}") String apiKey, ObjectMapper json) {
        this.apiKey = apiKey;
        this.json = json;
        this.exa = RestClient.builder().baseUrl("https://api.exa.ai").build();
    }

    @Override
    public BriefResponse check(BriefRequest request) {
        if (Helper.isNullOrEmpty(apiKey))
            throw new IllegalStateException("Road check is not configured");
        if (request == null || Helper.isNullOrEmpty(request.route()))
            throw new IllegalArgumentException("Type the route you are about to take");
        String route = request.route().trim();
        JsonNode created = post(Map.of(
                "query", "For the public transport route \"" + route + "\" in South Africa, find published reports from the last 14 days about taxi strikes, bus cancellations, road closures, accidents, or route changes that would affect a commuter today. If nothing current is published, say so. Do not invent an incident.",
                "effort", "minimal",
                "outputSchema", schema(),
                "budget", Map.of("maxCostDollars", 0.25, "maxDurationSeconds", 40)
        ));
        String id = created.path("id").asText("");
        if (id.isBlank())
            throw new IllegalStateException("Exa did not start a road check");
        JsonNode run = created;
        for (int i = 0; i < 16 && !finished(run); i++) {
            sleep();
            run = get(id);
        }
        if (!"completed".equals(run.path("status").asText()))
            throw new IllegalStateException(fail(run));
        return read(run.path("output"));
    }

    private JsonNode post(Map<String, Object> body) {
        String raw = exa.post().uri("/agent/runs")
                .header("x-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        return json.readTree(raw == null ? "{}" : raw);
    }

    private JsonNode get(String id) {
        String raw = exa.get().uri("/agent/runs/{id}", id)
                .header("x-api-key", apiKey)
                .retrieve()
                .body(String.class);
        return json.readTree(raw == null ? "{}" : raw);
    }

    private static boolean finished(JsonNode run) {
        String status = run.path("status").asText();
        return "completed".equals(status) || "failed".equals(status) || "cancelled".equals(status);
    }

    private static String fail(JsonNode run) {
        String message = run.path("error").path("message").asText("");
        if (!message.isBlank()) return message;
        return "The road check did not finish. Try again in a moment.";
    }

    private static BriefResponse read(JsonNode output) {
        JsonNode structured = output.path("structured");
        if (structured.isObject()) {
            List<BriefResponse.Source> sources = new ArrayList<>();
            for (JsonNode source : structured.path("sources")) {
                String url = source.path("url").asText("");
                String title = source.path("title").asText(url);
                if (!url.isBlank()) sources.add(new BriefResponse.Source(title, url));
            }
            String status = structured.path("status").asText("unknown");
            String summary = structured.path("summary").asText(output.path("text").asText("No published update."));
            return new BriefResponse(status, summary, sources);
        }
        return new BriefResponse("unknown", output.path("text").asText("No published update."), List.of());
    }

    private static Map<String, Object> schema() {
        Map<String, Object> source = Map.of(
                "type", "object",
                "properties", Map.of("title", Map.of("type", "string"), "url", Map.of("type", "string")),
                "required", List.of("title", "url"));
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "status", Map.of("type", "string", "enum", List.of("clear", "disrupted", "unknown")),
                        "summary", Map.of("type", "string"),
                        "sources", Map.of("type", "array", "maxItems", 3, "items", source)),
                "required", List.of("status", "summary", "sources"));
    }

    private static void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Road check was interrupted");
        }
    }
}
