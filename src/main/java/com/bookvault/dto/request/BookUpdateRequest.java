package com.bookvault.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record BookUpdateRequest(
        @Size(max = 300, message = "Title must not exceed 300 characters")
        String title,

        @Size(max = 200, message = "Author must not exceed 200 characters")
        String author,

        @Size(max = 200, message = "Publisher must not exceed 200 characters")
        String publisher,

        Integer publicationYear,

        @Min(value = 1, message = "Total copies must be at least 1")
        Integer totalCopies,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        Long categoryId) {
}
