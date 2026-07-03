package com.pkmprojects.shoppiq.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for username/password registration.
 *
 * <p>
 * Bean Validation constraints mirror those used elsewhere in the
 * registration flow (see {@code CompleteGoogleRegistrationRequest})
 * so that malformed requests are rejected by {@code GlobalExceptionHandler}
 * as a standard RFC 9457 validation response, rather than surfacing later
 * as an opaque database constraint violation.
 * </p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String businessName;

    @Email(message = "Business email must be valid")
    private String businessEmail;

    private String phone;

    private String gstNumber;

    @Size(min = 10, max = 10, message = "PAN number must be exactly 10 characters")
    private String panNumber;

    public boolean isSellerRegistration() {
        return businessName != null && !businessName.isBlank()
                && businessEmail != null && !businessEmail.isBlank()
                && phone != null && !phone.isBlank()
                && panNumber != null && !panNumber.isBlank();
    }
}
