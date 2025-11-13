package com.jamenori.travel.travel_backend.controller;

import com.jamenori.travel.travel_backend.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * ✅ FileController
 * Controller สำหรับอัปโหลดไฟล์ (รูปภาพ)
 *
 * 🔹 Endpoint:
 * POST /api/files/upload → อัปโหลดรูปภาพและคืน public URL
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final SupabaseStorageService storageService;

    /**
     * ✅ POST /api/files/upload
     * อัปโหลดรูปภาพไปยัง Supabase Storage
     *
     * @param file Multipart file จาก Frontend
     * @return public URL ของไฟล์ที่อัปโหลดสำเร็จ
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileUrl = storageService.uploadFile(file);
        return ResponseEntity.ok(fileUrl);
    }
}

