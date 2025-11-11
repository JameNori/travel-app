package com.jamenori.travel.travel_backend.controller;

import com.jamenori.travel.travel_backend.dto.AuthResponse;
import com.jamenori.travel.travel_backend.dto.LoginRequest;
import com.jamenori.travel.travel_backend.dto.RegisterRequest;
import com.jamenori.travel.travel_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * ✅ Auth API: Register, Login, Logout, Me
 * Controller นี้รับผิดชอบการจัดการ Authentication ทั้งหมด
 * ได้แก่ การสมัครสมาชิก, เข้าสู่ระบบ, ออกจากระบบ และตรวจสอบผู้ใช้ปัจจุบัน
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // ✅ อนุญาตให้ frontend ทุกโดเมนเรียกใช้งานได้
public class AuthController {

    // ✅ เชื่อมต่อกับ AuthService เพื่อให้ Controller ไม่ต้องจัดการ logic เอง
    private final AuthService authService;

    /**
     * ✅ Register — สมัครสมาชิกใหม่
     * รับข้อมูลจาก Frontend ผ่าน DTO RegisterRequest
     * ส่งต่อไปให้ AuthService ประมวลผลและบันทึกข้อมูลผู้ใช้ใหม่
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // เรียกใช้ service เพื่อสร้าง user ใหม่และ generate token
        AuthResponse response = authService.register(request);
        // ส่งกลับ token และข้อมูลผู้ใช้ในรูปแบบ JSON
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Login — เข้าสู่ระบบ
     * รับ email/password จาก Frontend ผ่าน LoginRequest DTO
     * จากนั้นให้ AuthService ตรวจสอบข้อมูลและออก JWT token ใหม่
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // เรียก service เพื่อ authenticate และคืน token
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Logout — ออกจากระบบ
     * เนื่องจากระบบนี้ใช้ JWT แบบ stateless
     * ฝั่ง backend จึงไม่ต้องลบ session ใด ๆ (frontend เป็นคนลบ token เอง)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout(); // ทำงานเปล่า (placeholder)
        return ResponseEntity.ok().build();
    }

    /**
     * ✅ Me — ตรวจสอบผู้ใช้ปัจจุบันจาก JWT
     * ใช้ Authentication object จาก SecurityContext (ที่ filter ตั้งไว้)
     * เพื่อตรวจว่า token ถูกต้องและดึง email ผู้ใช้ที่ล็อกอินอยู่
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        // 🔸 ถ้าไม่มีการ authenticate แปลว่า token ผิดหรือหมดอายุ
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("[DEBUG] ❌ Authentication is null or not authenticated");
            return ResponseEntity.status(403).body("Forbidden: Unauthorized user");
        }

        // ✅ ดึง email จาก SecurityContext (JWT filter เป็นคน set ค่าไว้)
        String email = authentication.getName();
        System.out.println("[DEBUG] ✅ Authenticated user email: " + email);

        // ✅ ส่ง email กลับให้ frontend ทราบว่าเป็นใคร
        return ResponseEntity.ok(java.util.Map.of("email", email));
    }
}
