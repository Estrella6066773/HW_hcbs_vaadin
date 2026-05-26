package com.hcbs.repository;

import com.hcbs.model.User;
import com.hcbs.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findFirstByRole(UserRole role);

    List<User> findByRoleOrderByFullNameAsc(UserRole role);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    List<User> findByRoleAndPhoneStartsWithOrderByPhoneAsc(UserRole role, String phonePrefix);
}
