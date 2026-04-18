package com.bookvault.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Represents a book held in the library inventory.
 * <p>
 * Each book record tracks the total number of copies and how many are
 * currently available for borrowing.
 * </p>
 */
@Entity
@Table(name = "books")
public class Book extends BaseEntity {

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "author", nullable = false, length = 200)
    private String author;

    @Column(name = "publisher", length = 200)
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "total_copies", nullable = false)
    private int totalCopies;

    @Column(name = "available_copies", nullable = false)
    private int availableCopies;

    @Column(name = "description", length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Required by JPA. */
    protected Book() {
    }

    /**
     * Creates a new book entry with the given details.
     *
     * @param isbn            the ISBN identifier
     * @param title           the book title
     * @param author          the primary author
     * @param publisher       the publisher name
     * @param publicationYear the year of first publication
     * @param totalCopies     the number of physical copies held
     * @param category        the library category
     */
    public Book(String isbn, String title, String author,
                String publisher, Integer publicationYear,
                int totalCopies, Category category) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.category = category;
    }

    /**
     * Decrements the available copy count when a book is borrowed.
     *
     * @throws IllegalStateException if no copies are currently available
     */
    public void decrementAvailable() {
        if (availableCopies <= 0) {
            throw new IllegalStateException(
                    "No available copies for book: " + isbn);
        }
        availableCopies--;
    }

    /**
     * Increments the available copy count when a book is returned.
     *
     * @throws IllegalStateException if available would exceed total copies
     */
    public void incrementAvailable() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException(
                    "Available copies cannot exceed total copies for book: " + isbn);
        }
        availableCopies++;
    }

    /**
     * Checks whether at least one copy is available for borrowing.
     *
     * @return {@code true} if copies are available
     */
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Book{id=" + getId() + ", isbn='" + isbn
                + "', title='" + title + "', author='" + author + "'}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Book book = (Book) other;
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }
}
