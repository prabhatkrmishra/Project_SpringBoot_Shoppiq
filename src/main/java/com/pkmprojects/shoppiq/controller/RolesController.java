package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.entity.Role;
import com.pkmprojects.shoppiq.service.RolesService;
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
public class RolesController {

    private final RolesService rolesService;

    public RolesController(RolesService rolesService) {
        this.rolesService = rolesService;
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

        return ResponseEntity.ok(rolesService.createNewRole(roleName));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Role>> getAllRole() {
        return ResponseEntity.ok(rolesService.getAllExistingRoles());
    }
}
