package com.bookvault.service;

import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.MemberStatus;
import com.bookvault.dto.request.MemberCreateRequest;
import com.bookvault.dto.request.MemberUpdateRequest;
import com.bookvault.dto.response.MemberResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.MemberRepository;
import com.bookvault.util.MemberNumberGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for managing library membership.
 */
@Service
@Transactional(readOnly = true)
public class MemberService {

    private static final String ENTITY_NAME = "Member";

    private final MemberRepository memberRepository;

    /**
     * Constructs the service with its required repository dependency.
     *
     * @param memberRepository the member data store
     */
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Returns a paginated list of all members.
     *
     * @param pageable pagination and sorting parameters
     * @return paged member responses
     */
    public PageResponse<MemberResponse> findAll(Pageable pageable) {
        Page<MemberResponse> page = memberRepository.findAll(pageable)
                .map(MemberResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns a single member by their database ID.
     *
     * @param id the member ID
     * @return the member response
     * @throws ResourceNotFoundException if no member exists with the given ID
     */
    public MemberResponse findById(Long id) {
        return MemberResponse.from(loadById(id));
    }

    /**
     * Returns a member by their library card number.
     *
     * @param memberNumber the library card number
     * @return the member response
     * @throws ResourceNotFoundException if no member exists with the given number
     */
    public MemberResponse findByMemberNumber(String memberNumber) {
        Member member = memberRepository.findByMemberNumber(memberNumber)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, memberNumber));
        return MemberResponse.from(member);
    }

    /**
     * Searches members by name.
     *
     * @param keyword  the search term
     * @param pageable pagination parameters
     * @return paged matching member responses
     */
    public PageResponse<MemberResponse> searchByName(String keyword, Pageable pageable) {
        Page<MemberResponse> page = memberRepository.searchByName(keyword, pageable)
                .map(MemberResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns all members with a specific status.
     *
     * @param status   the member status filter
     * @param pageable pagination parameters
     * @return paged member responses
     */
    public PageResponse<MemberResponse> findByStatus(MemberStatus status, Pageable pageable) {
        Page<MemberResponse> page = memberRepository.findByStatus(status, pageable)
                .map(MemberResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Registers a new library member.
     *
     * @param request the creation request
     * @return the created member response
     * @throws BusinessRuleException if the email is already registered
     */
    @Transactional
    public MemberResponse register(MemberCreateRequest request) {
        assertEmailUnique(request.getEmail());
        String memberNumber = MemberNumberGenerator.generate();
        Member member = new Member(
                memberNumber,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress());
        Member saved = memberRepository.save(member);
        return MemberResponse.from(saved);
    }

    /**
     * Updates an existing member's profile.
     *
     * @param id      the member ID
     * @param request the update request
     * @return the updated member response
     * @throws ResourceNotFoundException if no member exists with the given ID
     * @throws BusinessRuleException     if the new email conflicts with another member
     */
    @Transactional
    public MemberResponse update(Long id, MemberUpdateRequest request) {
        Member member = loadById(id);
        if (request.getEmail() != null) {
            assertEmailUniqueExcluding(request.getEmail(), id);
        }
        applyUpdates(member, request);
        Member saved = memberRepository.save(member);
        return MemberResponse.from(saved);
    }

    /**
     * Suspends a member's account.
     *
     * @param id the member ID
     * @return the updated member response
     * @throws ResourceNotFoundException if no member exists with the given ID
     */
    @Transactional
    public MemberResponse suspend(Long id) {
        Member member = loadById(id);
        member.suspend();
        return MemberResponse.from(memberRepository.save(member));
    }

    /**
     * Reactivates a suspended member's account.
     *
     * @param id the member ID
     * @return the updated member response
     * @throws ResourceNotFoundException if no member exists with the given ID
     */
    @Transactional
    public MemberResponse activate(Long id) {
        Member member = loadById(id);
        member.activate();
        return MemberResponse.from(memberRepository.save(member));
    }

    /**
     * Loads the internal {@link Member} entity by ID.
     *
     * @param id the member ID
     * @return the member entity
     * @throws ResourceNotFoundException if not found
     */
    public Member loadById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, id));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void assertEmailUnique(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessRuleException(
                    "A member with email '" + email + "' already exists");
        }
    }

    private void assertEmailUniqueExcluding(String email, Long excludedId) {
        memberRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(excludedId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException(
                            "A member with email '" + email + "' already exists");
                });
    }

    private void applyUpdates(Member member, MemberUpdateRequest request) {
        if (request.getFirstName() != null) {
            member.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            member.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            member.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            member.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            member.setAddress(request.getAddress());
        }
    }
}
