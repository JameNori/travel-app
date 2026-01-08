// src/api/api.js
import axios from "axios";

// สร้าง axios instance กลางเพื่อ reuse configuration และ interceptor
// baseURL จาก env เพื่อแยก config ระหว่าง dev/prod
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

// getTrips return [] แทน throw เพื่อให้ UI ไม่ break เมื่อ API error (UX: แสดง empty state)
export async function getTrips() {
  try {
    const response = await api.get("/trips"); // "/trips" ต้องตรงกับ API path ใน Spring Boot
    return response.data;
  } catch (error) {
    console.error("Error fetching trips:", error);
    return [];
  }
}

// createTrip throw error เพื่อให้ caller จัดการ error (เช่น show toast) เพราะเป็น user action
export async function createTrip(tripData) {
  try {
    const response = await api.post("/trips", tripData);
    return response.data;
  } catch (error) {
    console.error("Error creating trip:", error);
    throw error;
  }
}
