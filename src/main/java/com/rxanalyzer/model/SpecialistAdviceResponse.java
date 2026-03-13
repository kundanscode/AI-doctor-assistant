package com.rxanalyzer.model;

import lombok.Data;
import java.util.List;

@Data
public class SpecialistAdviceResponse {
    private String specialistType;
    private String medicalExplanation;
    private List<String> remedies;
    private List<String> dietaryAdvice;
    private List<String> lifestyleChanges;
    private String source;
    private String status;
    private String errorMessage;
}