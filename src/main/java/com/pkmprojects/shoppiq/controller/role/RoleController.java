package com.pkmprojects.shoppiq.controller.role;

import com.pkmprojects.shoppiq.entity.role.Role;
import com.pkmprojects.shoppiq.service.role.RoleService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> REST controller for role management operations.
 *
 * <p>Provides endpoints to create, list, and delete application roles.
 * All endpoints require the {@code ADMIN} role. Roles define the
 * authorization hierarchy used by Spring Security's
 * {@code @PreAuthorize} annotations throughout the application.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Creates a new application role.
     *
     * @param roleName the role name (uppercase, underscore-separated)
     * @return 200 OK with the created role
     */
    @PostMapping("/create/{roleName}")
    public ResponseEntity<Role> createRole(
            @PathVariable
            @NotBlank(message = "Role name must not be blank")
            @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
            @Pattern(
                    regexp = "^[A-Z][A-Z0-9_]*$",
                    message = "Role name must start with an uppercase letter and contain only uppercase letters, digits, and underscores"
            )
            String roleName) {

        return ResponseEntity.ok(roleService.createNewRole(roleName));
    }

    /**
     * Returns all existing roles.
     *
     * @return 200 OK with list of all roles
     */
    @GetMapping("/all")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllExistingRoles());
    }
}
