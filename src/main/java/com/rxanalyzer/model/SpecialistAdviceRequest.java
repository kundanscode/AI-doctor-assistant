package com.rxanalyzer.model;

import lombok.Data;
import java.util.List;

@Data
public class SpecialistAdviceRequest {
    private String specialistType;
    private PatientInfo patientInfo;
    private List<MedicineEntry> medicines;
}