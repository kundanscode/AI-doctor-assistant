package com.rxanalyzer.model;

import lombok.Data;
import java.util.List;

@Data
public class PrescriptionResponse {
    private String rawText;
    private PatientInfo patientInfo;
    private List<MedicineEntry> medicines;
    private String processingStatus;
    private String errorMessage;
}