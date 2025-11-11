package com.jamenori.travel.travel_backend.config;

import com.jamenori.travel.travel_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * ✅ คลาสนี้เป็นส่วนของการตั้งค่า Spring Security หลักของระบบ
 * โดยจะกำหนดกฎการเข้าถึง API, การใช้ JWT, และ Session Policy ทั้งหมด
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // ✅ ให้ Controller ใช้ SecurityContext ได้ (เช่น @PreAuthorize)
@RequiredArgsConstructor
public class SecurityConfig {

    // ✅ ตัวกรอง JWT ที่เราสร้างเอง (ใช้ตรวจ token ทุกครั้งก่อนเข้าถึง API)
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * ✅ กำหนด Security Filter Chain หลักของระบบ
     * คือโครงสร้างลำดับการตรวจสอบ request ก่อนถึง Controller จริง
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("[DEBUG] SecurityConfig is loaded ✅");
        System.out.println("[DEBUG] Registering JwtAuthenticationFilter into Security Chain...");

        http
            // ✅ ปิดการใช้ CSRF (Cross Site Request Forgery)
            // เพราะเราใช้ JWT แบบ stateless อยู่แล้ว ไม่ใช้ session cookies
            .csrf(csrf -> csrf.disable())

            // ✅ ตั้งค่าให้ระบบไม่สร้าง session ฝั่ง server
            // เพราะ JWT จะถูกตรวจสอบทุกครั้ง ไม่ต้องเก็บ session state
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ✅ ตั้งค่า rule ของการเข้าถึง API (Authorization Rules)
            .authorizeHttpRequests(auth -> auth
                // ✅ ปล่อยให้ endpoint สำหรับ Register และ Login ใช้ได้โดยไม่ต้องมี token
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                // ✅ อนุญาตให้ OPTIONS method ผ่านได้ (จำเป็นสำหรับ CORS preflight request จาก frontend)
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                // 🔒 ส่วนอื่น ๆ ของระบบ ต้องแนบ JWT ที่ถูกต้องถึงจะเข้าได้
                .anyRequest().authenticated()
            )

            // ✅ ใส่ตัวกรอง JwtAuthenticationFilter ก่อน UsernamePasswordAuthenticationFilter
            // เพื่อให้ token ถูกตรวจสอบก่อนเข้าสู่การยืนยันตัวตนของ Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // ✅ สุดท้าย สร้างและคืนค่า SecurityFilterChain ให้ Spring ใช้งาน
        return http.build();
    }

    /**
     * ✅ PasswordEncoder ใช้เข้ารหัส password ก่อนบันทึกลง database
     * BCrypt เป็น algorithm ที่นิยมและปลอดภัย
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ✅ AuthenticationManager คือ class กลางที่ใช้ในการตรวจสอบ username/password
     * โดยจะถูกเรียกใช้ใน AuthService ตอน login
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ✅ ปรับ Firewall ของ Spring Security ให้อนุญาตบางอักขระพิเศษใน URL ได้
     * เช่น // , ; , \ (ใช้ในบาง endpoint หรือ URL ที่มี encoding แปลก ๆ)
     */
    @Bean
    public HttpFirewall allowUrlEncodedHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowSemicolon(true);
        firewall.setAllowBackSlash(true);
        return firewall;
    }
}
