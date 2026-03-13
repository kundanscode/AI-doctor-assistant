package com.rxanalyzer.model;

import java.util.List;

import lombok.Data;

@Data
public class PrescriptionResponse {
    private String rawText;
    private List<MedicineEntry> medicines;
    private String processingStatus;
    private String errorMessage;
}
