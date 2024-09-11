package com.medilocate.repository;

import com.medilocate.entity.User;
import com.medilocate.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndRole(String email, Role role);

}
