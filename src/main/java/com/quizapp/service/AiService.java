package com.quizapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizapp.dto.Dtos.QuestionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Talks to the Google Gemini API using Java's built-in HttpClient.
 * To switch to another AI provider (OpenAI, Claude), only the ask() method changes.
 */
@Service
public class AiService {
    @Value("${app.ai.key}")
    private String apiKey;

    @Value("${app.ai.model}")
    private String model;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** Sends a prompt to Google Gemini and returns its text reply. */
    public String ask(String prompt) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI is not configured. Set the GEMINI_API_KEY environment variable.");
        }
        String body = mapper.writeValueAsString(Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent"))
                .header("x-goog-api-key", apiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            String err = response.body();
            if (err.length() > 300) err = err.substring(0, 300);
            throw new RuntimeException("AI request failed (status " + response.statusCode() + "): " + err);
        }
        String text = mapper.readTree(response.body())
                .path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        if (text.isBlank()) {
            throw new RuntimeException("AI returned an empty reply, please try again");
        }
        return text;
    }

    /** Asks the AI for MCQs and converts its JSON reply into Java objects. */
    public List<QuestionRequest> generateQuestions(String topic, int count, String difficulty) throws Exception {
        String prompt = "Create " + count + " " + difficulty + " level multiple choice questions about: " + topic + ". "
                + "Each question must have exactly 4 options and one correct answer. "
                + "Reply with ONLY a JSON array and nothing else. Each item must look like: "
                + "{\"text\":\"...\",\"optionA\":\"...\",\"optionB\":\"...\",\"optionC\":\"...\",\"optionD\":\"...\","
                + "\"correctOption\":\"A\",\"explanation\":\"one short sentence\"}";

        String reply = ask(prompt);
        int start = reply.indexOf('[');
        int end = reply.lastIndexOf(']');
        if (start < 0 || end < 0) {
            throw new RuntimeException("AI did not return valid questions, please try again");
        }
        return mapper.readValue(reply.substring(start, end + 1), new TypeReference<List<QuestionRequest>>() {});
    }
}