package com.sanjay.quiz_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSummaryDto {
    private Integer id;
    private String title;
    private Integer questionCount;
    private String category;
}