package com.bookvault.dto.response;

import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.MemberStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read-only view of a {@link Member} entity returned to API clients.
 */
public class MemberResponse {

    private Long id;
    private String memberNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDate joinDate;
    private MemberStatus status;
    private LocalDateTime createdAt;

    public MemberResponse() {
        // Required for JSON deserialisation by Jackson
    }

    /**
     * Maps a {@link Member} entity to a response DTO.
     *
     * @param member the source entity
     * @return the populated response
     */
    public static MemberResponse from(Member member) {
        MemberResponse response = new MemberResponse();
        response.id = member.getId();
        response.memberNumber = member.getMemberNumber();
        response.firstName = member.getFirstName();
        response.lastName = member.getLastName();
        response.fullName = member.getFullName();
        response.email = member.getEmail();
        response.phone = member.getPhone();
        response.address = member.getAddress();
        response.joinDate = member.getJoinDate();
        response.status = member.getStatus();
        response.createdAt = member.getCreatedAt();
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMemberNumber() { return memberNumber; }
    public void setMemberNumber(String memberNumber) { this.memberNumber = memberNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
