package com.bookvault.dto.response;

import com.bookvault.domain.entity.Category;

/**
 * Read-only view of a {@link Category} entity returned to API clients.
 */
public record CategoryResponse(Long id, String name, String description, long bookCount) {

    public static CategoryResponse from(Category category, long bookCount) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                bookCount);
    }
}
