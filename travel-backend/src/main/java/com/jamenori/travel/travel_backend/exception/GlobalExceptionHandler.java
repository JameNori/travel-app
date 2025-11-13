package com.jamenori.travel.travel_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ Global Exception Handler
 *
 * รวมศูนย์การจัดการ Error ที่เกิดขึ้นในระบบ
 * ส่ง response เป็น JSON ที่อ่านง่ายให้ frontend แสดงผลได้สะดวก
 *
 * 📦 ตัวอย่าง response ที่ frontend จะได้รับ:
 * {
 *   "error": "Validation failed",
 *   "details": { "email": "must not be blank" },
 *   "path": "/api/auth/register"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 🔸 Validation Error — ดักจับ error จาก @Valid / @NotBlank / @Size
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("details", fieldErrors);
        body.put("path", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 🔸 Missing Multipart File — กรณีไม่ส่งไฟล์มาใน request (เช่น key 'file' หาย)
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handleMissingFile(MissingServletRequestPartException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "400 BAD_REQUEST");
        body.put("details", "Missing required file part: " + ex.getRequestPartName());
        body.put("path", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 🔸 IllegalArgumentException — เช่น email ซ้ำ, password ไม่ถูกต้อง
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Invalid input");
        body.put("details", ex.getMessage());
        body.put("path", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 🔸 ResponseStatusException — ใช้ใน service layer (เช่น SupabaseStorageService)
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getStatusCode().toString());
        body.put("details", ex.getReason());
        body.put("path", request.getDescription(false));
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    /**
     * 🔸 SecurityException — กรณี unauthorized (401)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(SecurityException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Unauthorized");
        body.put("details", ex.getMessage());
        body.put("path", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * 🔸 Fallback — สำหรับ Exception อื่น ๆ ที่ไม่ได้ handle
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Internal server error");
        body.put("details", ex.getMessage());
        body.put("path", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
