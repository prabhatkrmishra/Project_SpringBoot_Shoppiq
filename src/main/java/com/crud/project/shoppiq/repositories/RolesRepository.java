package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
}
