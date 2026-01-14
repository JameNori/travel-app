package com.jamenori.travel.travel_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * TripRequest DTO
 *
 * ใช้เป็นตัวกลางในการรับข้อมูลจาก client ก่อนจะส่งไปยัง Service
 * เพื่อป้องกันการ bind Entity ตรง ๆ และเพิ่ม Validation
 */
public record TripRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        String title,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        List<String> photos,     // optional
        List<String> tags,       // optional

        String url,              // optional - external URL (e.g., Wongnai)

        @NotNull(message = "Latitude is required")
        Double latitude,

        @NotNull(message = "Longitude is required")
        Double longitude
) {}

