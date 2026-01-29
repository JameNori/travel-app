package com.jamenori.travel.travel_backend.dto;

import lombok.Data;

/**
 * ใช้รับข้อมูลตอนเปลี่ยนรหัสผ่าน (Change Password)
 * ต้องส่งรหัสปัจจุบันและรหัสใหม่ (ยืนยันรหัสตรวจฝั่ง frontend)
 */
@Data
public class ChangePasswordRequest {

    /** รหัสผ่านปัจจุบัน — ใช้ตรวจกับ passwordHash ใน DB */
    private String currentPassword;

    /** รหัสผ่านใหม่ — จะถูกเข้ารหัสแล้วบันทึก */
    private String newPassword;
}
