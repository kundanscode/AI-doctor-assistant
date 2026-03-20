package com.rxanalyzer.model;

import lombok.Data;
import java.util.List;

@Data
public class SubstitutionResponse {
    private List<SubstitutionResult> substitutions;
    private String status;
    private String errorMessage;
}