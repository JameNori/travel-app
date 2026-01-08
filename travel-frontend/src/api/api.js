// src/api/api.js
// ไฟล์นี้จะถูกย้ายไป trip.js
// ตอนนี้ใช้ client.ts เป็น axios instance กลาง
import api from "./client";

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
