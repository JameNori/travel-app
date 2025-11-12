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
 * ✅ SecurityConfig
 * คลาสนี้เป็นส่วนของการตั้งค่า Spring Security หลักของระบบ
 *
 * 🔹 หน้าที่:
 *  - กำหนดการเข้าถึง API (Public / Protected)
 *  - ตั้งค่า JWT Filter
 *  - ปิด Session state (ใช้ JWT แบบ stateless)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // ✅ อนุญาตให้ใช้ annotation @PreAuthorize ได้ใน Controller
@RequiredArgsConstructor
public class SecurityConfig {

    // ✅ ตัวกรอง JWT ที่เราสร้างเอง (ใช้ตรวจ token ก่อนเข้าถึง API ทุกครั้ง)
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * ✅ Security Filter Chain — กำหนดลำดับการตรวจสอบ Request ก่อนถึง Controller
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("[DEBUG] SecurityConfig loaded ✅");
        System.out.println("[DEBUG] Registering JwtAuthenticationFilter into Security Chain...");

        http
            // ✅ ปิดการใช้ CSRF เพราะเราใช้ JWT แบบ Stateless
            .csrf(csrf -> csrf.disable())

            // ✅ ไม่ให้ Spring สร้าง Session เพราะเราจะตรวจ JWT ทุกครั้ง
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ✅ กำหนด Authorization Rule ของ API
            .authorizeHttpRequests(auth -> auth

                // -------------------------------
                // 🌍 Public Endpoints (ไม่ต้องแนบ Token)
                // -------------------------------
                // ✅ Auth API (Register/Login)
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                // ✅ Trip API (อ่านข้อมูลทริปได้แบบสาธารณะ)
                .requestMatchers("/api/trips", "/api/trips/**").permitAll()

                // ✅ อนุญาต OPTIONS (CORS preflight)
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                // -------------------------------
                // 🔒 Protected Endpoints (ต้องแนบ Token)
                // -------------------------------
                // ✅ เพิ่ม /api/trips/mine, POST, PUT, DELETE ให้ต้อง Authenticated
                .requestMatchers("/api/trips/mine").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/trips/**").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/trips/**").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/trips/**").authenticated()

                // ✅ Endpoints อื่น ๆ ทั้งหมด ต้อง Authenticated เช่นกัน
                .anyRequest().authenticated()
            )

            // ✅ เพิ่ม JWT Filter ก่อน UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ✅ PasswordEncoder — ใช้เข้ารหัส password ก่อนบันทึกใน Database
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ✅ AuthenticationManager — ใช้ตรวจสอบ username/password ตอน Login
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ✅ ปรับ Firewall — อนุญาตบางอักขระพิเศษใน URL ได้ เช่น //, ;, \
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
