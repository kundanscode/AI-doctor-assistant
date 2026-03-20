package com.rxanalyzer.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MedicineAlternative {
    private String brandName;
    private String manufacturer;
    private BigDecimal price;
    private String packSize;
    private String composition;
    private String type;
}