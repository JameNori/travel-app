package com.jamenori.travel.travel_backend.service;

import com.jamenori.travel.travel_backend.entity.Trip;
import com.jamenori.travel.travel_backend.entity.User;
import com.jamenori.travel.travel_backend.repository.TripRepository;
import com.jamenori.travel.travel_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * ✅ TripService
 * ชั้น Service สำหรับจัดการ Business Logic ของระบบ Trip
 *
 * 📦 หน้าที่หลัก:
 * - เพิ่มทริปใหม่ (Create)
 * - ดึงทริปทั้งหมด หรือค้นหาด้วย keyword (Read/Search)
 * - ดึงทริปรายการเดียว (Read One)
 * - ดึงเฉพาะทริปของผู้ใช้ปัจจุบัน (My Trips)
 * - แก้ไขทริป (Update)
 * - ลบทริป (Delete)
 *
 * 💡 ใช้ร่วมกับ GlobalExceptionHandler เพื่อให้ error format เหมือนระบบ Auth
 */
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    /**
     * ✅ Create Trip — เพิ่มทริปใหม่
     * ดึง email จาก JWT → หา User → บันทึก Trip ลงฐานข้อมูล
     */
    public Trip createTrip(Trip tripRequest) {
        User currentUser = getCurrentUser();

        Trip trip = Trip.builder()
                .title(tripRequest.getTitle())
                .description(tripRequest.getDescription())
                .photos(tripRequest.getPhotos())
                .tags(tripRequest.getTags())
                .latitude(tripRequest.getLatitude())
                .longitude(tripRequest.getLongitude())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .author(currentUser)
                .build();

        return tripRepository.save(trip);
    }

    /**
     * ✅ Get All Trips — ดึงทริปทั้งหมด หรือค้นหาด้วย keyword
     * ถ้ามี query → ค้นหา
     * ถ้าไม่มี → คืนทั้งหมด
     */
    public List<Trip> getAllTrips(String query) {
        if (query != null && !query.isBlank()) {
            return tripRepository.searchTrips(query);
        }
        return tripRepository.findAll();
    }

    /**
     * ✅ Get Trip by ID — ดึงทริปเดียว (Public)
     */
    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
    }

    /**
     * ✅ Get My Trips — ดึงทริปของผู้ใช้ที่ล็อกอินอยู่ (Protected)
     */
    public List<Trip> getMyTrips() {
        User currentUser = getCurrentUser();
        return tripRepository.findByAuthor(currentUser);
    }

    /**
     * ✅ Update Trip — แก้ไขทริป (เฉพาะเจ้าของเท่านั้น)
     * - ตรวจสอบว่าเป็นเจ้าของหรือไม่
     * - อัปเดตเฉพาะ field ที่เปลี่ยน
     */
    public Trip updateTrip(Long id, Trip updatedTrip) {
        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        // 🔒 ตรวจสอบสิทธิ์เจ้าของทริป
        if (!trip.getAuthor().getId().equals(currentUser.getId())) {
            throw new SecurityException("You do not have permission to edit this trip");
        }

        trip.setTitle(updatedTrip.getTitle());
        trip.setDescription(updatedTrip.getDescription());
        trip.setPhotos(updatedTrip.getPhotos());
        trip.setTags(updatedTrip.getTags());
        trip.setLatitude(updatedTrip.getLatitude());
        trip.setLongitude(updatedTrip.getLongitude());
        trip.setUpdatedAt(Instant.now());

        return tripRepository.save(trip);
    }

    /**
     * ✅ Delete Trip — ลบทริป (เฉพาะเจ้าของเท่านั้น)
     */
    public void deleteTrip(Long id) {
        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        // 🔒 ตรวจสอบสิทธิ์เจ้าของทริป
        if (!trip.getAuthor().getId().equals(currentUser.getId())) {
            throw new SecurityException("You do not have permission to delete this trip");
        }

        tripRepository.delete(trip);
    }

    /**
     * 🧩 Utility: ดึง User ปัจจุบันจาก JWT Token ที่ decode แล้ว
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
