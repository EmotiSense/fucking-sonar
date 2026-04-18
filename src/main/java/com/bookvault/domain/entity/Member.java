package com.bookvault.domain.entity;

import com.bookvault.domain.enums.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a registered library member.
 * <p>
 * A member can borrow books, make reservations, and accumulate fines.
 * The status field controls whether the member is currently permitted
 * to perform borrowing operations.
 * </p>
 */
@Entity
@Table(name = "members")
public class Member extends BaseEntity {

    @Column(name = "member_number", nullable = false, unique = true, length = 20)
    private String memberNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    /** Required by JPA. */
    protected Member() {
    }

    /**
     * Creates a new active library member.
     *
     * @param memberNumber a unique library card number
     * @param firstName    the member's given name
     * @param lastName     the member's family name
     * @param email        a unique contact email address
     * @param phone        an optional phone number
     * @param address      an optional postal address
     */
    public Member(String memberNumber, String firstName, String lastName,
                  String email, String phone, String address) {
        this.memberNumber = memberNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.joinDate = LocalDate.now();
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * Returns the member's full display name.
     *
     * @return first and last name separated by a space
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Checks whether this member is permitted to borrow books.
     *
     * @return {@code true} when the member status is {@link MemberStatus#ACTIVE}
     */
    public boolean canBorrow() {
        return MemberStatus.ACTIVE == status;
    }

    /**
     * Suspends the member, preventing further borrowing.
     */
    public void suspend() {
        this.status = MemberStatus.SUSPENDED;
    }

    /**
     * Reactivates a previously suspended member.
     */
    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Member{id=" + getId() + ", memberNumber='" + memberNumber
                + "', name='" + getFullName() + "', email='" + email + "'}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Member member = (Member) other;
        return Objects.equals(memberNumber, member.memberNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberNumber);
    }
}
