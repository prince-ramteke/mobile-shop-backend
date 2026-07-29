package com.shopmanager.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoice_sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "year" is a reserved word in H2 (and some other DBs); quote it so the
    // generated SQL is portable. The physical column name stays `year`, so this
    // is a no-op migration on the existing MySQL schema.
    @Column(name = "`year`", nullable = false, unique = true)
    private Integer year;

    @Column(nullable = false)
    private Long lastNumber;
}