package com.bookvault.repository;

import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Member} entities.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * Finds a member by their unique library card number.
     *
     * @param memberNumber the library card number
     * @return an optional containing the member, if found
     */
    Optional<Member> findByMemberNumber(String memberNumber);

    /**
     * Finds a member by their email address.
     *
     * @param email the email address
     * @return an optional containing the member, if found
     */
    Optional<Member> findByEmail(String email);

    /**
     * Checks whether a member with the given email already exists.
     *
     * @param email the email to check
     * @return {@code true} if a record exists
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether a member with the given library card number already exists.
     *
     * @param memberNumber the library card number to check
     * @return {@code true} if a record exists
     */
    boolean existsByMemberNumber(String memberNumber);

    /**
     * Returns all members with the given status.
     *
     * @param status   the member status filter
     * @param pageable pagination specification
     * @return page of members with the specified status
     */
    Page<Member> findByStatus(MemberStatus status, Pageable pageable);

    /**
     * Searches members by name (first or last, case-insensitive).
     *
     * @param keyword  the name search term
     * @param pageable pagination specification
     * @return page of matching members
     */
    @Query("SELECT m FROM Member m WHERE LOWER(m.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))"
            + " OR LOWER(m.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Member> searchByName(@Param("keyword") String keyword, Pageable pageable);
}
