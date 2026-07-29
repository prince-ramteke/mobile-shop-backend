package com.shopmanager.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Sales total for one item category (Accessory / Service / Repair) with its share. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesCategorySlice {
    private String category;
    private BigDecimal amount;
    private int percentage;
}
