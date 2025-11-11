package com.jamenori.travel.travel_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ✅ WebConfig
 * ใช้กำหนดการตั้งค่า CORS (Cross-Origin Resource Sharing)
 * เพื่อให้ Frontend (เช่น Vue.js) สามารถยิง request มาหา Backend ได้
 * โดยเฉพาะในช่วงพัฒนา (localhost ต่างพอร์ต)
 */
@Configuration
public class WebConfig {

    /**
     * ✅ กำหนด Bean ที่ใช้จัดการ CORS ทั้งระบบ
     * Spring Boot จะเรียกใช้ค่านี้อัตโนมัติในทุก endpoint
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                // ✅ อนุญาตให้ทุก endpoint (/**) รองรับการเรียกข้ามโดเมน
                registry.addMapping("/**")
                        // ✅ เปิดทุก origin (เช่น http://localhost:5173 ของ Vue)
                        // ถ้าต้องการจำกัดสามารถใส่เฉพาะโดเมนได้ เช่น .allowedOrigins("https://myfrontend.com")
                        .allowedOrigins("*")

                        // ✅ อนุญาตเฉพาะ HTTP methods ที่ต้องใช้ในระบบ
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                        // ✅ เปิดรับทุก header (เช่น Authorization, Content-Type)
                        .allowedHeaders("*")

                        // ⚠️ allowCredentials(false): 
                        // หมายความว่าไม่อนุญาตให้ส่ง Cookie หรือ Authorization header แบบ credential 
                        // ถ้าระบบใช้ JWT ผ่าน header อยู่แล้ว ก็สามารถ false ได้
                        .allowCredentials(false);
            }
        };
    }
}
