package com.rxanalyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class CompositionIdentifierService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String identifyComposition(String medicineName) {
        String prompt = """
                Given the medicine brand name "%s", identify its active
                pharmaceutical ingredient (salt/composition).

                Rules:
                - Return ONLY the salt name and strength
                - Example: "Pan 40" → "Pantoprazole 40mg"
                - Example: "Augmentin 625" → "Amoxycillin 500mg Clavulanic Acid 125mg"
                - Example: "Crocin 500" → "Paracetamol 500mg"
                - If misspelled, correct it and identify the composition
                - Return ONLY the composition string, nothing else
                - No explanation, no extra text
                """.formatted(medicineName);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)),
                "temperature", 0.0,
                "max_tokens", 50);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class);
            return extractContent(response.getBody());
        } catch (Exception e) {
            return medicineName;
        }
    }

    // Extract just the salt name without dosage
    // "Pantoprazole 40mg" → "Pantoprazole"
    public String extractSaltName(String composition) {
        if (composition == null)
            return null;
        return composition.split("\\s+")[0].trim();
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map responseBody) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return message.get("content").toString().trim();
    }
}