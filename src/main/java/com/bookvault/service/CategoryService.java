package com.bookvault.service;

import com.bookvault.domain.entity.Category;
import com.bookvault.dto.request.CategoryRequest;
import com.bookvault.dto.response.CategoryResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.BookRepository;
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
    private final BookRepository bookRepository;

    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(cat -> CategoryResponse.from(cat, bookRepository.countByCategory(cat)))
                .toList();
    }

    public CategoryResponse findById(Long id) {
        Category category = loadById(id);
        return CategoryResponse.from(category, bookRepository.countByCategory(category));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        assertNameUnique(request.name());
        Category category = new Category(request.name(), request.description());
        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved, 0L);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = loadById(id);
        assertNameUniqueExcluding(request.name(), id);
        category.setName(request.name());
        category.setDescription(request.description());
        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved, bookRepository.countByCategory(saved));
    }

    @Transactional
    public void delete(Long id) {
        Category category = loadById(id);
        if (bookRepository.existsByCategory(category)) {
            throw new BusinessRuleException(
                    "Cannot delete category '" + category.getName()
                            + "' because it still contains books");
        }
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
}
