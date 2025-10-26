package com.sanjay.questionservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Question title is required")
    @Column(nullable = false, unique = true, length = 1000)
    private String questionTitle;

    @NotBlank(message = "Option 1 is required")
    @Column(nullable = false, length = 500)
    private String option1;

    @NotBlank(message = "Option 2 is required")
    @Column(nullable = false, length = 500)
    private String option2;

    @NotBlank(message = "Option 3 is required")
    @Column(nullable = false, length = 500)
    private String option3;

    @NotBlank(message = "Option 4 is required")
    @Column(nullable = false, length = 500)
    private String option4;

    @NotBlank(message = "Answer is required")
    @Column(nullable = false, length = 500)
    private String answer;

    @Column(length = 50)
    private String difficultyLevel;

    @Column(length = 100)
    private String category;
}