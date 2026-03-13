package com.rxanalyzer.service;

import com.rxanalyzer.model.PrescriptionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PrescriptionService {

    private final OcrService ocrService;
    private final AiExtractionService aiExtractionService;

    public PrescriptionService(OcrService ocrService,
            AiExtractionService aiExtractionService) {
        this.ocrService = ocrService;
        this.aiExtractionService = aiExtractionService;
    }

    public PrescriptionResponse analyze(MultipartFile file) {
        PrescriptionResponse response = new PrescriptionResponse();
        try {
            // Step 1: OCR
            String rawText = ocrService.extractText(file);
            response.setRawText(rawText);

            // Step 2: AI extraction
            PrescriptionResponse aiResult = aiExtractionService.extractAll(rawText);
            response.setPatientInfo(aiResult.getPatientInfo());
            response.setMedicines(aiResult.getMedicines());

            response.setProcessingStatus("SUCCESS");

        } catch (Exception e) {
            response.setProcessingStatus("FAILED");
            response.setErrorMessage(e.getMessage());
        }
        return response;
    }
}