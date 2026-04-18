package com.bookvault.dto.response;

import com.bookvault.domain.entity.Book;

import java.time.LocalDateTime;

/**
 * Read-only view of a {@link Book} entity returned to API clients.
 */
public class BookResponse {

    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private int totalCopies;
    private int availableCopies;
    private String description;
    private String categoryName;
    private boolean available;
    private LocalDateTime createdAt;

    public BookResponse() {
        // Required for JSON deserialisation by Jackson
    }

    /**
     * Maps a {@link Book} entity to a response DTO.
     *
     * @param book the source entity
     * @return the populated response
     */
    public static BookResponse from(Book book) {
        BookResponse response = new BookResponse();
        response.id = book.getId();
        response.isbn = book.getIsbn();
        response.title = book.getTitle();
        response.author = book.getAuthor();
        response.publisher = book.getPublisher();
        response.publicationYear = book.getPublicationYear();
        response.totalCopies = book.getTotalCopies();
        response.availableCopies = book.getAvailableCopies();
        response.description = book.getDescription();
        response.available = book.isAvailable();
        response.createdAt = book.getCreatedAt();
        if (book.getCategory() != null) {
            response.categoryName = book.getCategory().getName();
        }
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
