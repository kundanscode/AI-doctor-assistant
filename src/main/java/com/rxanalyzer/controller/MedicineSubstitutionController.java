package com.rxanalyzer.controller;

import com.rxanalyzer.model.MedicineEntry;
import com.rxanalyzer.model.SubstitutionResponse;
import com.rxanalyzer.service.MedicineSubstitutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescription")
public class MedicineSubstitutionController {

    private final MedicineSubstitutionService substitutionService;

    public MedicineSubstitutionController(
            MedicineSubstitutionService substitutionService) {
        this.substitutionService = substitutionService;
    }

    @PostMapping("/substitutes")
    public ResponseEntity<SubstitutionResponse> getSubstitutes(
            @RequestBody List<MedicineEntry> medicines) {

        if (medicines == null || medicines.isEmpty()) {
            SubstitutionResponse error = new SubstitutionResponse();
            error.setStatus("FAILED");
            error.setErrorMessage("No medicines provided");
            return ResponseEntity.badRequest().body(error);
        }

        return ResponseEntity.ok(
                substitutionService.findSubstitutes(medicines));
    }
}