package com.bookvault.controller;

import com.bookvault.dto.response.ApiResponse;
import com.bookvault.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller exposing library reporting and statistics endpoints.
 */
@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Library statistics and reporting")
public class ReportController {

    private final ReportService reportService;

    /**
     * Constructs the controller with its required service dependency.
     *
     * @param reportService the report service
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Returns a high-level summary of key library metrics.
     *
     * @return a map of metric names to values
     */
    @GetMapping("/summary")
    @Operation(summary = "Get library summary statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        Map<String, Object> summary = reportService.getLibrarySummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Library summary retrieved"));
    }

    /**
     * Returns a breakdown of borrow record counts by status.
     *
     * @return a map of status to count
     */
    @GetMapping("/borrows/by-status")
    @Operation(summary = "Borrow records breakdown by status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getBorrowBreakdown() {
        Map<String, Long> breakdown = reportService.getBorrowStatusBreakdown();
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }

    /**
     * Returns a breakdown of fine counts by status.
     *
     * @return a map of status to count
     */
    @GetMapping("/fines/by-status")
    @Operation(summary = "Fines breakdown by status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getFineBreakdown() {
        Map<String, Long> breakdown = reportService.getFineStatusBreakdown();
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }

    /**
     * Returns inventory statistics.
     *
     * @return a map of inventory metric names to values
     */
    @GetMapping("/inventory")
    @Operation(summary = "Get inventory statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventoryStats() {
        Map<String, Object> stats = reportService.getInventoryStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
