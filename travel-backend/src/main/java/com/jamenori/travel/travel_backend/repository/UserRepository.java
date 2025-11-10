package com.jamenori.travel.travel_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jamenori.travel.travel_backend.entity.User;
import java.util.Optional;

/**
 * Repository สำหรับจัดการข้อมูลผู้ใช้ (users)
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

