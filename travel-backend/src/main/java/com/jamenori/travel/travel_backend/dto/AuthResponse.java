package com.jamenori.travel.travel_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * ใช้ส่งกลับไปให้ front หลัง register/login
 *
 * ✅ DTO (Data Transfer Object)
 * เป็นคลาสที่ใช้สำหรับ "ขนส่งข้อมูล" ระหว่างฝั่ง backend และ frontend
 * โดยไม่ต้องเปิดเผย entity จริง (User)
 * ช่วยให้ปลอดภัยและควบคุม field ที่จะส่งออกได้ง่าย
 */
@Data // ✅ สร้าง getter/setter/toString ให้อัตโนมัติด้วย Lombok
@AllArgsConstructor // ✅ สร้าง constructor ที่รับทุก field
@Builder // ✅ ใช้ builder pattern เพื่อสร้าง object ได้ง่าย เช่น AuthResponse.builder().token(...).build()
public class AuthResponse {

    // ✅ JWT Token ที่ออกให้ผู้ใช้หลังจาก login หรือ register สำเร็จ
    private String token;

    // ✅ userId ของผู้ใช้ (primary key จากตาราง users)
    private Long userId;

    // ✅ อีเมลของผู้ใช้ (ใช้เป็น username)
    private String email;

    // ✅ ชื่อที่จะแสดงใน frontend เช่น "John Doe"
    private String displayName;
}

