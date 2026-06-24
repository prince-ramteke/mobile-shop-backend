package com.shopmanager.dashboard.controller;

import com.shopmanager.dashboard.DashboardStatsResponse;
import com.shopmanager.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard Controller - Provides statistics and quick overview data
 * for the dashboard UI.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/revenue-chart")
    public ResponseEntity<List<Map<String, Object>>> getRevenueChart(
            @RequestParam(defaultValue = "week") String period) {
        return ResponseEntity.ok(dashboardService.getRevenueChartData(period));
    }

    @GetMapping("/repair-status")
    public ResponseEntity<List<Map<String, Object>>> getRepairStatusDistribution() {
        return ResponseEntity.ok(dashboardService.getRepairStatusDistribution());
    }

    @GetMapping
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/today-summary")
    public ResponseEntity<Map<String, Object>> getTodaySummary() {
        return ResponseEntity.ok(dashboardService.getTodaySummary());
    }

    @GetMapping("/quick-stats")
    public ResponseEntity<Map<String, Object>> getQuickStats() {
        return ResponseEntity.ok(dashboardService.getQuickStats());
    }

    @GetMapping("/pending-counts")
    public ResponseEntity<Map<String, Long>> getPendingCounts() {
        return ResponseEntity.ok(dashboardService.getPendingCounts());
    }
}