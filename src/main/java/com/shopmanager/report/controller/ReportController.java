package com.shopmanager.report.controller;

import com.shopmanager.dto.report.AdvancedDashboardDto;
import com.shopmanager.dto.report.DailyReportDto;
import com.shopmanager.dto.report.DashboardSummaryDto;
import com.shopmanager.dto.report.MonthlyReportDto;
import com.shopmanager.service.ReportPdfService;
import com.shopmanager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/custom-reports")
@RequiredArgsConstructor
public class ReportController {

    @Qualifier("customReportService")
    private final ReportService reportService;

    // NOTE: Excel export (Apache POI) was removed to reduce memory on the 512MB
    // free tier. The frontend only uses the PDF exports below.

    @GetMapping("/dashboard")
    public DashboardSummaryDto getDashboard() {
        return reportService.getDashboardSummary();
    }

    @GetMapping("/daily")
    public DailyReportDto getDaily(@RequestParam String date) {
        return reportService.getDailyReport(date);
    }

    @GetMapping("/monthly")
    public MonthlyReportDto getMonthly(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return reportService.getMonthlyReport(year, month);
    }

    @GetMapping("/advanced-dashboard")
    public AdvancedDashboardDto getAdvancedDashboard() {
        return reportService.getAdvancedDashboard();
    }

    @GetMapping("/advanced-dashboard-fast")
    public AdvancedDashboardDto getAdvancedDashboardFast() {
        return reportService.getAdvancedDashboardOptimized();
    }

    private final ReportPdfService reportPdfService;

    @GetMapping("/export/daily-pdf")
    public ResponseEntity<byte[]> exportDailyPdf(@RequestParam String date) {

        byte[] pdf = reportPdfService.exportDailyPdf(date);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=daily-report.pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdf);
    }

    @GetMapping("/export/monthly-pdf")
    public ResponseEntity<byte[]> exportMonthlyPdf(
            @RequestParam int year,
            @RequestParam int month) {

        byte[] pdf = reportPdfService.exportMonthlyPdf(year, month);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=monthly-report.pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdf);
    }
}