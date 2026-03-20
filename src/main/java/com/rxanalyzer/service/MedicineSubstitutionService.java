package com.rxanalyzer.service;

import com.rxanalyzer.model.*;
import com.rxanalyzer.repository.MedicineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MedicineSubstitutionService {

    private final CompositionIdentifierService compositionIdentifier;
    private final MedicineRepository medicineRepository;

    public MedicineSubstitutionService(
            CompositionIdentifierService compositionIdentifier,
            MedicineRepository medicineRepository) {
        this.compositionIdentifier = compositionIdentifier;
        this.medicineRepository = medicineRepository;
    }

    public SubstitutionResponse findSubstitutes(List<MedicineEntry> medicines) {
        SubstitutionResponse response = new SubstitutionResponse();
        List<SubstitutionResult> results = new ArrayList<>();

        try {
            for (MedicineEntry medicine : medicines) {
                SubstitutionResult result = processOneMedicine(medicine);
                results.add(result);
            }
            response.setSubstitutions(results);
            response.setStatus("SUCCESS");
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    private SubstitutionResult processOneMedicine(MedicineEntry medicine) {
        SubstitutionResult result = new SubstitutionResult();
        result.setOriginalMedicine(medicine.getMedicineName());

        try {
            // Step 1 — Ask Groq to identify composition
            String composition = compositionIdentifier
                    .identifyComposition(medicine.getMedicineName());
            result.setIdentifiedComposition(composition);
            log.info("Medicine: {} → Composition: {}",
                    medicine.getMedicineName(), composition);

            // Step 2 — Extract salt name for DB search
            String saltName = compositionIdentifier.extractSaltName(composition);

            // Step 3 — Search database
            List<Medicine> dbResults = medicineRepository
                    .findByComposition(saltName);

            // Step 4 — Map to alternatives (top 5 cheapest)
            List<MedicineAlternative> alternatives = dbResults.stream()
                    .limit(5)
                    .map(this::mapToAlternative)
                    .toList();
            result.setAlternatives(alternatives);

            // Step 5 — Calculate estimated savings
            result.setEstimatedSavings(
                    calculateSavings(dbResults));

        } catch (Exception e) {
            log.error("Error processing medicine {}: {}",
                    medicine.getMedicineName(), e.getMessage());
            result.setAlternatives(new ArrayList<>());
            result.setEstimatedSavings("Unable to calculate");
        }

        return result;
    }

    private MedicineAlternative mapToAlternative(Medicine medicine) {
        MedicineAlternative alt = new MedicineAlternative();
        alt.setBrandName(medicine.getName());
        alt.setManufacturer(medicine.getManufacturerName());
        alt.setPrice(medicine.getPrice());
        alt.setPackSize(medicine.getPackSizeLabel());
        alt.setComposition(medicine.getShortComposition1());
        alt.setType(medicine.getType());
        return alt;
    }

    private String calculateSavings(List<Medicine> medicines) {
        if (medicines == null || medicines.isEmpty())
            return "No data";

        // Filter medicines with valid prices
        List<BigDecimal> prices = medicines.stream()
                .filter(m -> m.getPrice() != null
                        && m.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .map(Medicine::getPrice)
                .sorted()
                .toList();

        if (prices.isEmpty())
            return "Price data unavailable";
        if (prices.size() == 1)
            return "₹" + prices.get(0);

        BigDecimal cheapest = prices.get(0);
        BigDecimal mostExpensive = prices.get(prices.size() - 1);

        if (mostExpensive.compareTo(BigDecimal.ZERO) == 0)
            return "No data";

        BigDecimal savingsPercent = mostExpensive.subtract(cheapest)
                .divide(mostExpensive, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return "Cheapest option ₹" + cheapest +
                " — save up to " + savingsPercent + "% vs most expensive";
    }
}