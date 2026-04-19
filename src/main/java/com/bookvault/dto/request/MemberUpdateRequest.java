package com.bookvault.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Email(message = "Email must be a valid address")
        @Size(max = 200, message = "Email must not exceed 200 characters")
        String email,

        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address) {
}
