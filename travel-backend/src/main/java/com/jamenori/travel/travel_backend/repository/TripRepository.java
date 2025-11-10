package com.jamenori.travel.travel_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jamenori.travel.travel_backend.entity.Trip;
import com.jamenori.travel.travel_backend.entity.User;
import java.util.List;

/**
 * Repository สำหรับจัดการข้อมูลทริป (trips)
 */
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByAuthor(User author);
}

