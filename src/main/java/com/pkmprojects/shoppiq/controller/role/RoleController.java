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
 * REST controller for role management operations.
 *
 * <p>Provides endpoints to create and list application roles. Roles define the
 * authorization hierarchy used by Spring Security's @PreAuthorize annotations
 * throughout the application. Role names must follow a naming convention:
 * uppercase letters, digits, and underscores only, starting with a letter.</p>
 *
 * <p>This controller acts as the HTTP boundary for role administration. It
 * delegates all business logic — role creation, name validation, and listing —
 * to {@link RoleService}. The controller handles no business logic beyond
 * path variable validation.</p>
 *
 * <p>All endpoints require ADMIN role and are mounted under /roles.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /roles/create/{roleName}  — create a new application role
 * GET    /roles/all                — list all existing roles
 * </pre>
 *
 * @author prabhatkrmishra
 * @see RoleService
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
     * <p>The role name must be uppercase letters, digits, and underscores
     * only, starting with a letter. Names must be between 2 and 50 characters.</p>
     *
     * @param roleName the role name (uppercase, underscore-separated)
     * @return 200 OK with the created role response
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
     * @return 200 OK with list of all role responses
     */
    @GetMapping("/all")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllExistingRoles());
    }
}
