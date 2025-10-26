package com.sanjay.quiz_service.controller;

import com.sanjay.quiz_service.dto.QuizDto;
import com.sanjay.quiz_service.entity.QuestionWrapper;
import com.sanjay.quiz_service.entity.Response;
import com.sanjay.quiz_service.service.QuizService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("quiz")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("create")
    public ResponseEntity<String> createQuiz(@RequestBody @Valid QuizDto quizDto) {
        if (quizDto == null) {
            return new ResponseEntity<>("Quiz data cannot be null", HttpStatus.BAD_REQUEST);
        }
        return quizService.createQuiz(
                quizDto.getCategory(),
                quizDto.getNoOfQ(),
                quizDto.getTitle()
        );
    }

    @GetMapping("get/{id}")
    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return quizService.getQuizQuestions(id);
    }

    @PostMapping("submit/{id}")
    public ResponseEntity<Integer> submitQuiz(
            @PathVariable Integer id,
            @RequestBody List<Response> responses) {
        if (id == null || id <= 0 || responses == null || responses.isEmpty()) {
            return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
        }
        return quizService.calculateResult(id, responses);
    }
}