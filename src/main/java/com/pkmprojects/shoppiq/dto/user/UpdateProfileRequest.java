package com.pkmprojects.shoppiq.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating the current user's display name.
 *
 * <p>This record is submitted to the profile update endpoint when an
 * authenticated user wants to change their display name. Only the
 * {@code name} field is editable through this endpoint; email and
 * username remain locked after registration to maintain authentication
 * integrity.</p>
 *
 * <p>The single-field design keeps the update endpoint minimal and
 * focused. The {@code @NotBlank} and {@code @Size} annotations ensure
 * the new name is non-empty and within the allowed length before
 * reaching the service layer.</p>
 *
 * @param name new display name for the user, required, max 100 characters;
 *             used in storefront displays and order receipts
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateProfileRequest {

    /**
     * User's display name. Must not be blank. Max 100 characters.
     */
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
}
