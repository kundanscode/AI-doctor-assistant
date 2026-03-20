package com.rxanalyzer.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String name;

    private BigDecimal price;

    private Boolean isDiscontinued;

    @Column(length = 500)
    private String manufacturerName;

    @Column(length = 100)
    private String type;

    @Column(length = 200)
    private String packSizeLabel;

    @Column(length = 500)
    private String shortComposition1;

    @Column(length = 500)
    private String shortComposition2;
}