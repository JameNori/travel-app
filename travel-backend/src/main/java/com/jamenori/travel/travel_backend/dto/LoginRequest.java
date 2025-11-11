package com.jamenori.travel.travel_backend.dto;

import lombok.Data;

/**
 * ใช้รับข้อมูลตอน Login
 *
 * ✅ DTO (Data Transfer Object)
 * ใช้สำหรับ "รับข้อมูลจาก Frontend" ในรูปแบบ JSON
 * เมื่อผู้ใช้กดปุ่ม Login บนหน้าเว็บ Vue.js
 * ข้อมูลเช่น email/password จะถูกส่งมาใน request body แล้ว map เข้า object นี้โดยอัตโนมัติ
 */
@Data // ✅ สร้าง getter/setter/toString ให้อัตโนมัติด้วย Lombok
public class LoginRequest {

    // ✅ email ของผู้ใช้ — ใช้เป็น username สำหรับตรวจสอบการเข้าสู่ระบบ
    private String email;

    // ✅ password ของผู้ใช้ — จะถูกตรวจสอบกับข้อมูลที่เข้ารหัสไว้ใน database
    private String password;
}
