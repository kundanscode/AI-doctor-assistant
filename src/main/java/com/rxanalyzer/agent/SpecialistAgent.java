package com.rxanalyzer.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxanalyzer.model.MedicineEntry;
import com.rxanalyzer.model.PatientInfo;
import com.rxanalyzer.model.SpecialistAdviceRequest;
import com.rxanalyzer.model.SpecialistAdviceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class SpecialistAgent {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpecialistAdviceResponse analyze(SpecialistAdviceRequest request) {
        try {
            return callGroq(request);
        } catch (Exception e) {
            SpecialistAdviceResponse error = new SpecialistAdviceResponse();
            error.setStatus("FAILED");
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }

    private SpecialistAdviceResponse callGroq(SpecialistAdviceRequest request) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content",
                                buildSystemPrompt(request.getSpecialistType())),
                        Map.of("role", "user", "content",
                                buildUserPrompt(request))),
                "temperature", 0.0,
                "max_tokens", 1000);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Map.class);

        String content = extractContent(response.getBody());
        return parseResponse(content, request.getSpecialistType());
    }

    private String buildSystemPrompt(String specialistType) {
        return """
                You are an experienced %s doctor.
                Analyze the patient details and provide advice in simple English
                that a patient can easily understand.
                Return ONLY a valid JSON object. No explanation, no markdown, no code blocks.
                Use exactly this structure:
                {
                  "medicalExplanation": "simple explanation of the condition in 2-3 sentences",
                  "remedies": ["remedy 1", "remedy 2", "remedy 3"],
                  "dietaryAdvice": ["dietary tip 1", "dietary tip 2", "dietary tip 3"],
                  "lifestyleChanges": ["lifestyle tip 1", "lifestyle tip 2", "lifestyle tip 3"]
                }
                """.formatted(specialistType);
    }

    private String buildUserPrompt(SpecialistAdviceRequest request) {
        PatientInfo info = request.getPatientInfo();
        List<MedicineEntry> medicines = request.getMedicines();

        return """
                Patient Name: %s
                Age: %s, Sex: %s
                Symptoms: %s
                Diagnoses: %s
                Prescribed Medicines: %s
                Please analyze this patient's condition and provide your advice.
                """.formatted(
                info.getPatientName() != null ? info.getPatientName() : "Unknown",
                info.getAge() != null ? info.getAge() : "Unknown",
                info.getSex() != null ? info.getSex() : "Unknown",
                info.getSymptoms() != null ? info.getSymptoms() : "None",
                info.getDiagnoses() != null ? info.getDiagnoses() : "None",
                medicines != null ? medicines.stream()
                        .map(m -> m.getMedicineName() + " " +
                                (m.getDosage() != null ? m.getDosage() : "") + " " +
                                (m.getFrequency() != null ? m.getFrequency() : ""))
                        .toList() : "None");
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map responseBody) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return message.get("content").toString().trim();
    }

    @SuppressWarnings("unchecked")
    private SpecialistAdviceResponse parseResponse(
            String jsonResponse, String specialistType) {
        SpecialistAdviceResponse response = new SpecialistAdviceResponse();
        response.setSpecialistType(specialistType);
        response.setSource("GROQ");
        try {
            String cleaned = jsonResponse.trim();
            if (cleaned.contains("{")) {
                cleaned = cleaned.substring(cleaned.indexOf("{"),
                        cleaned.lastIndexOf("}") + 1);
            }
            var node = objectMapper.readTree(cleaned);

            if (node.has("medicalExplanation"))
                response.setMedicalExplanation(
                        node.get("medicalExplanation").asText());
            if (node.has("remedies"))
                response.setRemedies(objectMapper.treeToValue(
                        node.get("remedies"), List.class));
            if (node.has("dietaryAdvice"))
                response.setDietaryAdvice(objectMapper.treeToValue(
                        node.get("dietaryAdvice"), List.class));
            if (node.has("lifestyleChanges"))
                response.setLifestyleChanges(objectMapper.treeToValue(
                        node.get("lifestyleChanges"), List.class));

            response.setStatus("SUCCESS");
        } catch (Exception e) {
            response.setMedicalExplanation(jsonResponse);
            response.setStatus("PARSE_ERROR");
        }
        return response;
    }
}