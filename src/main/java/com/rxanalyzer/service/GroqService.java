package com.rxanalyzer.service;

import com.rxanalyzer.model.PatientInfo;
import com.rxanalyzer.model.SpecialistRecommendation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final OllamaSpecialistService ollamaSpecialistService;

    public GroqService(OllamaSpecialistService ollamaSpecialistService) {
        this.ollamaSpecialistService = ollamaSpecialistService;
    }

    public SpecialistRecommendation recommendSpecialist(PatientInfo patientInfo) {
        try {
            return callGroq(patientInfo);
        } catch (Exception e) {
            // Fallback to Ollama
            return ollamaSpecialistService.recommendSpecialist(patientInfo);
        }
    }

    private SpecialistRecommendation callGroq(PatientInfo patientInfo) {
        String prompt = buildPrompt(patientInfo);

        // Build request body
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", """
                                You are a medical triage assistant.
                                You will be given patient symptoms and diagnoses.
                                Your job is to recommend exactly ONE most appropriate medical specialist.
                                Reply with ONLY the specialist name. Nothing else.
                                Examples of valid responses:
                                Neurologist
                                Cardiologist
                                General Physician
                                Gastroenterologist
                                Orthopedic Specialist
                                ENT Specialist
                                Dermatologist
                                Pulmonologist
                                Urologist
                                Ophthalmologist
                                Psychiatrist
                                Endocrinologist
                                """),
                        Map.of("role", "user", "content", prompt)),
                "temperature", 0.0,
                "max_tokens", 20);

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Call Groq API
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                Map.class);

        // Extract specialist name from response
        String specialistName = extractSpecialistFromResponse(response.getBody());

        SpecialistRecommendation recommendation = new SpecialistRecommendation();
        recommendation.setSpecialistType(specialistName);
        recommendation.setSource("GROQ");
        return recommendation;
    }

    private String buildPrompt(PatientInfo patientInfo) {
        return """
                Patient symptoms: %s
                Doctor's findings/diagnoses: %s
                Based on these symptoms and findings, which single medical specialist
                should this patient see?
                """.formatted(
                patientInfo.getSymptoms() != null ? patientInfo.getSymptoms().toString() : "not mentioned",
                patientInfo.getDiagnoses() != null ? patientInfo.getDiagnoses().toString() : "not mentioned");
    }

    @SuppressWarnings("unchecked")
    private String extractSpecialistFromResponse(Map responseBody) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return message.get("content").toString().trim();
    }
}