package com.shopmanager.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One point in the date-range daily trend (sales + repairs revenue for a day). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTrendPoint {
    private String date;        // ISO yyyy-MM-dd
    private BigDecimal sales;
    private BigDecimal repairs;
}
