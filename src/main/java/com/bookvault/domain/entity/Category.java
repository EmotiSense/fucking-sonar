package com.bookvault.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a book category (genre) in the library catalogue.
 */
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @OneToMany(mappedBy = "category")
    private List<Book> books = new ArrayList<>();

    /** Required by JPA. */
    protected Category() {
    }

    /**
     * Creates a category with a name and optional description.
     *
     * @param name        the category name; must not be null or blank
     * @param description an optional human-readable description
     */
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the category name.
     *
     * @return category name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the category name.
     *
     * @param name the new name; must not be null or blank
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the optional description.
     *
     * @return description, or {@code null} if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the optional description.
     *
     * @param description the description text
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns an unmodifiable view of the books in this category.
     *
     * @return immutable list of books
     */
    public List<Book> getBooks() {
        return Collections.unmodifiableList(books);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Category{id=" + getId() + ", name='" + name + "'}";
    }
}
