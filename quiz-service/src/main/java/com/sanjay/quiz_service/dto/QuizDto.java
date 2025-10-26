package com.sanjay.quiz_service.dto;

import lombok.Data;

@Data
public class QuizDto {
    String category;
    Integer noOfQ;
    String title;
}
