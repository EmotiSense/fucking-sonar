package com.bookvault.controller;

import com.bookvault.dto.request.BookCreateRequest;
import com.bookvault.dto.request.BookUpdateRequest;
import com.bookvault.dto.response.ApiResponse;
import com.bookvault.dto.response.BookResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for book catalogue endpoints.
 */
@RestController
@RequestMapping("/books")
@Tag(name = "Books", description = "Library book catalogue management")
public class BookController {

    private final BookService bookService;

    /**
     * Constructs the controller with its required service dependency.
     *
     * @param bookService the book service
     */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Returns a paginated list of all books.
     *
     * @param page page number (0-indexed)
     * @param size number of items per page
     * @param sort sort field
     * @return paged book list
     */
    @GetMapping
    @Operation(summary = "List all books (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<BookResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        PageResponse<BookResponse> response = bookService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Returns a single book by its database ID.
     *
     * @param id the book ID
     * @return the book details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID")
    public ResponseEntity<ApiResponse<BookResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bookService.findById(id)));
    }

    /**
     * Returns a single book by its ISBN.
     *
     * @param isbn the ISBN
     * @return the book details
     */
    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get a book by ISBN")
    public ResponseEntity<ApiResponse<BookResponse>> findByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(ApiResponse.success(bookService.findByIsbn(isbn)));
    }

    /**
     * Searches books by title or author keyword.
     *
     * @param keyword the search term
     * @param page    page number
     * @param size    page size
     * @return paged search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search books by title or author")
    public ResponseEntity<ApiResponse<PageResponse<BookResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<BookResponse> response = bookService.search(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Returns books filtered by category.
     *
     * @param categoryId the category ID
     * @param page       page number
     * @param size       page size
     * @return paged book list
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "List books in a category")
    public ResponseEntity<ApiResponse<PageResponse<BookResponse>>> findByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<BookResponse> response = bookService.findByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Returns all books with at least one available copy.
     *
     * @return list of available books
     */
    @GetMapping("/available")
    @Operation(summary = "List available books")
    public ResponseEntity<ApiResponse<List<BookResponse>>> findAvailable() {
        return ResponseEntity.ok(ApiResponse.success(bookService.findAvailable()));
    }

    /**
     * Adds a new book to the catalogue.
     *
     * @param request the creation request
     * @return the created book with 201 status
     */
    @PostMapping
    @Operation(summary = "Add a new book")
    public ResponseEntity<ApiResponse<BookResponse>> create(
            @Valid @RequestBody BookCreateRequest request) {
        BookResponse response = bookService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Book added successfully"));
    }

    /**
     * Updates an existing book's metadata.
     *
     * @param id      the book ID
     * @param request the update request
     * @return the updated book
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a book")
    public ResponseEntity<ApiResponse<BookResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        BookResponse response = bookService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Book updated successfully"));
    }

    /**
     * Removes a book from the catalogue.
     *
     * @param id the book ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
