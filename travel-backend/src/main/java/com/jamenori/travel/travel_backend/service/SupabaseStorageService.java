package com.jamenori.travel.travel_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket}")
    private String bucket;

    @Value("${supabase.apiKey}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder().build();

    /**
     * ✅ uploadFile — อัปโหลดไฟล์ขึ้น Supabase Storage
     */
    public String uploadFile(MultipartFile file) {
        try {
            // ✅ Validation ก่อนอัปโหลด
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
            }

            if (file.getSize() > 5_000_000) { // จำกัด 5MB
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds 5MB limit");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
            }

            // ✅ สร้างชื่อไฟล์ใหม่แบบ unique
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();

            // ✅ URL สำหรับอัปโหลดไฟล์
            String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, filename);

            // ✅ ส่งคำขอ PUT ไปยัง Supabase API
            webClient.put()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("apikey", apiKey)
                    .header("Content-Type", file.getContentType())
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res ->
                            res.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new ResponseStatusException(
                                            HttpStatus.BAD_REQUEST,
                                            "Failed to upload to Supabase: " + body)))
                    )
                    .bodyToMono(String.class)
                    .block();

            // ✅ คืน URL สาธารณะ
            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, filename);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file");
        }
    }
}
