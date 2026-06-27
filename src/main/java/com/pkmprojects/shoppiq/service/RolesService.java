package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.entity.Role;
import com.pkmprojects.shoppiq.repository.RolesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolesService {
    private final RolesRepository rolesRepository;

    public RolesService(RolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    public Role createNewRole(String roleName) {
        try {
            Role newRole = new Role();
            String finalRole = "ROLE_" + roleName.toUpperCase();
            newRole.setRoleName(finalRole);

            return rolesRepository.save(newRole);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Role> getAllExistingRoles() {
        try {
            return rolesRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Role getCustomerRole() {
        return rolesRepository.findByRoleName("ROLE_CUSTOMER").orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));
    }
}
