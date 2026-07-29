package com.shopmanager.report.service.impl;

import com.shopmanager.report.dto.DailyTrendPoint;
import com.shopmanager.report.dto.DateRangeReportResponse;
import com.shopmanager.report.dto.SalesCategorySlice;
import com.shopmanager.report.repository.DateRangeReportRepository;
import com.shopmanager.report.service.DateRangeReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DateRangeReportServiceImpl implements DateRangeReportService {

    private final DateRangeReportRepository reportRepository;

    @Override
    public DateRangeReportResponse getReport(LocalDate fromDate, LocalDate toDate) {

        List<Object[]> rows = reportRepository.getDateRangeSummary(fromDate, toDate);
        // COUNT + COALESCE always yields exactly one row; guard defensively anyway.
        Object[] row = rows.isEmpty() ? new Object[6] : rows.get(0);

        return DateRangeReportResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalInvoices(row[0] != null ? (Long) row[0] : 0L)
                .subTotal(row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO)
                .totalTax(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO)
                .grandTotal(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO)
                .amountReceived(row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO)
                .pendingAmount(row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO)
                .totalRepairs(reportRepository.countRepairsBetween(fromDate, toDate))
                .dailyData(buildDailyTrend(fromDate, toDate))
                .salesBreakdown(buildSalesBreakdown(fromDate, toDate))
                .build();
    }

    // ---------------- DAILY TREND ----------------

    private List<DailyTrendPoint> buildDailyTrend(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            return new ArrayList<>();
        }

        Map<LocalDate, BigDecimal> salesByDay = toDateMap(reportRepository.getDailySales(from, to));
        Map<LocalDate, BigDecimal> repairsByDay = toDateMap(reportRepository.getDailyRepairs(from, to));

        List<DailyTrendPoint> points = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            points.add(DailyTrendPoint.builder()
                    .date(d.toString())
                    .sales(salesByDay.getOrDefault(d, BigDecimal.ZERO))
                    .repairs(repairsByDay.getOrDefault(d, BigDecimal.ZERO))
                    .build());
        }
        return points;
    }

    private Map<LocalDate, BigDecimal> toDateMap(List<Object[]> rows) {
        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] r : rows) {
            LocalDate day = toLocalDate(r[0]);
            if (day == null) continue;
            map.put(day, r[1] != null ? (BigDecimal) r[1] : BigDecimal.ZERO);
        }
        return map;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date sqlDate) return sqlDate.toLocalDate();
        if (value instanceof java.util.Date d) return d.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return LocalDate.parse(value.toString().substring(0, 10));
    }

    // ---------------- CATEGORY BREAKDOWN ----------------

    private List<SalesCategorySlice> buildSalesBreakdown(LocalDate from, LocalDate to) {
        List<Object[]> rows = reportRepository.getSalesByCategory(from, to);

        BigDecimal total = BigDecimal.ZERO;
        for (Object[] r : rows) {
            if (r[1] != null) total = total.add((BigDecimal) r[1]);
        }

        List<SalesCategorySlice> slices = new ArrayList<>();
        for (Object[] r : rows) {
            String category = r[0] != null ? r[0].toString() : "Other";
            BigDecimal amount = r[1] != null ? (BigDecimal) r[1] : BigDecimal.ZERO;
            int percentage = total.signum() == 0 ? 0
                    : amount.multiply(BigDecimal.valueOf(100))
                    .divide(total, 0, RoundingMode.HALF_UP).intValue();
            slices.add(SalesCategorySlice.builder()
                    .category(category)
                    .amount(amount)
                    .percentage(percentage)
                    .build());
        }
        return slices;
    }
}
