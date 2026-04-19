package com.bookvault.dto.response;

import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.MemberStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-only view of a {@link Member} entity returned to API clients.
 */
public record MemberResponse(
        Long id,
        String memberNumber,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String address,
        LocalDate joinDate,
        MemberStatus status,
        LocalDateTime createdAt) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getMemberNumber(),
                member.getFirstName(),
                member.getLastName(),
                member.getFullName(),
                member.getEmail(),
                member.getPhone(),
                member.getAddress(),
                member.getJoinDate(),
                member.getStatus(),
                member.getCreatedAt());
    }
}
