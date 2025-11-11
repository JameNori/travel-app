package com.jamenori.travel.travel_backend.dto;

import lombok.Data;

/**
 * ใช้รับข้อมูลตอน Register
 *
 * ✅ DTO (Data Transfer Object)
 * ใช้สำหรับ "รับข้อมูลจาก Frontend" เมื่อผู้ใช้สมัครสมาชิกใหม่
 * ตัว Spring จะใช้ Jackson ทำการแปลง JSON จาก body ให้กลายเป็น object RegisterRequest อัตโนมัติ
 *
 * ตัวอย่าง JSON ที่ Frontend (Vue.js) ส่งมา:
 * {
 *   "email": "user@example.com",
 *   "password": "123456",
 *   "displayName": "John Doe"
 * }
 */
@Data // ✅ Lombok: สร้าง getter/setter/toString ให้อัตโนมัติ
public class RegisterRequest {

    // ✅ อีเมลของผู้ใช้ (ใช้ตรวจสอบว่าเคยสมัครหรือยัง)
    private String email;

    // ✅ รหัสผ่าน (จะถูกเข้ารหัสด้วย BCrypt ก่อนบันทึกลงฐานข้อมูล)
    private String password;

    // ✅ ชื่อที่จะแสดงในระบบ เช่น “JameNori” หรือชื่อจริง
    private String displayName;
}

