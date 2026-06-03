package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepository extends JpaRepository<Role, Long> {

}
