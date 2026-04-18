package com.bookvault.service;

import com.bookvault.domain.entity.Book;
import com.bookvault.domain.entity.Category;
import com.bookvault.dto.request.BookCreateRequest;
import com.bookvault.dto.request.BookUpdateRequest;
import com.bookvault.dto.response.BookResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.BookRepository;
import com.bookvault.repository.CategoryRepository;
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
 * Unit tests for {@link BookService}.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;
    private Category sampleCategory;
    private BookCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category("Fiction", "Fictional works");
        sampleBook = new Book("978-0-06-112008-4", "To Kill a Mockingbird",
                "Harper Lee", "HarperCollins", 1960, 3, sampleCategory);
        createRequest = buildCreateRequest();
    }

    @Test
    void findAll_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        given(bookRepository.findAll(pageable))
                .willReturn(new PageImpl<>(List.of(sampleBook)));
        PageResponse<BookResponse> result = bookService.findAll(pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void findById_existingBook_shouldReturn() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(sampleBook));
        BookResponse result = bookService.findById(1L);
        assertThat(result.getIsbn()).isEqualTo("978-0-06-112008-4");
        assertThat(result.getTitle()).isEqualTo("To Kill a Mockingbird");
    }

    @Test
    void findById_nonExisting_shouldThrowNotFound() {
        given(bookRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> bookService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByIsbn_existingIsbn_shouldReturn() {
        given(bookRepository.findByIsbn("978-0-06-112008-4"))
                .willReturn(Optional.of(sampleBook));
        BookResponse result = bookService.findByIsbn("978-0-06-112008-4");
        assertThat(result.getTitle()).isEqualTo("To Kill a Mockingbird");
    }

    @Test
    void create_newIsbn_shouldSaveBook() {
        given(bookRepository.existsByIsbn(createRequest.getIsbn())).willReturn(false);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(sampleCategory));
        given(bookRepository.save(any(Book.class))).willReturn(sampleBook);
        BookResponse result = bookService.create(createRequest);
        verify(bookRepository).save(any(Book.class));
        assertThat(result).isNotNull();
    }

    @Test
    void create_duplicateIsbn_shouldThrowBusinessRule() {
        given(bookRepository.existsByIsbn(createRequest.getIsbn())).willReturn(true);
        assertThatThrownBy(() -> bookService.create(createRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void update_existingBook_shouldApplyChanges() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(sampleBook));
        given(bookRepository.save(any(Book.class))).willReturn(sampleBook);
        BookUpdateRequest updateRequest = new BookUpdateRequest();
        updateRequest.setTitle("Updated Title");
        BookResponse result = bookService.update(1L, updateRequest);
        assertThat(result).isNotNull();
        verify(bookRepository).save(sampleBook);
    }

    @Test
    void delete_bookWithAllCopiesAvailable_shouldDelete() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(sampleBook));
        bookService.delete(1L);
        verify(bookRepository).delete(sampleBook);
    }

    @Test
    void delete_bookWithCheckedOutCopies_shouldThrowBusinessRule() {
        sampleBook.decrementAvailable();
        given(bookRepository.findById(1L)).willReturn(Optional.of(sampleBook));
        assertThatThrownBy(() -> bookService.delete(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("checked out");
    }

    @Test
    void findAvailable_shouldReturnOnlyAvailableBooks() {
        given(bookRepository.findAllAvailable()).willReturn(List.of(sampleBook));
        List<BookResponse> result = bookService.findAvailable();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isAvailable()).isTrue();
    }

    private BookCreateRequest buildCreateRequest() {
        BookCreateRequest req = new BookCreateRequest();
        req.setIsbn("978-0-06-112008-4");
        req.setTitle("To Kill a Mockingbird");
        req.setAuthor("Harper Lee");
        req.setTotalCopies(3);
        req.setCategoryId(1L);
        return req;
    }
}
