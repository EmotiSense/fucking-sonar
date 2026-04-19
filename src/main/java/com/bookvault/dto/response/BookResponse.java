package com.bookvault.dto.response;

import com.bookvault.domain.entity.Book;

import java.time.LocalDateTime;

/**
 * Read-only view of a {@link Book} entity returned to API clients.
 */
public record BookResponse(
        Long id,
        String isbn,
        String title,
        String author,
        String publisher,
        Integer publicationYear,
        int totalCopies,
        int availableCopies,
        String description,
        String categoryName,
        boolean available,
        LocalDateTime createdAt) {

    public static BookResponse from(Book book) {
        String catName = book.getCategory() != null ? book.getCategory().getName() : null;
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getPublicationYear(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getDescription(),
                catName,
                book.isAvailable(),
                book.getCreatedAt());
    }
}
