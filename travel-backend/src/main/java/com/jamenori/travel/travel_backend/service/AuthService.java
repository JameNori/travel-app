package com.jamenori.travel.travel_backend.service;

import com.jamenori.travel.travel_backend.dto.AuthResponse;
import com.jamenori.travel.travel_backend.dto.ChangePasswordRequest;
import com.jamenori.travel.travel_backend.dto.LoginRequest;
import com.jamenori.travel.travel_backend.dto.RegisterRequest;
import com.jamenori.travel.travel_backend.entity.User;
import com.jamenori.travel.travel_backend.repository.UserRepository;
import com.jamenori.travel.travel_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * ✅ AuthService
 * ชั้น Service สำหรับจัดการ logic ทั้งหมดของระบบ Authentication
 * (สมัครสมาชิก, เข้าสู่ระบบ, ออกจากระบบ)
 *
 * 🔹 การทำงานของ AuthService:
 * 1. Register → ตรวจสอบ email ซ้ำ, เข้ารหัสรหัสผ่าน, สร้าง user ใหม่, คืน token
 * 2. Login → ตรวจสอบ email/password, สร้าง token ใหม่
 * 3. Logout → ให้ frontend ลบ token ทิ้ง (เพราะ JWT เป็น stateless)
 */
@Service
@RequiredArgsConstructor // ✅ ให้ Lombok สร้าง constructor อัตโนมัติสำหรับ field ที่เป็น final
public class AuthService {

    // ✅ Repository สำหรับจัดการข้อมูลผู้ใช้ในฐานข้อมูล
    private final UserRepository userRepository;

    // ✅ ใช้เข้ารหัสรหัสผ่าน (BCrypt)
    private final PasswordEncoder passwordEncoder;

    // ✅ ใช้สร้างและตรวจสอบ JWT token
    private final JwtService jwtService;

    // ✅ ใช้ตรวจสอบความถูกต้องของ email/password ตอน login
    private final AuthenticationManager authenticationManager;

    /**
     * ✅ Register — สมัครสมาชิกใหม่
     * 1. ตรวจสอบว่า email ซ้ำไหม
     * 2. เข้ารหัสรหัสผ่าน
     * 3. บันทึก user ลงฐานข้อมูล
     * 4. สร้าง JWT token เพื่อให้ล็อกอินอัตโนมัติหลังสมัครเสร็จ
     */
    public AuthResponse register(RegisterRequest request) {
        // 🔸 เช็คว่า email ถูกใช้ไปแล้วหรือยัง
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            // ✅ หาก email ซ้ำ จะโยน IllegalArgumentException
            // ซึ่ง GlobalExceptionHandler จะจับและส่ง HTTP 400 ให้ frontend
            throw new IllegalArgumentException("Email is already taken");
        }

        // ✅ เข้ารหัสรหัสผ่านก่อนบันทึก (ห้ามเก็บ plain text)
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .createdAt(Instant.now())
                .build();

        // ✅ บันทึกผู้ใช้ใหม่ลงฐานข้อมูล
        User saved = userRepository.save(user);

        // ✅ สร้าง JWT token ใหม่สำหรับผู้ใช้
        String token = jwtService.generateTokenFromEmail(saved.getEmail());

        // ✅ ส่งกลับข้อมูลผู้ใช้ + token ไปยัง frontend
        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .email(saved.getEmail())
                .displayName(saved.getDisplayName())
                .build();
    }

    /**
     * ✅ Login — เข้าสู่ระบบ
     * 1. ใช้ AuthenticationManager ตรวจสอบ email/password
     * 2. ดึงข้อมูลผู้ใช้จากฐานข้อมูล
     * 3. สร้าง token ใหม่และส่งกลับ
     */
    public AuthResponse login(LoginRequest request) {
        // ✅ ให้ Spring Security ตรวจสอบความถูกต้องของ email/password อัตโนมัติ
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            // ❌ หากรหัสผ่านไม่ตรง จะโยน exception ไปยัง GlobalExceptionHandler
            throw new IllegalArgumentException("Invalid email or password");
        }

        // ✅ ดึงข้อมูลผู้ใช้จากฐานข้อมูล (เชื่อว่า email ถูกต้องแน่นอนแล้ว)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // ✅ สร้าง JWT token ใหม่
        String token = jwtService.generateTokenFromEmail(user.getEmail());

        // ✅ ส่งกลับข้อมูลผู้ใช้ + token
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
    }

    /**
     * ✅ Logout — ออกจากระบบ
     * เพราะระบบนี้ใช้ JWT แบบ Stateless (ไม่มี session)
     * ฝั่ง Backend ไม่ต้องทำอะไรเพิ่มเติม
     * ให้ Frontend ลบ token ที่เก็บไว้ใน localStorage ก็เพียงพอ
     */
    public void logout() {
        // JWT เป็น stateless → ฝั่ง server ไม่ต้องทำอะไรเป็นพิเศษ
        // ให้ frontend ทำการลบ token ทิ้งก็พอ
    }

    /**
     * ✅ Change Password — เปลี่ยนรหัสผ่าน (ผู้ใช้ต้อง login แล้ว)
     * 1. ตรวจสอบรหัสปัจจุบันกับ passwordHash ใน DB
     * 2. ตรวจความยาวรหัสใหม่ (อย่างน้อย 8 ตัว)
     * 3. เข้ารหัสรหัสใหม่และบันทึก
     */
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("รหัสผ่านปัจจุบันไม่ถูกต้อง");
        }

        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("รหัสผ่านใหม่ต้องมีอย่างน้อย 8 ตัวอักษร");
        }
        if (newPassword.length() > 100) {
            throw new IllegalArgumentException("รหัสผ่านใหม่ต้องไม่เกิน 100 ตัวอักษร");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
