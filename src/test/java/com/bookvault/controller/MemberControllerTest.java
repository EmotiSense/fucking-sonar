package com.bookvault.controller;

import com.bookvault.dto.request.MemberCreateRequest;
import com.bookvault.dto.response.MemberResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.domain.enums.MemberStatus;
import com.bookvault.exception.GlobalExceptionHandler;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link MemberController} using MockMvc in standalone mode.
 */
@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MemberResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        sampleResponse = buildSampleResponse();
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        PageResponse<MemberResponse> page = buildPageResponse(List.of(sampleResponse));
        given(memberService.findAll(any(Pageable.class))).willReturn(page);
        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void findById_existing_shouldReturn200() throws Exception {
        given(memberService.findById(1L)).willReturn(sampleResponse);
        mockMvc.perform(get("/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberNumber").value("BV-202401-000001"))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"));
    }

    @Test
    void findById_missing_shouldReturn404() throws Exception {
        given(memberService.findById(99L))
                .willThrow(new ResourceNotFoundException("Member", 99L));
        mockMvc.perform(get("/members/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_validRequest_shouldReturn201() throws Exception {
        MemberCreateRequest request = buildCreateRequest();
        given(memberService.register(any(MemberCreateRequest.class))).willReturn(sampleResponse);
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void register_invalidEmail_shouldReturn400() throws Exception {
        MemberCreateRequest invalid = new MemberCreateRequest();
        invalid.setFirstName("Alice");
        invalid.setLastName("Smith");
        invalid.setEmail("not-an-email");
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankFirstName_shouldReturn400() throws Exception {
        MemberCreateRequest invalid = new MemberCreateRequest();
        invalid.setFirstName("");
        invalid.setLastName("Smith");
        invalid.setEmail("alice@example.com");
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void suspend_shouldReturn200() throws Exception {
        sampleResponse.setStatus(MemberStatus.SUSPENDED);
        given(memberService.suspend(1L)).willReturn(sampleResponse);
        mockMvc.perform(patch("/members/1/suspend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    @Test
    void activate_shouldReturn200() throws Exception {
        given(memberService.activate(1L)).willReturn(sampleResponse);
        mockMvc.perform(patch("/members/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void findByMemberNumber_shouldReturn200() throws Exception {
        given(memberService.findByMemberNumber("BV-202401-000001")).willReturn(sampleResponse);
        mockMvc.perform(get("/members/number/BV-202401-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberNumber").value("BV-202401-000001"));
    }

    private MemberResponse buildSampleResponse() {
        MemberResponse response = new MemberResponse();
        response.setId(1L);
        response.setMemberNumber("BV-202401-000001");
        response.setFirstName("Alice");
        response.setLastName("Smith");
        response.setFullName("Alice Smith");
        response.setEmail("alice@example.com");
        response.setPhone("555-0100");
        response.setJoinDate(LocalDate.now());
        response.setStatus(MemberStatus.ACTIVE);
        return response;
    }

    private MemberCreateRequest buildCreateRequest() {
        MemberCreateRequest req = new MemberCreateRequest();
        req.setFirstName("Alice");
        req.setLastName("Smith");
        req.setEmail("alice@example.com");
        req.setPhone("555-0100");
        return req;
    }

    @SuppressWarnings("unchecked")
    private <T> PageResponse<T> buildPageResponse(List<T> content) {
        org.springframework.data.domain.Page<T> page =
                new org.springframework.data.domain.PageImpl<>(content);
        return PageResponse.from(page);
    }
}
