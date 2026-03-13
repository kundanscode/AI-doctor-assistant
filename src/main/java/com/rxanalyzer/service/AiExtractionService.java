package com.rxanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxanalyzer.model.MedicineEntry;
import com.rxanalyzer.model.PatientInfo;
import com.rxanalyzer.model.PrescriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiExtractionService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiExtractionService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public PrescriptionResponse extractAll(String rawText) {
        String prompt = """
                You are a medical prescription parser.

                Your task is to extract structured information from the prescription text and return it as JSON.

                IMPORTANT:
                Return STRICTLY a valid JSON object.
                Your response MUST start with '{' and end with '}'.
                Do NOT include explanations, markdown, code blocks, or any text before or after the JSON.

                Do NOT guess or infer information that is not explicitly present in the text.

                If information is missing:
                - Use null for single values
                - Use [] for arrays

                =====================
                OUTPUT FORMAT
                =====================

                {
                  "patientInfo": {
                    "patientName": "full name of patient",
                    "age": "age of patient",
                    "sex": "M or F",
                    "consultantName": "doctor name",
                    "symptoms": ["symptom 1", "symptom 2"],
                    "diagnoses": ["diagnosis or clinical finding 1", "diagnosis 2"],
                    "visitDate": "date of visit",
                    "visitType": "1st Visit or Follow up"
                  },
                  "medicines": [
                    {
                      "medicineName": "medicine name",
                      "dosage": "dosage such as 500mg or 40mg",
                      "frequency": "morning only / afternoon only / night only / morning and night / morning afternoon night / twice daily",
                      "duration": "duration such as 5 days",
                      "instructions": "special instructions",
                      "notes": "any additional notes"
                    }
                  ]
                }

                =====================
                MEDICINE EXTRACTION RULES
                =====================

                Only extract real medicine entries.

                A medicine line starts with one of these prefixes:

                T. or Tab.   = Tablet
                Cap.         = Capsule
                Inj.         = Injection
                Syr. or Syp. = Syrup
                Oint.        = Ointment
                Drop.        = Eye/Ear drops

                Extraction rules:

                Medicine Name:
                - The text after the prefix is the medicine name.

                Dosage:
                - If a number appears immediately after the medicine name, treat it as dosage.
                - Convert it to mg if unit not specified.

                Frequency format in prescriptions:

                1-0-0 → morning only
                0-1-0 → afternoon only
                0-0-1 → night only
                1-0-1 → morning and night
                1-1-0 → morning and afternoon
                1-1-1 → morning afternoon night

                Convert these patterns into plain English for the frequency field.

                Ignore lines that contain:

                "Or any other cheaper generic medicine"

                This is a doctor's note and NOT a medicine.

                Ignore hospital name, address, and patient info when extracting medicines.

                =====================
                PATIENT INFORMATION EXTRACTION RULES
                =====================

                patientName:
                Look for the name written after "Name:".

                age:
                Look for number written after "Age:".

                sex:
                Look for M or F written after "Sex:".

                consultantName:
                Look for the doctor's name written after "Consultant:".

                visitDate:
                Look for date written after "Date:".

                =====================
                SYMPTOMS EXTRACTION RULES
                =====================

                Extract patient complaints.

                Include duration if mentioned.

                Vitals should also be included if present:

                BP
                Pulse
                Temperature
                SpO2

                If prescription mentions:

                no drug allergies
                nodrug allergue

                Interpret it as:

                "No known drug allergies"

                Add it to symptoms.

                Ignore hospital name, address, and administrative details.

                =====================
                DIAGNOSIS / CLINICAL FINDINGS EXTRACTION RULES
                =====================

                Extract doctor's examination findings.

                These are clinical findings and should be listed in the diagnoses array.

                =====================
                IMPORTANT EXTRACTION RULES
                =====================

                Only extract information explicitly present in the prescription text.

                Do NOT guess missing details.

                Do NOT invent medicines.

                Do NOT add information not present in the text.

                =====================
                PRESCRIPTION TEXT
                =====================

                %s
                """
                .formatted(rawText);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseFullResponse(response);
    }

    private PrescriptionResponse parseFullResponse(String jsonResponse) {
        PrescriptionResponse result = new PrescriptionResponse();
        try {
            String cleaned = jsonResponse.trim();
            if (cleaned.contains("{")) {
                cleaned = cleaned.substring(cleaned.indexOf("{"),
                        cleaned.lastIndexOf("}") + 1);
            }

            var node = objectMapper.readTree(cleaned);

            // Parse patient info
            if (node.has("patientInfo")) {
                PatientInfo patientInfo = objectMapper.treeToValue(
                        node.get("patientInfo"), PatientInfo.class);
                result.setPatientInfo(patientInfo);
            }

            // Parse medicines
            if (node.has("medicines")) {
                List<MedicineEntry> medicines = objectMapper.treeToValue(
                        node.get("medicines"),
                        objectMapper.getTypeFactory().constructCollectionType(
                                List.class, MedicineEntry.class));
                result.setMedicines(medicines);
            }

        } catch (Exception e) {
            MedicineEntry errorEntry = new MedicineEntry();
            errorEntry.setMedicineName("PARSE_ERROR: " + e.getMessage());
            result.setMedicines(new ArrayList<>(List.of(errorEntry)));
        }
        return result;
    }
}