package com.shopmanager.report.controller;

import com.shopmanager.report.dto.GstSummaryReportDto;
import com.shopmanager.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Exposes the GST summary that {@link ReportService#getGstSummary} already
 * computes from real sales data. Previously there was no endpoint for it, so
 * the frontend GST Report page always fell back to fabricated demo numbers.
 */
@RestController
@RequestMapping("/api/custom-reports")
@RequiredArgsConstructor
public class GstReportController {

    // Only one bean implements com.shopmanager.report.service.ReportService,
    // so this injects unambiguously by type.
    private final ReportService reportService;

    @GetMapping("/gst")
    public GstSummaryReportDto getGstReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return reportService.getGstSummary(startDate, endDate);
    }
}
