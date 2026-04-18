package com.bookvault.repository;

import com.bookvault.domain.entity.Book;
import com.bookvault.domain.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Book} entities.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Finds a book by its ISBN.
     *
     * @param isbn the ISBN string
     * @return an optional containing the book, if found
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Checks whether a book with the given ISBN already exists.
     *
     * @param isbn the ISBN to check
     * @return {@code true} if a record exists
     */
    boolean existsByIsbn(String isbn);

    /**
     * Returns all books belonging to a specific category.
     *
     * @param category the category filter
     * @param pageable pagination and sorting specification
     * @return a page of books in the category
     */
    Page<Book> findByCategory(Category category, Pageable pageable);

    /**
     * Full-text search across title and author fields (case-insensitive).
     *
     * @param keyword  the search term
     * @param pageable pagination and sorting specification
     * @return a page of matching books
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))"
            + " OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByTitleOrAuthor(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Returns all books that have at least one available copy.
     *
     * @return list of available books
     */
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    List<Book> findAllAvailable();

    /**
     * Returns books by a specific author (case-insensitive).
     *
     * @param author   the author name to match
     * @param pageable pagination specification
     * @return page of books by the author
     */
    Page<Book> findByAuthorIgnoreCase(String author, Pageable pageable);
}
