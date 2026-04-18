package com.bookvault.controller;

import com.bookvault.dto.request.ReservationRequest;
import com.bookvault.dto.response.ApiResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.dto.response.ReservationResponse;
import com.bookvault.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for book reservation endpoints.
 */
@RestController
@RequestMapping("/reservations")
@Tag(name = "Reservations", description = "Book reservation management")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Constructs the controller with its required service dependency.
     *
     * @param reservationService the reservation service
     */
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Returns a paginated list of all reservations.
     *
     * @param page page number
     * @param size page size
     * @return paged reservations
     */
    @GetMapping
    @Operation(summary = "List all reservations")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(reservationService.findAll(pageable)));
    }

    /**
     * Returns a single reservation by its ID.
     *
     * @param id the reservation ID
     * @return the reservation details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a reservation by ID")
    public ResponseEntity<ApiResponse<ReservationResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.findById(id)));
    }

    /**
     * Returns reservations for a specific member.
     *
     * @param memberId the member ID
     * @param page     page number
     * @param size     page size
     * @return paged reservations
     */
    @GetMapping("/member/{memberId}")
    @Operation(summary = "List reservations for a member")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> findByMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                ApiResponse.success(reservationService.findByMember(memberId, pageable)));
    }

    /**
     * Returns pending reservations for a specific book.
     *
     * @param bookId the book ID
     * @return list of pending reservations
     */
    @GetMapping("/book/{bookId}/pending")
    @Operation(summary = "List pending reservations for a book")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> findPendingByBook(
            @PathVariable Long bookId) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.findPendingByBook(bookId)));
    }

    /**
     * Places a book reservation.
     *
     * @param request the reservation request
     * @return the created reservation with 201 status
     */
    @PostMapping
    @Operation(summary = "Place a book reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> placeReservation(
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.placeReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Reservation placed successfully"));
    }

    /**
     * Cancels a reservation.
     *
     * @param id the reservation ID
     * @return the updated reservation
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancel(@PathVariable Long id) {
        ReservationResponse response = reservationService.cancelReservation(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Reservation cancelled"));
    }
}
