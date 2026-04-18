package com.bookvault.controller;

import com.bookvault.domain.enums.MemberStatus;
import com.bookvault.dto.request.MemberCreateRequest;
import com.bookvault.dto.request.MemberUpdateRequest;
import com.bookvault.dto.response.ApiResponse;
import com.bookvault.dto.response.MemberResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for library member management endpoints.
 */
@RestController
@RequestMapping("/members")
@Tag(name = "Members", description = "Library member management")
public class MemberController {

    private final MemberService memberService;

    /**
     * Constructs the controller with its required service dependency.
     *
     * @param memberService the member service
     */
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * Returns a paginated list of all members.
     *
     * @param page page number
     * @param size items per page
     * @param sort sort field
     * @return paged member list
     */
    @GetMapping
    @Operation(summary = "List all members (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<MemberResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(ApiResponse.success(memberService.findAll(pageable)));
    }

    /**
     * Returns a single member by their database ID.
     *
     * @param id the member ID
     * @return the member details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a member by ID")
    public ResponseEntity<ApiResponse<MemberResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.findById(id)));
    }

    /**
     * Returns a member by their library card number.
     *
     * @param memberNumber the library card number
     * @return the member details
     */
    @GetMapping("/number/{memberNumber}")
    @Operation(summary = "Get a member by library card number")
    public ResponseEntity<ApiResponse<MemberResponse>> findByMemberNumber(
            @PathVariable String memberNumber) {
        return ResponseEntity.ok(ApiResponse.success(memberService.findByMemberNumber(memberNumber)));
    }

    /**
     * Searches members by name.
     *
     * @param keyword the search term
     * @param page    page number
     * @param size    page size
     * @return paged search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search members by name")
    public ResponseEntity<ApiResponse<PageResponse<MemberResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(memberService.searchByName(keyword, pageable)));
    }

    /**
     * Returns members filtered by status.
     *
     * @param status member status
     * @param page   page number
     * @param size   page size
     * @return paged member list
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "List members by status")
    public ResponseEntity<ApiResponse<PageResponse<MemberResponse>>> findByStatus(
            @PathVariable MemberStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(memberService.findByStatus(status, pageable)));
    }

    /**
     * Registers a new library member.
     *
     * @param request the registration request
     * @return the created member with 201 status
     */
    @PostMapping
    @Operation(summary = "Register a new member")
    public ResponseEntity<ApiResponse<MemberResponse>> register(
            @Valid @RequestBody MemberCreateRequest request) {
        MemberResponse response = memberService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Member registered successfully"));
    }

    /**
     * Updates an existing member's profile.
     *
     * @param id      the member ID
     * @param request the update request
     * @return the updated member
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a member profile")
    public ResponseEntity<ApiResponse<MemberResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody MemberUpdateRequest request) {
        MemberResponse response = memberService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Member updated successfully"));
    }

    /**
     * Suspends a member's account.
     *
     * @param id the member ID
     * @return the updated member
     */
    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspend a member account")
    public ResponseEntity<ApiResponse<MemberResponse>> suspend(@PathVariable Long id) {
        MemberResponse response = memberService.suspend(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Member suspended"));
    }

    /**
     * Reactivates a suspended member's account.
     *
     * @param id the member ID
     * @return the updated member
     */
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a member account")
    public ResponseEntity<ApiResponse<MemberResponse>> activate(@PathVariable Long id) {
        MemberResponse response = memberService.activate(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Member activated"));
    }
}
