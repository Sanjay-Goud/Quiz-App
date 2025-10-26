package com.sanjay.questionservice.service;

import com.sanjay.questionservice.entity.Question;
import com.sanjay.questionservice.entity.QuestionWrapper;
import com.sanjay.questionservice.entity.Response;
import com.sanjay.questionservice.repository.QuestionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
public class QuestionService {

    private final QuestionRepository questionRepository;

    public ResponseEntity<List<Question>> getAllQuestions() {
        try {
            List<Question> questions = questionRepository.findAll();
            return new ResponseEntity<>(questions, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching all questions: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<List<Question>> getQuestionsByCategory(String category) {
        try {
            if (category == null || category.trim().isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
            }
            List<Question> questions = questionRepository.findAllByCategory(category);
            return new ResponseEntity<>(questions, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching questions by category {}: {}", category, e.getMessage(), e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<String> addQuestion(Question question) {
        try {
            if (question == null || question.getQuestionTitle() == null ||
                    question.getQuestionTitle().trim().isEmpty()) {
                return new ResponseEntity<>("Invalid question data", HttpStatus.BAD_REQUEST);
            }

            if (question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
                return new ResponseEntity<>("Answer is required", HttpStatus.BAD_REQUEST);
            }

            questionRepository.save(question);
            return new ResponseEntity<>("Question added successfully", HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate question title: {}", e.getMessage());
            return new ResponseEntity<>("Question with this title already exists", HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Error adding question: {}", e.getMessage(), e);
            return new ResponseEntity<>("Failed to add question", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<List<Integer>> getQuestionsForQuiz(String category, Integer noOfQ) {
        try {
            if (category == null || category.trim().isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
            }
            if (noOfQ == null || noOfQ <= 0) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
            }

            List<Integer> questionIds = questionRepository.findRandomQuestionsByCategory(category, noOfQ);

            if (questionIds.isEmpty()) {
                log.warn("No questions found for category: {}", category);
            }

            return new ResponseEntity<>(questionIds, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generating questions for quiz: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<List<QuestionWrapper>> getQuestionsFromId(List<Integer> questionIds) {
        try {
            if (questionIds == null || questionIds.isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
            }

            // Fixed: Use findAllById to avoid N+1 query problem
            List<Question> questions = questionRepository.findAllById(questionIds);

            if (questions.isEmpty()) {
                log.warn("No questions found for provided IDs");
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            // Convert to QuestionWrapper using stream
            List<QuestionWrapper> wrappers = questions.stream()
                    .map(question -> new QuestionWrapper(
                            question.getId(),
                            question.getQuestionTitle(),
                            question.getOption1(),
                            question.getOption2(),
                            question.getOption3(),
                            question.getOption4()
                    ))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(wrappers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching questions from IDs: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Integer> getScore(List<Response> responses) {
        try {
            if (responses == null || responses.isEmpty()) {
                return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
            }

            int right = 0;

            for (Response response : responses) {
                if (response.getId() == null || response.getResponse() == null) {
                    continue; // Skip invalid responses
                }

                Optional<Question> questionOpt = questionRepository.findById(response.getId());

                if (questionOpt.isPresent()) {
                    Question question = questionOpt.get();
                    if (response.getResponse().trim().equalsIgnoreCase(question.getAnswer().trim())) {
                        right++;
                    }
                } else {
                    log.warn("Question not found for ID: {}", response.getId());
                }
            }

            return new ResponseEntity<>(right, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error calculating score: {}", e.getMessage(), e);
            return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}