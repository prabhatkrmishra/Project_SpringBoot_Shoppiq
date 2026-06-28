package com.pkmprojects.shoppiq.config;

import com.pkmprojects.shoppiq.entity.Role;
import com.pkmprojects.shoppiq.repository.RolesRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Runs at application startup via {@link CommandLineRunner}.
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private final RolesRepository rolesRepository;

    public DataInitializer(RolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    /**
     * Checks for the existence of the required roles and creates them
     * if missing. This guarantees the security configuration can
     * assign proper roles after startup.
     * <p>
     * Create ROLE_ADMIN
     * Create ROLE_CUSTOMER
     *
     * @param args command line arguments (unused)
     */
    @Override
    public void run(String... args) {
        if (rolesRepository.findByRoleName("ROLE_ADMIN").isEmpty()) {
            Role role = new Role();
            role.setRoleName("ROLE_ADMIN");
            rolesRepository.save(role);
        }

        if (rolesRepository.findByRoleName("ROLE_CUSTOMER").isEmpty()) {
            Role role = new Role();
            role.setRoleName("ROLE_CUSTOMER");
            rolesRepository.save(role);
        }
    }
}
