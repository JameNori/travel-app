// src/api/api.js
import axios from "axios";

// ✅ สร้าง instance หลักของ axios พร้อม baseURL จาก .env
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

// ✅ ดึงข้อมูลทริปทั้งหมด (GET /api/trips)
export async function getTrips() {
  try {
    const response = await api.get("/trips"); // "/trips" ต้องตรงกับ API path ใน Spring Boot
    return response.data;
  } catch (error) {
    console.error("Error fetching trips:", error);
    return [];
  }
}

// ✅ ตัวอย่าง: เพิ่มทริปใหม่ (POST /api/trips)
export async function createTrip(tripData) {
  try {
    const response = await api.post("/trips", tripData);
    return response.data;
  } catch (error) {
    console.error("Error creating trip:", error);
    throw error;
  }
}
