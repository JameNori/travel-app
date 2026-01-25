package com.jamenori.travel.travel_backend.config;

import com.jamenori.travel.travel_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * SecurityConfig
 *
 * คลาสนี้ใช้กำหนดการตั้งค่าทั้งหมดของ Spring Security ในระบบ
 * จุดประสงค์หลักของคลาสนี้:
 *
 * 1. กำหนดว่า Endpoint ไหนเข้าถึงได้โดยไม่ต้องมี JWT (public)
 * 2. กำหนดว่า Endpoint ไหนต้องมี JWT (protected)
 * 3. ปิดการใช้ Session เพราะระบบใช้ JWT แบบ Stateless
 * 4. เพิ่ม JwtAuthenticationFilter ก่อนเข้าสู่ Controller
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Filter ที่ใช้ตรวจสอบความถูกต้องของ JWT ทุก request
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Security Filter Chain
     * เมธอดหลักที่กำหนดการเข้าถึงของทุก API ในระบบ
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("[DEBUG] SecurityConfig loaded.");

        http
            // ปิด CSRF เนื่องจากใช้ JWT แทน Cookie
            .csrf(csrf -> csrf.disable())

            // ✅ เพิ่ม CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ตั้งค่าให้ระบบทำงานแบบ Stateless (ไม่สร้าง session)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // กำหนด Authorization rules
            .authorizeHttpRequests(auth -> auth

                // -----------------------------------------------------
                // Public endpoints (ไม่ต้องส่ง JWT)
                // -----------------------------------------------------

                // สมัครสมาชิก / ล็อกอิน
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                // ดึงรายการทริปทั้งหมด และอ่านทริปรายตัว (GET เท่านั้น)
                .requestMatchers(HttpMethod.GET, "/api/trips", "/api/trips/**").permitAll()

                // อนุญาต OPTIONS (สำหรับ CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // -----------------------------------------------------
                // Protected endpoints (ต้องมี JWT)
                // -----------------------------------------------------

                // ดึงทริปที่ user เป็นเจ้าของ
                .requestMatchers("/api/trips/mine").authenticated()

                // การสร้าง / แก้ไข / ลบ ทริป
                .requestMatchers(HttpMethod.POST, "/api/trips/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/trips/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/trips/**").authenticated()

                // upload รูป ต้องใช้ token เช่นกัน
                .requestMatchers(HttpMethod.POST, "/api/files/upload").authenticated()

                // default: endpoint อื่นทั้งหมดต้องตรวจสอบ JWT
                .anyRequest().authenticated()
            )

            // เพิ่ม JWT Filter ก่อน UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * PasswordEncoder — ใช้เข้ารหัสรหัสผ่านก่อนบันทึกลงฐานข้อมูล
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager — ใช้โดย AuthService ในการตรวจสอบ email/password
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ✅ CORS Configuration Source
     * กำหนด CORS settings สำหรับ Spring Security
     * อนุญาตให้ frontend (localhost:5173) สามารถเรียก API ได้
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // อนุญาต origin จาก frontend
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
        
        // อนุญาต HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // อนุญาต headers (รวมถึง Authorization สำหรับ JWT)
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // ✅ ต้องเป็น true เพื่อให้ส่ง Authorization header ได้
        configuration.setAllowCredentials(true);
        
        // ตั้งค่า max age สำหรับ preflight request (1 ชั่วโมง)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * ปรับ Firewall เพื่ออนุญาตอักขระพิเศษใน URL
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
