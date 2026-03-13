package com.rxanalyzer.model;

import lombok.Data;
import java.util.List;

@Data
public class PatientInfo {
    private String patientName;
    private String age;
    private String sex;
    private String consultantName;
    private List<String> symptoms;
    private List<String> diagnoses;
    private String visitDate;
    private String visitType;
}