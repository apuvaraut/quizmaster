package com.quizapp.controller;

import com.quizapp.dto.Dtos.ExplainRequest;
import com.quizapp.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    // Student got a question wrong -> AI explains it in simple words
    @PostMapping("/api/ai/explain")
    public Map<String, String> explain(@RequestBody ExplainRequest r) throws Exception {
        String prompt = "A student is practicing with a quiz. Explain in simple words (maximum 4 sentences) "
                + "why the correct answer is right and what mistake the student may have made.\n"
                + "Question: " + r.question() + "\n"
                + "Student's answer: " + r.yourAnswer() + "\n"
                + "Correct answer: " + r.correctAnswer();
        return Map.of("explanation", aiService.ask(prompt));
    }
}
