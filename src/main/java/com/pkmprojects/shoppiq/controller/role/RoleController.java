package com.pkmprojects.shoppiq.controller.role;

import com.pkmprojects.shoppiq.entity.role.Role;
import com.pkmprojects.shoppiq.service.role.RoleService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

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

    @GetMapping("/all")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllExistingRoles());
    }
}
