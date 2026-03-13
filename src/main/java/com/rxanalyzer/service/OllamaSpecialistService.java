package com.rxanalyzer.service;

import com.rxanalyzer.model.PatientInfo;
import com.rxanalyzer.model.SpecialistRecommendation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class OllamaSpecialistService {

    private final ChatClient chatClient;

    public OllamaSpecialistService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public SpecialistRecommendation recommendSpecialist(PatientInfo patientInfo) {
        String prompt = """
                You are a medical triage assistant.
                Based on the following patient symptoms and diagnoses,
                recommend exactly ONE most appropriate medical specialist.
                Reply with ONLY the specialist name. Nothing else.

                Patient symptoms: %s
                Doctor findings: %s
                """.formatted(
                patientInfo.getSymptoms() != null ? patientInfo.getSymptoms().toString() : "not mentioned",
                patientInfo.getDiagnoses() != null ? patientInfo.getDiagnoses().toString() : "not mentioned");

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content()
                .trim();

        SpecialistRecommendation recommendation = new SpecialistRecommendation();
        recommendation.setSpecialistType(response);
        recommendation.setSource("OLLAMA");
        return recommendation;
    }
}