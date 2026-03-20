package com.rxanalyzer.config;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.rxanalyzer.model.Medicine;
import com.rxanalyzer.repository.MedicineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MedicineDataLoader implements CommandLineRunner {

    private final MedicineRepository medicineRepository;

    public MedicineDataLoader(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only load if database is empty
        if (medicineRepository.count() > 0) {
            log.info("Medicine database already loaded with {} records.",
                    medicineRepository.count());
            return;
        }

        log.info("Loading Indian medicine database...");
        loadMedicines();
    }

    private void loadMedicines() {
        try {
            ClassPathResource resource = new ClassPathResource("indian_medicines.csv");
            CSVReader reader = new CSVReader(
                    new InputStreamReader(resource.getInputStream()));

            List<String[]> rows = reader.readAll();
            List<Medicine> batch = new ArrayList<>();

            // Skip header row
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                try {
                    Medicine medicine = mapRowToMedicine(row);
                    if (medicine != null) {
                        batch.add(medicine);
                    }

                    // Save in batches of 500 for performance
                    if (batch.size() == 500) {
                        medicineRepository.saveAll(batch);
                        batch.clear();
                        log.info("Loaded {} medicines so far...", i);
                    }
                } catch (Exception e) {
                    // Skip malformed rows
                    log.warn("Skipping malformed row {}: {}", i, e.getMessage());
                }
            }

            // Save remaining
            if (!batch.isEmpty()) {
                medicineRepository.saveAll(batch);
            }

            log.info("Medicine database loaded successfully! Total: {} medicines",
                    medicineRepository.count());

        } catch (Exception e) {
            log.error("Failed to load medicine database: {}", e.getMessage());
        }
    }

    private Medicine mapRowToMedicine(String[] row) {
        if (row.length < 9)
            return null;

        Medicine medicine = new Medicine();
        medicine.setName(clean(row[1]));
        medicine.setPrice(parsePrice(row[2]));
        medicine.setIsDiscontinued(parseBoolean(row[3]));
        medicine.setManufacturerName(clean(row[4]));
        medicine.setType(clean(row[5]));
        medicine.setPackSizeLabel(clean(row[6]));
        medicine.setShortComposition1(clean(row[7]));
        medicine.setShortComposition2(clean(row[8]));
        return medicine;
    }

    private String clean(String value) {
        if (value == null)
            return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private BigDecimal parsePrice(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {
        if (value == null)
            return false;
        return value.trim().equalsIgnoreCase("TRUE");
    }
}