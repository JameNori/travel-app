package com.jamenori.travel.travel_backend.controller;

import com.jamenori.travel.travel_backend.dto.TripRequest;
import com.jamenori.travel.travel_backend.entity.Trip;
import com.jamenori.travel.travel_backend.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * TripController
 *
 * Controller ชั้นบนสุดที่ใช้รับ Request จาก Frontend
 * และส่งต่อให้ TripService จัดการ Business Logic
 *
 * โครงสร้างหลักของระบบ Trip:
 * - GET (Public)
 * - GET /mine (Protected)
 * - POST / PUT / DELETE (Protected + ตรวจ owner)
 *
 * ทุก error จะถูก GlobalExceptionHandler จัดรูปแบบให้อัตโนมัติ
 */
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // ─────────────────────────────────────────────
    // 1. GET /api/trips
    // ดึงทริปทั้งหมด หรือค้นหาตาม keyword
    // Public endpoint
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips(@RequestParam(required = false) String query) {
        List<Trip> trips = tripService.getAllTrips(query);
        return ResponseEntity.ok(trips);
    }

    // ─────────────────────────────────────────────
    // 2. GET /api/trips/{id}
    // ดึงทริปรายการเดียวตาม ID
    // Public endpoint
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable Long id) {
        Trip trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    // ─────────────────────────────────────────────
    // 3. GET /api/trips/mine
    // ดึงทริปทั้งหมดที่ user ปัจจุบันเป็นเจ้าของ
    // Protected (ต้องแนบ JWT)
    // ─────────────────────────────────────────────
    @GetMapping("/mine")
    public ResponseEntity<List<Trip>> getMyTrips() {
        List<Trip> myTrips = tripService.getMyTrips();
        return ResponseEntity.ok(myTrips);
    }

    // ─────────────────────────────────────────────
    // 4. POST /api/trips
    // เพิ่มทริปใหม่
    // Protected (ต้องแนบ JWT)
    // ใช้ DTO + Validation
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Trip> createTrip(@Valid @RequestBody TripRequest request) {
        Trip createdTrip = tripService.createTrip(request);
        return ResponseEntity.ok(createdTrip);
    }

    // ─────────────────────────────────────────────
    // 5. PUT /api/trips/{id}
    // อัปเดตทริป (เฉพาะเจ้าของเท่านั้น)
    // Protected + ตรวจ ownership
    // ใช้ DTO + Validation
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripRequest request
    ) {
        Trip updatedTrip = tripService.updateTrip(id, request);
        return ResponseEntity.ok(updatedTrip);
    }

    // ─────────────────────────────────────────────
    // 6. DELETE /api/trips/{id}
    // ลบทริป (เฉพาะเจ้าของเท่านั้น)
    // Protected + ตรวจ ownership
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }
}
