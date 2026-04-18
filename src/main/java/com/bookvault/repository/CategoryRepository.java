package com.bookvault.repository;

import com.bookvault.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Category} entities.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its exact name (case-insensitive).
     *
     * @param name the category name
     * @return an optional containing the category, if found
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Checks whether a category with the given name already exists.
     *
     * @param name the name to check
     * @return {@code true} if a record exists
     */
    boolean existsByNameIgnoreCase(String name);
}
