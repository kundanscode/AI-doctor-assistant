package com.rxanalyzer.controller;

import com.rxanalyzer.model.PrescriptionResponse;
import com.rxanalyzer.service.PrescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<PrescriptionResponse> analyze(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            PrescriptionResponse error = new PrescriptionResponse();
            error.setProcessingStatus("FAILED");
            error.setErrorMessage("No file uploaded");
            return ResponseEntity.badRequest().body(error);
        }

        PrescriptionResponse response = prescriptionService.analyze(file);
        return ResponseEntity.ok(response);
    }
}