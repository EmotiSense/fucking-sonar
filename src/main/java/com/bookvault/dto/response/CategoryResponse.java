package com.bookvault.dto.response;

import com.bookvault.domain.entity.Category;

/**
 * Read-only view of a {@link Category} entity returned to API clients.
 */
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private int bookCount;

    public CategoryResponse() {
        // Required for JSON deserialisation by Jackson
    }

    /**
     * Maps a {@link Category} entity to a response DTO.
     *
     * @param category the source entity
     * @return the populated response
     */
    public static CategoryResponse from(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.id = category.getId();
        response.name = category.getName();
        response.description = category.getDescription();
        response.bookCount = category.getBooks().size();
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getBookCount() { return bookCount; }
    public void setBookCount(int bookCount) { this.bookCount = bookCount; }
}
