package com.quizapp.controller;

import com.quizapp.dto.Dtos.*;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.repository.QuizRepository;
import com.quizapp.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Admin-only endpoints (protected by hasRole("ADMIN") in SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final QuizRepository quizRepo;
    private final AiService aiService;

    public AdminController(QuizRepository quizRepo, AiService aiService) {
        this.quizRepo = quizRepo;
        this.aiService = aiService;
    }

    @PostMapping("/quizzes")
    public Quiz createQuiz(@RequestBody QuizRequest r) {
        if (r.title() == null || r.title().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        Quiz quiz = new Quiz();
        quiz.setTitle(r.title().trim());
        quiz.setTopic(r.topic() == null || r.topic().isBlank() ? "General" : r.topic().trim());
        quiz.setDescription(r.description());
        return quizRepo.save(quiz);
    }

    @DeleteMapping("/quizzes/{id}")
    public Map<String, String> deleteQuiz(@PathVariable Long id) {
        quizRepo.deleteById(id);
        return Map.of("message", "Quiz deleted");
    }

    @PostMapping("/quizzes/{id}/questions")
    public Map<String, String> addQuestion(@PathVariable Long id, @RequestBody QuestionRequest r) {
        Quiz quiz = quizRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        quiz.getQuestions().add(buildQuestion(r, quiz));
        quizRepo.save(quiz);
        return Map.of("message", "Question added");
    }

    // AI feature: topic in -> a complete quiz saved in MySQL
    @PostMapping("/quizzes/generate")
    public Map<String, String> generate(@RequestBody GenerateRequest r) throws Exception {
        if (r.topic() == null || r.topic().isBlank()) {
            throw new IllegalArgumentException("Topic is required");
        }
        int count = Math.max(1, Math.min(r.count(), 10));
        String difficulty = r.difficulty() == null || r.difficulty().isBlank() ? "medium" : r.difficulty();

        List<QuestionRequest> generated = aiService.generateQuestions(r.topic(), count, difficulty);

        Quiz quiz = new Quiz();
        quiz.setTitle(r.topic().trim() + " Quiz");
        quiz.setTopic(r.topic().trim());
        quiz.setDescription("AI-generated " + difficulty + " quiz on " + r.topic().trim());
        for (QuestionRequest q : generated) {
            quiz.getQuestions().add(buildQuestion(q, quiz));
        }
        quizRepo.save(quiz);
        return Map.of("message", "Created \"" + quiz.getTitle() + "\" with " + generated.size() + " questions");
    }

    private Question buildQuestion(QuestionRequest r, Quiz quiz) {
        String correct = r.correctOption() == null ? "" : r.correctOption().trim().toUpperCase();
        if (!correct.matches("[ABCD]")) {
            throw new IllegalArgumentException("Correct option must be A, B, C or D");
        }
        if (r.text() == null || r.text().isBlank()) {
            throw new IllegalArgumentException("Question text is required");
        }
        Question q = new Question();
        q.setText(r.text());
        q.setOptionA(r.optionA());
        q.setOptionB(r.optionB());
        q.setOptionC(r.optionC());
        q.setOptionD(r.optionD());
        q.setCorrectOption(correct);
        q.setExplanation(r.explanation());
        q.setQuiz(quiz);
        return q;
    }
}
