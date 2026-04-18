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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for managing the library's book inventory.
 */
@Service
@Transactional(readOnly = true)
public class BookService {

    private static final String BOOK_ENTITY = "Book";
    private static final String CATEGORY_ENTITY = "Category";

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Constructs the service with its required repository dependencies.
     *
     * @param bookRepository     the book data store
     * @param categoryRepository the category data store
     */
    public BookService(BookRepository bookRepository,
                       CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Returns a paginated list of all books.
     *
     * @param pageable pagination and sorting parameters
     * @return paged book responses
     */
    public PageResponse<BookResponse> findAll(Pageable pageable) {
        Page<BookResponse> page = bookRepository.findAll(pageable)
                .map(BookResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns a single book by its database ID.
     *
     * @param id the book ID
     * @return the book response
     * @throws ResourceNotFoundException if no book exists with the given ID
     */
    public BookResponse findById(Long id) {
        return BookResponse.from(loadById(id));
    }

    /**
     * Returns a single book by its ISBN.
     *
     * @param isbn the ISBN string
     * @return the book response
     * @throws ResourceNotFoundException if no book exists with the given ISBN
     */
    public BookResponse findByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException(BOOK_ENTITY, isbn));
        return BookResponse.from(book);
    }

    /**
     * Searches books by title or author keyword.
     *
     * @param keyword  the search term
     * @param pageable pagination parameters
     * @return paged matching book responses
     */
    public PageResponse<BookResponse> search(String keyword, Pageable pageable) {
        Page<BookResponse> page = bookRepository
                .searchByTitleOrAuthor(keyword, pageable)
                .map(BookResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns all books in a given category.
     *
     * @param categoryId the category ID
     * @param pageable   pagination parameters
     * @return paged book responses
     * @throws ResourceNotFoundException if the category does not exist
     */
    public PageResponse<BookResponse> findByCategory(Long categoryId, Pageable pageable) {
        Category category = loadCategory(categoryId);
        Page<BookResponse> page = bookRepository
                .findByCategory(category, pageable)
                .map(BookResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns all books currently available for borrowing.
     *
     * @return list of available book responses
     */
    public List<BookResponse> findAvailable() {
        return bookRepository.findAllAvailable()
                .stream()
                .map(BookResponse::from)
                .toList();
    }

    /**
     * Adds a new book to the library catalogue.
     *
     * @param request the creation request
     * @return the created book response
     * @throws BusinessRuleException     if the ISBN is already registered
     * @throws ResourceNotFoundException if the specified category does not exist
     */
    @Transactional
    public BookResponse create(BookCreateRequest request) {
        assertIsbnUnique(request.getIsbn());
        Category category = resolveCategory(request.getCategoryId());
        Book book = buildBook(request, category);
        Book saved = bookRepository.save(book);
        return BookResponse.from(saved);
    }

    /**
     * Updates an existing book's metadata.
     *
     * @param id      the book ID
     * @param request the update request
     * @return the updated book response
     * @throws ResourceNotFoundException if no book or category exists with the given IDs
     */
    @Transactional
    public BookResponse update(Long id, BookUpdateRequest request) {
        Book book = loadById(id);
        applyUpdates(book, request);
        Book saved = bookRepository.save(book);
        return BookResponse.from(saved);
    }

    /**
     * Removes a book from the catalogue.
     *
     * @param id the book ID
     * @throws ResourceNotFoundException if no book exists with the given ID
     * @throws BusinessRuleException     if the book has outstanding borrows
     */
    @Transactional
    public void delete(Long id) {
        Book book = loadById(id);
        assertNoCopiesCheckedOut(book);
        bookRepository.delete(book);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Book loadById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BOOK_ENTITY, id));
    }

    private Category loadCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_ENTITY, categoryId));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return loadCategory(categoryId);
    }

    private void assertIsbnUnique(String isbn) {
        if (bookRepository.existsByIsbn(isbn)) {
            throw new BusinessRuleException(
                    "A book with ISBN '" + isbn + "' already exists");
        }
    }

    private void assertNoCopiesCheckedOut(Book book) {
        int checkedOut = book.getTotalCopies() - book.getAvailableCopies();
        if (checkedOut > 0) {
            throw new BusinessRuleException(
                    "Cannot delete book '" + book.getTitle()
                            + "' while " + checkedOut + " copies are checked out");
        }
    }

    private Book buildBook(BookCreateRequest request, Category category) {
        Book book = new Book(
                request.getIsbn(),
                request.getTitle(),
                request.getAuthor(),
                request.getPublisher(),
                request.getPublicationYear(),
                request.getTotalCopies(),
                category);
        book.setDescription(request.getDescription());
        return book;
    }

    private void applyUpdates(Book book, BookUpdateRequest request) {
        if (request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }
        if (request.getPublisher() != null) {
            book.setPublisher(request.getPublisher());
        }
        if (request.getPublicationYear() != null) {
            book.setPublicationYear(request.getPublicationYear());
        }
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }
        if (request.getTotalCopies() != null) {
            book.setTotalCopies(request.getTotalCopies());
        }
        if (request.getCategoryId() != null) {
            book.setCategory(loadCategory(request.getCategoryId()));
        }
    }
}
