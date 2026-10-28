package com.quizapp.dto;

import java.util.List;
import java.util.Map;

/** All request/response shapes in one place (Java records = simple immutable data classes). */
public class Dtos {
    public record RegisterRequest(String name, String email, String password) {}
    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String token, String name, String role) {}

    // Question sent to the browser: NOTE it has no correct answer in it
    public record QuestionView(Long id, String text, String optionA, String optionB, String optionC, String optionD) {}

    public record SubmitRequest(Map<Long, String> answers) {}
    public record ReviewItem(String question, String yourAnswer, String correctAnswer, boolean correct, String explanation) {}
    public record ResultResponse(int score, int total, List<ReviewItem> review) {}

    public record HistoryItem(String quizTitle, int score, int total, String takenAt) {}
    public record LeaderboardItem(String name, long totalScore) {}

    // Admin
    public record QuizRequest(String title, String topic, String description) {}
    public record QuestionRequest(String text, String optionA, String optionB, String optionC, String optionD,
                                  String correctOption, String explanation) {}
    public record GenerateRequest(String topic, int count, String difficulty) {}

    // AI
    public record ExplainRequest(String question, String yourAnswer, String correctAnswer) {}
}
