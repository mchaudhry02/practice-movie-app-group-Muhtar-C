package com.launchcode.moviemanager.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchcode.moviemanager.models.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;


@Service
public class AiMovieService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    @Value("${openai.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String generateSummary(Movie movie) {
        String prompt = String.format(
                "Write a short, engaging, spoiler-free blurb (2-3 sentences) about the movie " +
                        "\"%s\" (%d), directed by %s, in the %s genre. " +
                        "If you are not confident this is a real movie, say so honestly instead of inventing plot details.",
                movie.getTitle(), movie.getReleaseYear(),
                nullToUnknown(movie.getDirector()), nullToUnknown(movie.getGenre())
        );
        return callOpenAi(prompt, "AI summary is unavailable right now.");
    }

    public String generateRecommendations(Movie movie, List<Movie> collection) {
        StringBuilder collectionTitles = new StringBuilder();
        for (Movie m : collection) {
            if (!m.getId().equals(movie.getId())) {
                collectionTitles.append("- ").append(m.getTitle())
                        .append(" (").append(m.getGenre()).append(")\n");
            }
        }

        String prompt = String.format(
                "A user just watched \"%s\" (%s, directed by %s). " +
                        "Recommend exactly 3 other movies they would likely enjoy next, " +
                        "and give a one-sentence reason for each. Format as a simple numbered list. " +
                        "Try not to repeat movies already in their collection below unless especially relevant:\n%s",
                movie.getTitle(), nullToUnknown(movie.getGenre()), nullToUnknown(movie.getDirector()),
                collectionTitles.length() > 0 ? collectionTitles.toString() : "(collection is empty)"
        );
        return callOpenAi(prompt, "AI recommendations are unavailable right now.");
    }

    private String callOpenAi(String userPrompt, String fallbackMessage) {
        if (!isConfigured()) {
            return fallbackMessage + " (No OpenAI API key configured — see README for setup.)";
        }

        try {
            String requestBody = objectMapper.writeValueAsString(new ChatRequest(MODEL, userPrompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return fallbackMessage + " (API returned status " + response.statusCode() + ")";
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            String text = content.asText("").trim();
            return text.isEmpty() ? fallbackMessage : text;

        } catch (Exception e) {
            return fallbackMessage + " (" + e.getClass().getSimpleName() + ")";
        }
    }

    private String nullToUnknown(String value) {
        return (value == null || value.isBlank()) ? "an unknown director/genre" : value;
    }

    private static class ChatRequest {
        public String model;
        public List<Message> messages;
        public double temperature = 0.7;

        ChatRequest(String model, String userContent) {
            this.model = model;
            this.messages = List.of(new Message("user", userContent));
        }
    }

    private static class Message {
        public String role;
        public String content;

        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}