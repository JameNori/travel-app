package com.jamenori.travel.travel_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.jamenori.travel.travel_backend.entity.Trip;
import com.jamenori.travel.travel_backend.entity.User;
import java.util.List;

/**
 * ✅ TripRepository
 * Repository สำหรับจัดการข้อมูลทริป (trips)
 *
 * 🔹 หน้าที่ของ Repository ชั้นนี้:
 *  - ติดต่อกับฐานข้อมูลผ่าน JPA
 *  - ดึง / ค้นหา / บันทึก / ลบข้อมูลทริป
 *  - มี native query สำหรับค้นหาด้วย PostgreSQL UNNEST()
 */
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * ✅ ดึงทริปทั้งหมดที่ผู้ใช้คนใดเป็นเจ้าของ
     * ใช้ใน endpoint: GET /api/trips/mine
     */
    List<Trip> findByAuthor(User author);

    /**
     * ✅ ดึงทริปทั้งหมดที่ผู้ใช้คนใดเป็นเจ้าของ เรียงตามวันที่สร้าง (ล่าสุดก่อน) แล้วตามด้วย id (ใหม่สุดก่อน)
     * ใช้ใน endpoint: GET /api/trips/mine
     */
    @Query("SELECT t FROM Trip t WHERE t.author = :author ORDER BY t.createdAt DESC, t.id DESC")
    List<Trip> findByAuthorOrderByCreatedAtDesc(@Param("author") User author);

    /**
     * ✅ ดึงทริปทั้งหมดเรียงตามวันที่สร้าง (ล่าสุดก่อน) แล้วตามด้วย id (ใหม่สุดก่อน)
     * ใช้ใน endpoint: GET /api/trips
     */
    @Query("SELECT t FROM Trip t ORDER BY t.createdAt DESC, t.id DESC")
    List<Trip> findAllByOrderByCreatedAtDesc();

    /**
     * ✅ ค้นหาทริปด้วยคำค้น (keyword)
     * สามารถค้นหาได้จาก title, description และ tags[]
     *
     * 🔹 ใช้ใน endpoint: GET /api/trips?query=
     * 🔹 ใช้ PostgreSQL UNNEST() เพื่อแปลง tags array ออกมาเปรียบเทียบทีละค่า
     */
    @Query(
        value = """
            SELECT * FROM trips
            WHERE LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR EXISTS (
                    SELECT 1
                    FROM unnest(tags) AS tag
                    WHERE LOWER(tag) LIKE LOWER(CONCAT('%', :keyword, '%'))
               )
            ORDER BY created_at DESC, id DESC
            """,
        nativeQuery = true
    )
    List<Trip> searchTrips(@Param("keyword") String keyword);
}
