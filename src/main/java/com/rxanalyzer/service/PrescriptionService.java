package com.rxanalyzer.service;

import com.rxanalyzer.model.PrescriptionResponse;
import com.rxanalyzer.model.SpecialistRecommendation;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PrescriptionService {

    private final OcrService ocrService;
    private final AiExtractionService aiExtractionService;
    private final GroqService groqService;

    public PrescriptionService(OcrService ocrService,
            AiExtractionService aiExtractionService,
            GroqService groqService) {
        this.ocrService = ocrService;
        this.aiExtractionService = aiExtractionService;
        this.groqService = groqService;
    }

    public PrescriptionResponse analyze(MultipartFile file) {
        PrescriptionResponse response = new PrescriptionResponse();
        try {
            // Step 1: OCR
            String rawText = ocrService.extractText(file);
            response.setRawText(rawText);

            // Step 2: AI extraction (Ollama)
            PrescriptionResponse aiResult = aiExtractionService.extractAll(rawText);
            response.setPatientInfo(aiResult.getPatientInfo());
            response.setMedicines(aiResult.getMedicines());

            // Step 3: Specialist recommendation (Groq + Ollama fallback)
            if (aiResult.getPatientInfo() != null) {
                SpecialistRecommendation specialist = groqService.recommendSpecialist(aiResult.getPatientInfo());
                response.setSpecialistRecommendation(specialist);
            }

            response.setProcessingStatus("SUCCESS");

        } catch (Exception e) {
            response.setProcessingStatus("FAILED");
            response.setErrorMessage(e.getMessage());
        }
        return response;
    }
}