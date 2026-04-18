package com.bookvault.exception;

/**
 * Thrown when a requested resource does not exist in the data store.
 */
public class ResourceNotFoundException extends BookVaultException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    /**
     * Creates an exception indicating that a specific resource was not found.
     *
     * @param resourceType the type of resource (e.g., "Book", "Member")
     * @param identifier   the identifier used for the lookup
     */
    public ResourceNotFoundException(String resourceType, Object identifier) {
        super(resourceType + " not found with identifier: " + identifier, ERROR_CODE);
    }
}
