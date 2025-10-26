package com.sanjay.quiz_service.service;

import com.sanjay.quiz_service.dto.QuizSummaryDto;
import com.sanjay.quiz_service.entity.QuestionWrapper;
import com.sanjay.quiz_service.entity.Quiz;
import com.sanjay.quiz_service.entity.Response;
import com.sanjay.quiz_service.feign.QuizInterface;
import com.sanjay.quiz_service.repository.QuizRepository;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizInterface quizInterface;

    @Transactional
    public ResponseEntity<String> createQuiz(String category, Integer noOfQ, String title) {
        try {
            if (category == null || category.trim().isEmpty()) {
                return new ResponseEntity<>("Category cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (noOfQ == null || noOfQ <= 0) {
                return new ResponseEntity<>("Number of questions must be positive", HttpStatus.BAD_REQUEST);
            }
            if (title == null || title.trim().isEmpty()) {
                return new ResponseEntity<>("Title cannot be empty", HttpStatus.BAD_REQUEST);
            }

            ResponseEntity<List<Integer>> response = quizInterface.getQuestionsForQuiz(category, noOfQ);

            if (response.getBody() == null || response.getBody().isEmpty()) {
                return new ResponseEntity<>("No questions available for the given category", HttpStatus.NOT_FOUND);
            }

            List<Integer> questionIds = response.getBody();

            Quiz quiz = new Quiz();
            quiz.setTitle(title);
            quiz.setQuestionIds(questionIds);
            quiz.setCategory(category); // Store category for listing
            quizRepository.save(quiz);

            return new ResponseEntity<>("Quiz created successfully with ID: " + quiz.getId(), HttpStatus.CREATED);
        } catch (FeignException e) {
            log.error("Feign error while creating quiz: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error communicating with question service", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Error creating quiz: {}", e.getMessage(), e);
            return new ResponseEntity<>("Failed to create quiz", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<List<QuizSummaryDto>> getAllQuizzes() {
        try {
            List<Quiz> quizzes = quizRepository.findAll();

            List<QuizSummaryDto> summaries = quizzes.stream()
                    .map(quiz -> QuizSummaryDto.builder()
                            .id(quiz.getId())
                            .title(quiz.getTitle())
                            .category(quiz.getCategory() != null ? quiz.getCategory() : "General")
                            .questionCount(quiz.getQuestionIds() != null ? quiz.getQuestionIds().size() : 0)
                            .build())
                    .collect(Collectors.toList());

            return new ResponseEntity<>(summaries, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching all quizzes: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(Integer id) {
        try {
            if (id == null || id <= 0) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
            }

            Optional<Quiz> quizOpt = quizRepository.findById(id);

            if (quizOpt.isEmpty()) {
                log.warn("Quiz not found with ID: {}", id);
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            Quiz quiz = quizOpt.get();
            List<Integer> questionIds = quiz.getQuestionIds();

            if (questionIds == null || questionIds.isEmpty()) {
                log.warn("Quiz {} has no questions", id);
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            ResponseEntity<List<QuestionWrapper>> questions = quizInterface.getQuestionsFromId(questionIds);

            return questions;
        } catch (FeignException e) {
            log.error("Feign error while fetching quiz questions: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Error fetching quiz questions for ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Integer> calculateResult(Integer id, List<Response> responses) {
        try {
            if (id == null || id <= 0) {
                return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
            }

            if (responses == null || responses.isEmpty()) {
                return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
            }

            // Verify quiz exists
            Optional<Quiz> quizOpt = quizRepository.findById(id);
            if (quizOpt.isEmpty()) {
                log.warn("Quiz not found with ID: {}", id);
                return new ResponseEntity<>(0, HttpStatus.NOT_FOUND);
            }

            ResponseEntity<Integer> score = quizInterface.getScore(responses);

            if (score.getBody() == null) {
                return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return score;
        } catch (FeignException e) {
            log.error("Feign error while calculating result: {}", e.getMessage(), e);
            return new ResponseEntity<>(0, HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Error calculating result for quiz {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}