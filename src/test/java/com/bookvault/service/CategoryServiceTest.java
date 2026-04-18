package com.bookvault.service;

import com.bookvault.domain.entity.Category;
import com.bookvault.dto.request.CategoryRequest;
import com.bookvault.dto.response.CategoryResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link CategoryService}.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category fictionCategory;
    private CategoryRequest request;

    @BeforeEach
    void setUp() {
        fictionCategory = new Category("Fiction", "Fictional works");
        request = new CategoryRequest();
        request.setName("Science");
        request.setDescription("Science books");
    }

    @Test
    void findAll_shouldReturnAllCategories() {
        given(categoryRepository.findAll()).willReturn(List.of(fictionCategory));
        List<CategoryResponse> result = categoryService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fiction");
    }

    @Test
    void findById_existingId_shouldReturnCategory() {
        given(categoryRepository.findById(1L)).willReturn(Optional.of(fictionCategory));
        CategoryResponse result = categoryService.findById(1L);
        assertThat(result.getName()).isEqualTo("Fiction");
    }

    @Test
    void findById_nonExistingId_shouldThrowNotFound() {
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_newName_shouldSaveAndReturn() {
        given(categoryRepository.existsByNameIgnoreCase("Science")).willReturn(false);
        given(categoryRepository.save(any(Category.class))).willReturn(fictionCategory);
        CategoryResponse result = categoryService.create(request);
        verify(categoryRepository).save(any(Category.class));
        assertThat(result).isNotNull();
    }

    @Test
    void create_duplicateName_shouldThrowBusinessRule() {
        given(categoryRepository.existsByNameIgnoreCase("Science")).willReturn(true);
        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Science");
    }

    @Test
    void delete_missingCategory_shouldThrowNotFound() {
        given(categoryRepository.findById(2L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.delete(2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_emptyCategory_shouldDelete() {
        given(categoryRepository.findById(1L)).willReturn(Optional.of(fictionCategory));
        categoryService.delete(1L);
        verify(categoryRepository).delete(fictionCategory);
    }
}
