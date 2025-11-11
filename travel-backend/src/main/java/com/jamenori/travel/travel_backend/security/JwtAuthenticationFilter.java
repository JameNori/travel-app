package com.jamenori.travel.travel_backend.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ✅ JwtAuthenticationFilter
 * ใช้ตรวจสอบ JWT จาก Header ในทุก request
 * ยกเว้นเฉพาะ /api/auth/register และ /api/auth/login
 *
 * 🔹 Flow ของ Filter:
 * 1. ตรวจว่าคำขออยู่ใน path ที่ไม่ต้องเช็ค token ไหม
 * 2. ถ้ามี header Authorization → ดึง token ออกมา
 * 3. ใช้ JwtService ตรวจสอบว่า token ถูกต้องหรือไม่
 * 4. ถ้าถูกต้อง → เซ็ต Authentication เข้าสู่ SecurityContext
 * 5. ให้ request ผ่านต่อไปถึง Controller
 */
@Component
@RequiredArgsConstructor // ✅ ให้ Spring สร้าง constructor อัตโนมัติสำหรับ dependency ที่เป็น final
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @PostConstruct
    public void init() {
        System.out.println("[DEBUG] JwtAuthenticationFilter initialized ✅");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String path = request.getServletPath();
        System.out.println("[DEBUG] Incoming request path: " + path);

        // ✅ 1. ข้ามเฉพาะ /register และ /login เท่านั้น (public path)
        if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) {
            System.out.println("[JWT FILTER] Skipping JWT validation for public path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 2. ดึง Authorization header จาก request
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // ❌ ไม่มี header หรือรูปแบบผิด → ไม่ authenticate แต่ยังปล่อยผ่านไป (บาง endpoint อาจไม่ต้องใช้ token)
            System.out.println("[JWT FILTER] No or invalid Authorization header, skip authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 3. แยก JWT ออกจาก header (ตัดคำว่า "Bearer " ออก)
        final String jwt = authHeader.substring(7);
        String userEmail;

        try {
            // ✅ 4. ดึง email (subject) ออกจาก token
            userEmail = jwtService.extractUsername(jwt);
            System.out.println("[JWT FILTER] Extracted userEmail: " + userEmail);
        } catch (Exception e) {
            // ❌ Token ผิดรูปแบบ / หมดอายุ / ปลอม
            System.out.println("[JWT FILTER] Failed to extract username from token: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 5. ตรวจสอบว่าผู้ใช้ยังไม่ได้ authenticated อยู่แล้วใน context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // โหลด user จากฐานข้อมูล
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // ✅ 6. ตรวจสอบว่า token ยัง valid และตรงกับผู้ใช้ในระบบ
            if (jwtService.isTokenValid(jwt, userDetails)) {
                System.out.println("[JWT FILTER] ✅ Token valid for user: " + userEmail);

                // ✅ 7. สร้าง Authentication object แล้วเซ็ตเข้ากับ SecurityContext
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println("[JWT FILTER] ❌ Invalid or expired token for user: " + userEmail);
            }
        }

        // ✅ 8. ส่ง request ต่อไปยัง filter ถัดไปหรือ Controller
        filterChain.doFilter(request, response);
    }
}
