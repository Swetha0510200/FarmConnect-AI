package com.farmconnect.repository;

import com.farmconnect.entity.Role;
import com.farmconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);
    Optional<User> findByEmailOrMobile(String email, String mobile);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
    List<User> findByRole(Role role);
    long countByRole(Role role);
    List<User> findByRoleOrderByIdDesc(Role role);
}
