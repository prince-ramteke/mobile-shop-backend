package com.shopmanager.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeReportResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private long totalInvoices;
    private BigDecimal subTotal;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;
    private BigDecimal amountReceived;
    private BigDecimal pendingAmount;
    private long totalRepairs;

    // Daily sales/repairs trend across the range (one point per day).
    private List<DailyTrendPoint> dailyData;

    // Sales grouped by item category (Accessory / Service / Repair).
    private List<SalesCategorySlice> salesBreakdown;
}