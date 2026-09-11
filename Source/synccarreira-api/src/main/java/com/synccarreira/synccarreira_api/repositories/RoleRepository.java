package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByAuthority(String authority);
}
