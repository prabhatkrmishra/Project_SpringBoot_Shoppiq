package com.pkmprojects.shoppiq.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating the current user's profile.
 *
 * <p>Only the display name is editable; email and username remain locked.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
}
