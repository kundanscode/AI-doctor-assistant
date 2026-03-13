package com.rxanalyzer.controller;

import com.rxanalyzer.agent.SpecialistAgent;
import com.rxanalyzer.model.SpecialistAdviceRequest;
import com.rxanalyzer.model.SpecialistAdviceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescription")
public class SpecialistAdviceController {

    private final SpecialistAgent specialistAgent;

    public SpecialistAdviceController(SpecialistAgent specialistAgent) {
        this.specialistAgent = specialistAgent;
    }

    @PostMapping("/specialist-advice")
    public ResponseEntity<SpecialistAdviceResponse> getSpecialistAdvice(
            @RequestBody SpecialistAdviceRequest request) {

        if (request.getSpecialistType() == null ||
                request.getSpecialistType().isBlank()) {
            SpecialistAdviceResponse error = new SpecialistAdviceResponse();
            error.setStatus("FAILED");
            error.setErrorMessage("specialistType is required");
            return ResponseEntity.badRequest().body(error);
        }

        return ResponseEntity.ok(specialistAgent.analyze(request));
    }
}