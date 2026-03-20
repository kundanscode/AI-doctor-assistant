package com.rxanalyzer.model;

import lombok.Data;
import java.util.List;

@Data
public class SubstitutionResult {
    private String originalMedicine;
    private String identifiedComposition;
    private List<MedicineAlternative> alternatives;
    private String estimatedSavings;
}