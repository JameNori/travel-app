package com.jamenori.travel.travel_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * จัดการสร้าง/อ่าน/ตรวจสอบ JWT
 *
 * ✅ คลาสนี้เป็นตัวกลางสำหรับการทำงานกับ JWT โดยใช้ไลบรารี io.jsonwebtoken (JJWT)
 * - generateToken() → ใช้สร้าง token ใหม่
 * - extractUsername() → ใช้ดึง email/username ออกจาก token
 * - isTokenValid() → ใช้ตรวจสอบว่า token หมดอายุหรือไม่ และตรงกับ user ปัจจุบันไหม
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecretBase64; // ✅ เก็บ secret key แบบ Base64 (อ่านจากไฟล์ application.properties)

    @Value("${jwt.expiration}")
    private long jwtExpirationMs; // ✅ อายุของ token (หน่วย: milliseconds)

    /**
     * ✅ แปลง secret key จาก Base64 → Binary → Key object
     * ใช้สำหรับเข้ารหัส/ถอดรหัส JWT (algorithm HS256)
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * ✅ ดึงค่า subject (email/username) จาก token
     * JWT จะเก็บข้อมูลนี้ในฟิลด์ "sub" (subject)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * ✅ ฟังก์ชัน generic สำหรับดึง claim อื่น ๆ จาก token
     * สามารถใช้ดึง expiration, issuedAt หรือ custom claim เพิ่มได้
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * ✅ แปลง token (string) → Claims (ข้อมูลใน payload)
     * ใช้ parserBuilder() รุ่นใหม่ที่ปลอดภัยกว่า parser() รุ่นเก่า
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // ✅ ใช้ key ที่เราสร้างไว้ตรวจสอบลายเซ็น
                .build()
                .parseClaimsJws(token) // ✅ ถอด JWT แล้วตรวจสอบลายเซ็น
                .getBody(); // ✅ คืน payload (Claims)
    }

    /**
     * ✅ สร้าง token จากข้อมูลผู้ใช้ (UserDetails)
     * โดยค่า subject = email ของผู้ใช้
     */
    public String generateToken(UserDetails userDetails) {
        return generateTokenFromEmail(userDetails.getUsername());
    }

    /**
     * ✅ สร้าง JWT token จาก email โดยตรง
     * 1. ใส่ subject = email
     * 2. ใส่เวลาออก token (issuedAt)
     * 3. ใส่วันหมดอายุ (expiration)
     * 4. เซ็นด้วย HS256
     */
    public String generateTokenFromEmail(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(email) // ✅ "sub": email
                .setIssuedAt(now) // ✅ เวลาที่ออก token
                .setExpiration(expiry) // ✅ เวลาหมดอายุของ token
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // ✅ เซ็นด้วย secret key
                .compact(); // ✅ แปลงเป็น string พร้อมใช้งาน
    }

    /**
     * ✅ ตรวจสอบความถูกต้องของ token
     * 1. ดึง username ออกจาก token
     * 2. เทียบกับ username ของ UserDetails ปัจจุบัน
     * 3. ตรวจสอบว่า token ยังไม่หมดอายุ
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * ✅ ตรวจสอบว่า token หมดอายุหรือยัง
     */
    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
}
