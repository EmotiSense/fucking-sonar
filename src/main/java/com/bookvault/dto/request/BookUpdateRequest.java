package com.bookvault.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating an existing book.
 * All fields are optional; only non-null values are applied.
 */
public class BookUpdateRequest {

    @Size(max = 300, message = "Title must not exceed 300 characters")
    private String title;

    @Size(max = 200, message = "Author must not exceed 200 characters")
    private String author;

    @Size(max = 200, message = "Publisher must not exceed 200 characters")
    private String publisher;

    private Integer publicationYear;

    @Min(value = 1, message = "Total copies must be at least 1")
    private Integer totalCopies;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Long categoryId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
