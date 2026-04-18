package com.bookvault.service;

import com.bookvault.domain.entity.Category;
import com.bookvault.dto.request.CategoryRequest;
import com.bookvault.dto.response.CategoryResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for managing library book categories.
 */
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private static final String ENTITY_NAME = "Category";

    private final CategoryRepository categoryRepository;

    /**
     * Constructs the service with its required repository dependency.
     *
     * @param categoryRepository the category data store
     */
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Returns all categories sorted by name.
     *
     * @return list of category responses
     */
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    /**
     * Returns a single category by its ID.
     *
     * @param id the category ID
     * @return the category response
     * @throws ResourceNotFoundException if no category exists with the given ID
     */
    public CategoryResponse findById(Long id) {
        Category category = loadById(id);
        return CategoryResponse.from(category);
    }

    /**
     * Creates a new category.
     *
     * @param request the creation request
     * @return the created category response
     * @throws BusinessRuleException if a category with the same name already exists
     */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        assertNameUnique(request.getName());
        Category category = new Category(request.getName(), request.getDescription());
        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved);
    }

    /**
     * Updates an existing category.
     *
     * @param id      the category ID
     * @param request the update request
     * @return the updated category response
     * @throws ResourceNotFoundException if no category exists with the given ID
     * @throws BusinessRuleException     if the new name conflicts with another category
     */
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = loadById(id);
        assertNameUniqueExcluding(request.getName(), id);
        applyUpdates(category, request);
        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved);
    }

    /**
     * Deletes a category by its ID.
     *
     * @param id the category ID
     * @throws ResourceNotFoundException if no category exists with the given ID
     * @throws BusinessRuleException     if the category still has books assigned to it
     */
    @Transactional
    public void delete(Long id) {
        Category category = loadById(id);
        assertCategoryEmpty(category);
        categoryRepository.delete(category);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Category loadById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, id));
    }

    private void assertNameUnique(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleException(
                    "A category with the name '" + name + "' already exists");
        }
    }

    private void assertNameUniqueExcluding(String name, Long excludedId) {
        categoryRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(excludedId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException(
                            "A category with the name '" + name + "' already exists");
                });
    }

    private void assertCategoryEmpty(Category category) {
        if (!category.getBooks().isEmpty()) {
            throw new BusinessRuleException(
                    "Cannot delete category '" + category.getName()
                            + "' because it still contains books");
        }
    }

    private void applyUpdates(Category category, CategoryRequest request) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
    }
}
