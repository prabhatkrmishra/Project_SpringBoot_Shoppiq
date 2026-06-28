package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
}
