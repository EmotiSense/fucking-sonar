package com.bookvault.controller;

import com.bookvault.dto.request.BookCreateRequest;
import com.bookvault.dto.response.BookResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.exception.GlobalExceptionHandler;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link BookController} using MockMvc in standalone mode.
 */
@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private BookResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        sampleResponse = buildSampleResponse();
    }

    @Test
    void findAll_shouldReturn200WithPage() throws Exception {
        PageResponse<BookResponse> page = buildPageResponse(List.of(sampleResponse));
        given(bookService.findAll(any(Pageable.class))).willReturn(page);
        mockMvc.perform(get("/books").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void findById_existing_shouldReturn200() throws Exception {
        given(bookService.findById(1L)).willReturn(sampleResponse);
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isbn").value("978-0-06-112008-4"))
                .andExpect(jsonPath("$.data.title").value("To Kill a Mockingbird"));
    }

    @Test
    void findById_missing_shouldReturn404() throws Exception {
        given(bookService.findById(99L))
                .willThrow(new ResourceNotFoundException("Book", 99L));
        mockMvc.perform(get("/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void findByIsbn_existing_shouldReturn200() throws Exception {
        given(bookService.findByIsbn("978-0-06-112008-4")).willReturn(sampleResponse);
        mockMvc.perform(get("/books/isbn/978-0-06-112008-4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isbn").value("978-0-06-112008-4"));
    }

    @Test
    void create_validRequest_shouldReturn201() throws Exception {
        BookCreateRequest request = buildCreateRequest();
        given(bookService.create(any(BookCreateRequest.class))).willReturn(sampleResponse);
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void create_missingIsbn_shouldReturn400() throws Exception {
        BookCreateRequest invalidRequest = new BookCreateRequest();
        invalidRequest.setTitle("Some Book");
        invalidRequest.setAuthor("Author");
        invalidRequest.setTotalCopies(1);
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void findAvailable_shouldReturn200WithList() throws Exception {
        given(bookService.findAvailable()).willReturn(List.of(sampleResponse));
        mockMvc.perform(get("/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void delete_existing_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void search_withKeyword_shouldReturn200() throws Exception {
        PageResponse<BookResponse> page = buildPageResponse(List.of(sampleResponse));
        given(bookService.search(eq("mockingbird"), any(Pageable.class))).willReturn(page);
        mockMvc.perform(get("/books/search").param("keyword", "mockingbird"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    private BookResponse buildSampleResponse() {
        BookResponse response = new BookResponse();
        response.setId(1L);
        response.setIsbn("978-0-06-112008-4");
        response.setTitle("To Kill a Mockingbird");
        response.setAuthor("Harper Lee");
        response.setTotalCopies(3);
        response.setAvailableCopies(3);
        response.setAvailable(true);
        return response;
    }

    private BookCreateRequest buildCreateRequest() {
        BookCreateRequest req = new BookCreateRequest();
        req.setIsbn("978-0-06-112008-4");
        req.setTitle("To Kill a Mockingbird");
        req.setAuthor("Harper Lee");
        req.setTotalCopies(3);
        return req;
    }

    @SuppressWarnings("unchecked")
    private <T> PageResponse<T> buildPageResponse(List<T> content) {
        org.springframework.data.domain.Page<T> springPage =
                new org.springframework.data.domain.PageImpl<>(content);
        return PageResponse.from(springPage);
    }
}
