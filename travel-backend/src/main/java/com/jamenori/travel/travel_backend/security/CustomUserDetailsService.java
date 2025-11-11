package com.jamenori.travel.travel_backend.security;

import com.jamenori.travel.travel_backend.entity.User;
import com.jamenori.travel.travel_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * แปลง User ของเราไปเป็น UserDetails ให้ Spring Security ใช้
 *
 * ✅ คลาสนี้คือจุดที่ Spring Security จะเรียกใช้เมื่อจำเป็นต้องโหลดข้อมูลผู้ใช้
 * เช่นตอน verify JWT token หรือ login ผ่าน AuthenticationManager
 *
 * โดยเราจะดึงข้อมูลจากฐานข้อมูล (ผ่าน UserRepository)
 * แล้วแปลงให้อยู่ในรูปแบบของ `UserDetails` ที่ Spring เข้าใจได้
 */
@Service
@RequiredArgsConstructor // ✅ Lombok: สร้าง constructor อัตโนมัติสำหรับ field final ทั้งหมด
public class CustomUserDetailsService implements UserDetailsService {

    // ✅ Inject UserRepository เพื่อดึงข้อมูลผู้ใช้จากฐานข้อมูล
    private final UserRepository userRepository;

    /**
     * ✅ เมธอดหลักของ UserDetailsService
     * ใช้โหลดข้อมูลผู้ใช้ตาม "email" ซึ่งในระบบเราคือ username
     *
     * ถ้าไม่เจอ email ที่ระบุ จะโยน Exception เพื่อให้ Security หยุดกระบวนการตรวจสอบ
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // ✅ ค้นหาผู้ใช้จากฐานข้อมูล
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        // ✅ แปลงข้อมูล User (ของเราเอง) ไปเป็น UserDetails (ของ Spring)
        //   - email → username
        //   - passwordHash → password
        //   - ROLE_USER → สิทธิ์พื้นฐาน
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // ✅ ให้ทุก user มี role พื้นฐานเป็น ROLE_USER
        );
    }
}


