package com.rxanalyzer.model;

import lombok.Data;

@Data
public class MedicineEntry {
    private String medicineName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
    private String notes;

}
