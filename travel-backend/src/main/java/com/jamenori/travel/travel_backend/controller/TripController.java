package com.jamenori.travel.travel_backend.controller;

import com.jamenori.travel.travel_backend.entity.Trip;
import com.jamenori.travel.travel_backend.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ TripController
 * Controller สำหรับจัดการ Endpoint ของระบบทริป (Trip)
 *
 * 📦 หน้าที่หลัก:
 * - รับ request จากฝั่ง frontend (Vue.js)
 * - เรียกใช้ Service ชั้น TripService เพื่อประมวลผล
 * - ส่ง Response กลับไปยัง client
 *
 * 💡 ทุก endpoint ในนี้จะใช้ร่วมกับ GlobalExceptionHandler
 * เพื่อให้ error response มี format เดียวกับ Auth system
 */
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // ─────────────────────────────────────────────
    // 🔹 1. GET /api/trips — ดึงทั้งหมด หรือค้นหา
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips(@RequestParam(required = false) String query) {
        List<Trip> trips = tripService.getAllTrips(query);
        return ResponseEntity.ok(trips);
    }

    // ─────────────────────────────────────────────
    // 🔹 2. GET /api/trips/{id} — ดึงทริปรายการเดียว
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable Long id) {
        Trip trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    // ─────────────────────────────────────────────
    // 🔹 3. GET /api/trips/mine — ดึงทริปของผู้ใช้ที่ล็อกอิน
    // 🔒 Protected (ต้องแนบ JWT)
    // ─────────────────────────────────────────────
    @GetMapping("/mine")
    public ResponseEntity<List<Trip>> getMyTrips() {
        List<Trip> myTrips = tripService.getMyTrips();
        return ResponseEntity.ok(myTrips);
    }

    // ─────────────────────────────────────────────
    // 🔹 4. POST /api/trips — เพิ่มทริปใหม่
    // 🔒 Protected (ต้องแนบ JWT)
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        Trip createdTrip = tripService.createTrip(trip);
        return ResponseEntity.ok(createdTrip);
    }

    // ─────────────────────────────────────────────
    // 🔹 5. PUT /api/trips/{id} — แก้ไขทริป
    // 🔒 Protected + ตรวจ ownership
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable Long id, @RequestBody Trip trip) {
        Trip updatedTrip = tripService.updateTrip(id, trip);
        return ResponseEntity.ok(updatedTrip);
    }

    // ─────────────────────────────────────────────
    // 🔹 6. DELETE /api/trips/{id} — ลบทริป
    // 🔒 Protected + ตรวจ ownership
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }
}
