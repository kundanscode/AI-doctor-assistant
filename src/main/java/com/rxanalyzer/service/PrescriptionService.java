package com.rxanalyzer.service;

import com.rxanalyzer.model.MedicineEntry;
import com.rxanalyzer.model.PrescriptionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            // Step 1: OCR - extract raw text
            String rawText = ocrService.extractText(file);
            response.setRawText(rawText);

            // Step 2: AI - structure the raw text
            List<MedicineEntry> medicines = aiExtractionService
                    .extractMedicines(rawText);
            response.setMedicines(medicines);

            response.setProcessingStatus("SUCCESS");

        } catch (Exception e) {
            response.setProcessingStatus("FAILED");
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }
}