package com.quizapp.config;

import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.User;
import com.quizapp.repository.QuizRepository;
import com.quizapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Runs once at startup: creates the admin account and one sample quiz. */
@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepo;
    private final QuizRepository quizRepo;
    private final PasswordEncoder encoder;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepo, QuizRepository quizRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.quizRepo = quizRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepo.existsByEmail("admin@quiz.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@quiz.com");
            admin.setPassword(encoder.encode(adminPassword));
            admin.setRole("ADMIN");
            userRepo.save(admin);
        }

        if (quizRepo.count() == 0) {
            Quiz quiz = new Quiz();
            quiz.setTitle("Core Java Basics");
            quiz.setTopic("Java");
            quiz.setDescription("Warm-up questions on core Java fundamentals");
            quiz.getQuestions().add(q(quiz, "Which keyword is used to inherit a class in Java?",
                    "implements", "extends", "inherits", "super", "B", "'extends' is used for class inheritance; 'implements' is for interfaces."));
            quiz.getQuestions().add(q(quiz, "Which collection does NOT allow duplicate elements?",
                    "ArrayList", "LinkedList", "HashSet", "Vector", "C", "A Set stores unique elements only."));
            quiz.getQuestions().add(q(quiz, "What is the size of an int in Java?",
                    "8 bits", "16 bits", "32 bits", "64 bits", "C", "An int is always 32 bits in Java, on every platform."));
            quizRepo.save(quiz);
        }
    }

    private Question q(Quiz quiz, String text, String a, String b, String c, String d, String correct, String why) {
        Question q = new Question();
        q.setText(text);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectOption(correct);
        q.setExplanation(why);
        q.setQuiz(quiz);
        return q;
    }
}
