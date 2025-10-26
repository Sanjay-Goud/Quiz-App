package com.sanjay.questionservice.controller;

import com.sanjay.questionservice.entity.Question;
import com.sanjay.questionservice.entity.QuestionWrapper;
import com.sanjay.questionservice.entity.Response;
import com.sanjay.questionservice.service.QuestionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("question")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("allQuestions")
    public ResponseEntity<List<Question>> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @GetMapping("category/{category}")
    public ResponseEntity<List<Question>> getQuestionsByCategory(@PathVariable String category) {
        if (category == null || category.trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return questionService.getQuestionsByCategory(category);
    }

    @PostMapping("addQuestion")
    public ResponseEntity<String> addQuestion(@RequestBody @Valid Question question) {
        if (question == null) {
            return new ResponseEntity<>("Question cannot be null", HttpStatus.BAD_REQUEST);
        }
        return questionService.addQuestion(question);
    }

    @GetMapping("generate")
    public ResponseEntity<List<Integer>> getQuestionsForQuiz(
            @RequestParam String category,
            @RequestParam Integer noOfQ) {
        if (category == null || category.trim().isEmpty() || noOfQ == null || noOfQ <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return questionService.getQuestionsForQuiz(category, noOfQ);
    }

    @PostMapping("getQuestions")
    public ResponseEntity<List<QuestionWrapper>> getQuestionsFromId(@RequestBody List<Integer> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return questionService.getQuestionsFromId(questionIds);
    }

    @PostMapping("getScore")
    public ResponseEntity<Integer> getScore(@RequestBody List<Response> responses) {
        if (responses == null || responses.isEmpty()) {
            return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
        }
        return questionService.getScore(responses);
    }
}