package com.bookvault.controller;

import com.bookvault.dto.response.ApiResponse;
import com.bookvault.dto.response.FineResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.service.FineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for library fine management endpoints.
 */
@RestController
@RequestMapping("/fines")
@Tag(name = "Fines", description = "Library fine management")
public class FineController {

    private final FineService fineService;

    /**
     * Constructs the controller with its required service dependency.
     *
     * @param fineService the fine service
     */
    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    /**
     * Returns a paginated list of all fines.
     *
     * @param page page number
     * @param size page size
     * @return paged fines
     */
    @GetMapping
    @Operation(summary = "List all fines")
    public ResponseEntity<ApiResponse<PageResponse<FineResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(fineService.findAll(pageable)));
    }

    /**
     * Returns a single fine by its ID.
     *
     * @param id the fine ID
     * @return the fine details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a fine by ID")
    public ResponseEntity<ApiResponse<FineResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(fineService.findById(id)));
    }

    /**
     * Returns all fines for a specific member.
     *
     * @param memberId the member ID
     * @param page     page number
     * @param size     page size
     * @return paged fines for the member
     */
    @GetMapping("/member/{memberId}")
    @Operation(summary = "List fines for a member")
    public ResponseEntity<ApiResponse<PageResponse<FineResponse>>> findByMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(fineService.findByMember(memberId, pageable)));
    }

    /**
     * Returns all outstanding fines for a member.
     *
     * @param memberId the member ID
     * @return outstanding fines
     */
    @GetMapping("/member/{memberId}/outstanding")
    @Operation(summary = "List outstanding fines for a member")
    public ResponseEntity<ApiResponse<List<FineResponse>>> findOutstanding(
            @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(fineService.findOutstandingByMember(memberId)));
    }

    /**
     * Returns the total outstanding fine amount for a member.
     *
     * @param memberId the member ID
     * @return total outstanding amount
     */
    @GetMapping("/member/{memberId}/total")
    @Operation(summary = "Get total outstanding fine amount for a member")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalOutstanding(@PathVariable Long memberId) {
        BigDecimal total = fineService.getTotalOutstanding(memberId);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    /**
     * Issues a fine for a late-returned borrow record.
     *
     * @param borrowRecordId the borrow record ID
     * @return the created fine with 201 status
     */
    @PostMapping("/issue/{borrowRecordId}")
    @Operation(summary = "Issue a fine for a late return")
    public ResponseEntity<ApiResponse<FineResponse>> issueFine(
            @PathVariable Long borrowRecordId) {
        FineResponse response = fineService.issueFine(borrowRecordId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Fine issued successfully"));
    }

    /**
     * Records payment of a fine.
     *
     * @param fineId the fine ID
     * @return the updated fine
     */
    @PostMapping("/{fineId}/pay")
    @Operation(summary = "Pay a fine")
    public ResponseEntity<ApiResponse<FineResponse>> payFine(@PathVariable Long fineId) {
        FineResponse response = fineService.payFine(fineId);
        return ResponseEntity.ok(ApiResponse.success(response, "Fine paid successfully"));
    }

    /**
     * Waives a fine.
     *
     * @param fineId the fine ID
     * @return the updated fine
     */
    @PostMapping("/{fineId}/waive")
    @Operation(summary = "Waive a fine")
    public ResponseEntity<ApiResponse<FineResponse>> waiveFine(@PathVariable Long fineId) {
        FineResponse response = fineService.waiveFine(fineId);
        return ResponseEntity.ok(ApiResponse.success(response, "Fine waived successfully"));
    }
}
