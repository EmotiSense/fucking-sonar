package com.bookvault.controller;

import com.bookvault.dto.request.BorrowRequest;
import com.bookvault.dto.response.ApiResponse;
import com.bookvault.dto.response.BorrowRecordResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for book borrow and return endpoints.
 */
@RestController
@RequestMapping("/borrows")
@Tag(name = "Borrows", description = "Book checkout and return operations")
public class BorrowController {

    private final BorrowService borrowService;

    /**
     * Constructs the controller with its required service dependency.
     *
     * @param borrowService the borrow service
     */
    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * Returns a paginated list of all borrow records.
     *
     * @param page page number
     * @param size page size
     * @return paged borrow records
     */
    @GetMapping
    @Operation(summary = "List all borrow records")
    public ResponseEntity<ApiResponse<PageResponse<BorrowRecordResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(borrowService.findAll(pageable)));
    }

    /**
     * Returns a single borrow record by ID.
     *
     * @param id the record ID
     * @return the borrow record details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a borrow record by ID")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(borrowService.findById(id)));
    }

    /**
     * Returns borrow records for a specific member.
     *
     * @param memberId the member ID
     * @param page     page number
     * @param size     page size
     * @return paged borrow records
     */
    @GetMapping("/member/{memberId}")
    @Operation(summary = "List borrow records for a member")
    public ResponseEntity<ApiResponse<PageResponse<BorrowRecordResponse>>> findByMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(borrowService.findByMember(memberId, pageable)));
    }

    /**
     * Returns all currently active borrow records.
     *
     * @param page page number
     * @param size page size
     * @return paged active records
     */
    @GetMapping("/active")
    @Operation(summary = "List active borrow records")
    public ResponseEntity<ApiResponse<PageResponse<BorrowRecordResponse>>> findActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(borrowService.findActive(pageable)));
    }

    /**
     * Returns all currently overdue borrow records.
     *
     * @return list of overdue records
     */
    @GetMapping("/overdue")
    @Operation(summary = "List overdue borrow records")
    public ResponseEntity<ApiResponse<List<BorrowRecordResponse>>> findOverdue() {
        return ResponseEntity.ok(ApiResponse.success(borrowService.findOverdue()));
    }

    /**
     * Checks out a book to a member.
     *
     * @param request the borrow request
     * @return the created borrow record with 201 status
     */
    @PostMapping("/checkout")
    @Operation(summary = "Check out a book to a member")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> checkout(
            @Valid @RequestBody BorrowRequest request) {
        BorrowRecordResponse response = borrowService.borrowBook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Book checked out successfully"));
    }

    /**
     * Returns a borrowed book.
     *
     * @param recordId the borrow record ID
     * @return the updated borrow record
     */
    @PostMapping("/{recordId}/return")
    @Operation(summary = "Return a borrowed book")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> returnBook(
            @PathVariable Long recordId) {
        BorrowRecordResponse response = borrowService.returnBook(recordId);
        return ResponseEntity.ok(ApiResponse.success(response, "Book returned successfully"));
    }
}
