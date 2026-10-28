package com.quizapp.controller;

import com.quizapp.dto.Dtos.*;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.Result;
import com.quizapp.model.User;
import com.quizapp.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Endpoints for logged-in users: list quizzes, take a quiz, see history and leaderboard. */
@RestController
public class QuizController {
    private final QuizRepository quizRepo;
    private final QuestionRepository questionRepo;
    private final ResultRepository resultRepo;
    private final UserRepository userRepo;

    public QuizController(QuizRepository quizRepo, QuestionRepository questionRepo,
                          ResultRepository resultRepo, UserRepository userRepo) {
        this.quizRepo = quizRepo;
        this.questionRepo = questionRepo;
        this.resultRepo = resultRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/api/quizzes")
    public List<Quiz> allQuizzes() {
        return quizRepo.findAll();
    }

    // Correct answers are NOT sent to the browser, so nobody can cheat from dev tools
    @GetMapping("/api/quizzes/{id}/questions")
    public List<QuestionView> questions(@PathVariable Long id) {
        return questionRepo.findByQuizId(id).stream()
                .map(q -> new QuestionView(q.getId(), q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()))
                .toList();
    }

    // The score is calculated on the server
    @PostMapping("/api/quizzes/{id}/submit")
    public ResultResponse submit(@PathVariable Long id, @RequestBody SubmitRequest req, Authentication auth) {
        Quiz quiz = quizRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        User user = userRepo.findByEmail(auth.getName()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Map<Long, String> answers = req.answers() == null ? Map.of() : req.answers();

        List<Question> questions = questionRepo.findByQuizId(id);
        int score = 0;
        List<ReviewItem> review = new ArrayList<>();

        for (Question q : questions) {
            String chosen = answers.get(q.getId());
            boolean correct = q.getCorrectOption().equals(chosen);
            if (correct) score++;
            review.add(new ReviewItem(q.getText(), q.optionText(chosen), q.optionText(q.getCorrectOption()),
                    correct, q.getExplanation()));
        }

        Result result = new Result();
        result.setUser(user);
        result.setQuiz(quiz);
        result.setScore(score);
        result.setTotal(questions.size());
        result.setTakenAt(LocalDateTime.now());
        resultRepo.save(result);

        return new ResultResponse(score, questions.size(), review);
    }

    @GetMapping("/api/results/me")
    public List<HistoryItem> myHistory(Authentication auth) {
        return resultRepo.findByUserEmailOrderByTakenAtDesc(auth.getName()).stream()
                .map(r -> new HistoryItem(r.getQuiz().getTitle(), r.getScore(), r.getTotal(), r.getTakenAt().toString()))
                .toList();
    }

    @GetMapping("/api/leaderboard")
    public List<LeaderboardItem> leaderboard() {
        return resultRepo.leaderboard().stream()
                .map(row -> new LeaderboardItem((String) row[0], ((Number) row[1]).longValue()))
                .limit(10)
                .toList();
    }
}
