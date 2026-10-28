package com.quizapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", length = 1000)
    private String text;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    @Column(length = 1)
    private String correctOption;   // "A", "B", "C" or "D"

    @Column(length = 1000)
    private String explanation;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    /** Returns e.g. "B. Spring Boot" for a given letter. */
    public String optionText(String letter) {
        if (letter == null) return "Not answered";
        return switch (letter) {
            case "A" -> "A. " + optionA;
            case "B" -> "B. " + optionB;
            case "C" -> "C. " + optionC;
            case "D" -> "D. " + optionD;
            default -> "Not answered";
        };
    }
}
