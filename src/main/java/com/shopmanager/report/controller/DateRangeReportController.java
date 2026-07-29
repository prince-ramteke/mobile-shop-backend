package com.shopmanager.report.controller;

import com.shopmanager.report.dto.DateRangeReportResponse;
import com.shopmanager.report.service.DateRangeReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

// NOTE: authorization intentionally left open to stay consistent with the rest
// of the API (all /api/** is permitAll in SecurityConfig). A lone @PreAuthorize
// here would make this the only endpoint returning 403 on a missing/expired JWT,
// the same trap that broke the Settings module.
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class DateRangeReportController {

    private final DateRangeReportService reportService;

    @GetMapping("/range")
    public DateRangeReportResponse getDateRangeReport(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return reportService.getReport(from, to);
    }
}