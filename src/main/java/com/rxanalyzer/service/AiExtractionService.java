package com.rxanalyzer.service;

import com.rxanalyzer.model.MedicineEntry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiExtractionService {

    private final ChatClient chatClient;

    public AiExtractionService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public List<MedicineEntry> extractMedicines(String rawText) {
        String prompt = """
                You are a medical prescription parser.
                Extract all medicines from the following prescription text.

                Return ONLY a valid JSON array. No explanation, no markdown, no code blocks.
                Use exactly this structure:
                [
                  {
                    "medicineName": "medicine name here",
                    "dosage": "dosage here e.g. 500mg",
                    "frequency": "frequency here e.g. twice daily",
                    "duration": "duration here e.g. 5 days",
                    "notes": "any special instructions e.g. after meals"
                  }
                ]

                Rules:
                - If a field is not mentioned, set it to null
                - Do not add any text before or after the JSON array
                - Handle common medical abbreviations (BD=twice daily, TDS=thrice daily, OD=once daily, SOS=as needed)

                Prescription text:
                %s
                """.formatted(rawText);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseMedicines(response);
    }

    private List<MedicineEntry> parseMedicines(String jsonResponse) {
        try {
            // Clean response in case model adds any extra text
            String cleaned = jsonResponse.trim();
            if (cleaned.contains("[")) {
                cleaned = cleaned.substring(cleaned.indexOf("["),
                        cleaned.lastIndexOf("]") + 1);
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

            return mapper.readValue(cleaned,
                    mapper.getTypeFactory().constructCollectionType(
                            List.class, MedicineEntry.class));

        } catch (Exception e) {
            // If parsing fails return empty list with error info
            MedicineEntry errorEntry = new MedicineEntry();
            errorEntry.setMedicineName("PARSE_ERROR: " + e.getMessage());
            return new ArrayList<>(List.of(errorEntry));
        }
    }
}