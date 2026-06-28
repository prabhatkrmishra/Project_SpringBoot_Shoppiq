package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.entity.Role;
import com.pkmprojects.shoppiq.service.RolesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolesController {

    private final RolesService rolesService;

    public RolesController(RolesService rolesService) {
        this.rolesService = rolesService;
    }

    @PostMapping("/create/{roleName}")
    public ResponseEntity<Role> createRole(@PathVariable String roleName) {
        return ResponseEntity.ok(rolesService.createNewRole(roleName));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Role>> getAllRole() {
        return ResponseEntity.ok(rolesService.getAllExistingRoles());
    }
}
