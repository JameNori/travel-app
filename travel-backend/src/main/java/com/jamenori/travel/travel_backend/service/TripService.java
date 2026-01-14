package com.jamenori.travel.travel_backend.service;

import com.jamenori.travel.travel_backend.dto.TripRequest;
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
 * TripService
 *
 * ชั้น Service สำหรับจัดการ Business Logic ของ Trip ทั้งหมดในระบบ
 * ใช้ร่วมกับ SecurityConfig + GlobalExceptionHandler
 *
 * ใช้ TripRequest DTO แทนการ bind Entity ตรง ๆ
 */
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    /**
     * Create Trip — เพิ่มทริปใหม่ให้กับผู้ใช้ที่กำลังล็อกอินอยู่
     */
    public Trip createTrip(TripRequest request) {
        User currentUser = getCurrentUser();

        Trip trip = Trip.builder()
                .title(request.title())
                .description(request.description())
                // TripRequest = List<String>, Entity = String[]
                .photos(request.photos() != null ? request.photos().toArray(new String[0]) : null)
                .tags(request.tags() != null ? request.tags().toArray(new String[0]) : null)
                .url(request.url())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .author(currentUser)
                .build();

        return tripRepository.save(trip);
    }

    /**
     * ดึงทริปทั้งหมด หรือค้นหาด้วย keyword
     */
    public List<Trip> getAllTrips(String query) {
        if (query != null && !query.isBlank()) {
            return tripRepository.searchTrips(query);
        }
        return tripRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * ดึงทริปตาม ID (public)
     */
    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
    }

    /**
     * ดึงทริปของผู้ใช้ที่ล็อกอินอยู่ (ต้องใช้ JWT)
     */
    public List<Trip> getMyTrips() {
        User currentUser = getCurrentUser();
        return tripRepository.findByAuthorOrderByCreatedAtDesc(currentUser);
    }

    /**
     * Update Trip — อนุญาตเฉพาะเจ้าของทริปเท่านั้น
     */
    public Trip updateTrip(Long id, TripRequest request) {
        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        // ตรวจสิทธิ์เจ้าของ
        if (!trip.getAuthor().getId().equals(currentUser.getId())) {
            throw new SecurityException("You do not have permission to edit this trip");
        }

        trip.setTitle(request.title());
        trip.setDescription(request.description());
        trip.setPhotos(request.photos() != null ? request.photos().toArray(new String[0]) : null);
        trip.setTags(request.tags() != null ? request.tags().toArray(new String[0]) : null);
        trip.setUrl(request.url());
        trip.setLatitude(request.latitude());
        trip.setLongitude(request.longitude());
        trip.setUpdatedAt(Instant.now());

        return tripRepository.save(trip);
    }

    /**
     * Delete Trip — ต้องเป็นเจ้าของเท่านั้น
     */
    public void deleteTrip(Long id) {
        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        if (!trip.getAuthor().getId().equals(currentUser.getId())) {
            throw new SecurityException("You do not have permission to delete this trip");
        }

        tripRepository.delete(trip);
    }

    /**
     * ดึง User จาก JWT ที่ decode แล้วแบบปลอดภัย
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new SecurityException("Unauthorized access");
        }

        String email = auth.getName();
        if ("anonymousUser".equals(email)) {
            throw new SecurityException("Unauthorized access");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
