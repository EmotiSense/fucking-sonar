package com.bookvault.service;

import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.MemberStatus;
import com.bookvault.dto.request.MemberCreateRequest;
import com.bookvault.dto.request.MemberUpdateRequest;
import com.bookvault.dto.response.MemberResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link MemberService}.
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member sampleMember;

    @BeforeEach
    void setUp() {
        sampleMember = new Member("BV-202401-000001", "Alice", "Smith",
                "alice@example.com", "555-0100", "1 Main St");
    }

    @Test
    void findAll_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        given(memberRepository.findAll(pageable))
                .willReturn(new PageImpl<>(List.of(sampleMember)));
        var result = memberService.findAll(pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findById_existing_shouldReturn() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(sampleMember));
        MemberResponse result = memberService.findById(1L);
        assertThat(result.getFirstName()).isEqualTo("Alice");
        assertThat(result.getFullName()).isEqualTo("Alice Smith");
    }

    @Test
    void findById_missing_shouldThrowNotFound() {
        given(memberRepository.findById(99L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> memberService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByMemberNumber_existing_shouldReturn() {
        given(memberRepository.findByMemberNumber("BV-202401-000001"))
                .willReturn(Optional.of(sampleMember));
        MemberResponse result = memberService.findByMemberNumber("BV-202401-000001");
        assertThat(result.getMemberNumber()).isEqualTo("BV-202401-000001");
    }

    @Test
    void register_uniqueEmail_shouldSaveMember() {
        MemberCreateRequest request = buildCreateRequest();
        given(memberRepository.existsByEmail("bob@example.com")).willReturn(false);
        given(memberRepository.save(any(Member.class))).willReturn(sampleMember);
        MemberResponse result = memberService.register(request);
        verify(memberRepository).save(any(Member.class));
        assertThat(result).isNotNull();
    }

    @Test
    void register_duplicateEmail_shouldThrowBusinessRule() {
        MemberCreateRequest request = buildCreateRequest();
        given(memberRepository.existsByEmail("bob@example.com")).willReturn(true);
        assertThatThrownBy(() -> memberService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void update_validRequest_shouldApplyChanges() {
        MemberUpdateRequest updateRequest = new MemberUpdateRequest();
        updateRequest.setPhone("555-9999");
        given(memberRepository.findById(1L)).willReturn(Optional.of(sampleMember));
        given(memberRepository.save(any(Member.class))).willReturn(sampleMember);
        MemberResponse result = memberService.update(1L, updateRequest);
        assertThat(result).isNotNull();
        verify(memberRepository).save(sampleMember);
    }

    @Test
    void suspend_activeMember_shouldChangStatus() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(sampleMember));
        given(memberRepository.save(any(Member.class))).willReturn(sampleMember);
        memberService.suspend(1L);
        assertThat(sampleMember.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
    }

    @Test
    void activate_suspendedMember_shouldChangeStatus() {
        sampleMember.suspend();
        given(memberRepository.findById(1L)).willReturn(Optional.of(sampleMember));
        given(memberRepository.save(any(Member.class))).willReturn(sampleMember);
        memberService.activate(1L);
        assertThat(sampleMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void memberCanBorrow_whenActive_shouldReturnTrue() {
        assertThat(sampleMember.canBorrow()).isTrue();
    }

    @Test
    void memberCanBorrow_whenSuspended_shouldReturnFalse() {
        sampleMember.suspend();
        assertThat(sampleMember.canBorrow()).isFalse();
    }

    private MemberCreateRequest buildCreateRequest() {
        MemberCreateRequest req = new MemberCreateRequest();
        req.setFirstName("Bob");
        req.setLastName("Jones");
        req.setEmail("bob@example.com");
        req.setPhone("555-0200");
        return req;
    }
}
