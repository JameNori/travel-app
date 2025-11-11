package com.jamenori.travel.travel_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ Global Exception Handler
 *
 * ใช้รวมศูนย์การจัดการ Error ที่เกิดขึ้นในระบบ
 * เช่น Validation fail, IllegalArgumentException, Unauthorized, หรือ error ทั่วไป
 *
 * แทนที่ระบบจะส่ง Stack Trace ออกไป (ซึ่งไม่เหมาะกับ production)
 * เราจะจัดรูปแบบให้เป็น JSON ที่อ่านง่ายเพื่อให้ frontend แสดงผลได้สะดวก
 *
 * 📦 ตัวอย่าง response ที่ frontend จะได้รับ:
 * {
 *   "message": "Validation failed",
 *   "errors": { "email": "must not be blank" }
 * }
 */
@RestControllerAdvice // ✅ บอก Spring ให้ class นี้ดัก Exception จากทุก Controller
public class GlobalExceptionHandler {

    /**
     * 🔸 Validation Error — ดักจับ error จาก @Valid / @NotBlank / @Size
     * เช่น input จาก frontend ไม่ผ่าน validation rule
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        // ✅ รวม error จากทุก field ที่ validate ไม่ผ่าน
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        // ✅ สร้าง response body
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Validation failed");
        body.put("errors", fieldErrors);

        // ✅ ส่งกลับ HTTP 400 (Bad Request)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 🔸 IllegalArgumentException — ใช้สำหรับ error ที่โยนจาก AuthService หรือ Business Logic
     * เช่น email ซ้ำ, password ไม่ถูกต้อง
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 🔸 SecurityException — ใช้สำหรับกรณี unauthorized (401)
     * เช่น JWT ไม่ถูกต้อง, ไม่มีสิทธิ์เข้าถึง resource
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(SecurityException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Unauthorized");
        body.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * 🔸 Fallback — ใช้สำหรับ Exception อื่น ๆ ที่ไม่ได้ handle ไว้ข้างต้น
     * เช่น NullPointerException, Database error, หรือ Unexpected error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Internal server error");
        body.put("details", ex.getMessage());
        body.put("path", request.getDescription(false)); // ✅ แสดง path ที่เกิด error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
