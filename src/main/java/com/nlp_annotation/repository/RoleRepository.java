package com.nlp_annotation.repository;


import com.nlp_annotation.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByNomRole(String nomRole);
}
