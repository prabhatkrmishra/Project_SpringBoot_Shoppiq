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
 * Request DTO for user registration, supporting both customer and
 * combined user+seller registration.
 *
 * <p>This record handles two registration flows through a single DTO:
 * simple user registration (when only the basic fields are provided)
 * and combined user+seller registration (when business fields are also
 * included). The service layer detects the registration type using the
 * {@link #isSellerRegistration()} method and creates the appropriate
 * account type.</p>
 *
 * <p>Bean Validation constraints mirror those used in other registration
 * flows (e.g. OAuth completion) to ensure consistent input validation.
 * This DTO uses Lombok rather than a Java record because it contains
 * a behavioral method that inspects and derives from the data, which
 * is more natural in a Lombok POJO than in an immutable record.</p>
 *
 * @param name          user's full display name, required
 * @param email         email address, required, must be valid format
 * @param username      unique username, required, 3-30 characters,
 *                      alphanumeric with underscores only
 * @param password      account password, required, at least 8 characters
 *                      with uppercase, lowercase, digit, and special character
 * @param businessName  business name for seller registration, optional;
 *                      when provided with other seller fields, triggers
 *                      combined user+seller registration
 * @param businessEmail business email for seller registration, optional;
 *                      must be valid format if provided
 * @param phone         business phone for seller registration, optional
 * @param gstNumber     GST identification number for seller registration,
 *                      optional
 * @param panNumber     PAN number for seller registration, optional;
 *                      exactly 10 characters when provided
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserRequest {

    /**
     * User's full name. Must not be blank.
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * Email address. Must be valid.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /**
     * Username. Must be 3-30 characters, alphanumeric with underscores.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    /**
     * Password. Must be at least 8 characters with uppercase, lowercase, number, and special character.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\p{N})(?=.*[@$!%*?&]).+$",
            message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String password;

    /**
     * Business name for seller registration. Optional.
     */
    private String businessName;

    /**
     * Business email for seller registration. Optional — must be valid if provided.
     */
    @Email(message = "Business email must be valid")
    private String businessEmail;

    /**
     * Business phone for seller registration. Optional.
     */
    private String phone;

    /**
     * GST identification number for seller registration. Optional.
     */
    private String gstNumber;

    /**
     * PAN number for seller registration. Optional — must be exactly 10 characters.
     */
    @Size(min = 10, max = 10, message = "PAN number must be exactly 10 characters")
    private String panNumber;

    /**
     * Checks if this request includes seller registration fields.
     *
     * @return true if businessName, businessEmail, phone, and panNumber are all provided
     */
    public boolean isSellerRegistration() {
        return businessName != null && !businessName.isBlank()
                && businessEmail != null && !businessEmail.isBlank()
                && phone != null && !phone.isBlank()
                && panNumber != null && !panNumber.isBlank();
    }
}
