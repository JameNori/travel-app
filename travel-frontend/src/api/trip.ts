import api from "./client";

export interface Trip {
  id: number;
  title: string;
  description?: string;
  photos?: string[];
  tags?: string[];
  latitude?: number;
  longitude?: number;
  url?: string;
  author?: {
    id: number;
    email: string;
    displayName: string;
  };
  createdAt?: string;
  updatedAt?: string;
}

export interface TripRequest {
  title: string;
  description?: string;
  photos?: string[];
  tags?: string[];
  latitude: number;
  longitude: number;
}

/**
 * ดึงทริปทั้งหมด (รองรับ query parameter สำหรับ search)
 * Public endpoint - ไม่ต้องมี JWT token
 */
export async function getAllTrips(query?: string): Promise<Trip[]> {
  const params = query ? { query } : {};
  const response = await api.get<Trip[]>("/trips", { params });
  return response.data;
}

/**
 * ดึงทริปรายการเดียวตาม ID
 * Public endpoint - ไม่ต้องมี JWT token
 */
export async function getTripById(id: number): Promise<Trip> {
  const response = await api.get<Trip>(`/trips/${id}`);
  return response.data;
}

/**
 * ดึงทริปของฉัน (ต้องมี JWT token)
 * Protected endpoint - ต้องแนบ JWT token (interceptor จัดการให้อัตโนมัติ)
 */
export async function getMyTrips(): Promise<Trip[]> {
  const response = await api.get<Trip[]>("/trips/mine");
  return response.data;
}

/**
 * สร้างทริปใหม่ (ต้องมี JWT token)
 * Protected endpoint - ต้องแนบ JWT token (interceptor จัดการให้อัตโนมัติ)
 */
export async function createTrip(tripData: TripRequest): Promise<Trip> {
  const response = await api.post<Trip>("/trips", tripData);
  return response.data;
}

/**
 * อัปเดตทริป (ต้องมี JWT token + เป็นเจ้าของ)
 * Protected endpoint - ต้องแนบ JWT token และเป็นเจ้าของทริปเท่านั้น
 */
export async function updateTrip(
  id: number,
  tripData: TripRequest
): Promise<Trip> {
  const response = await api.put<Trip>(`/trips/${id}`, tripData);
  return response.data;
}

/**
 * ลบทริป (ต้องมี JWT token + เป็นเจ้าของ)
 * Protected endpoint - ต้องแนบ JWT token และเป็นเจ้าของทริปเท่านั้น
 */
export async function deleteTrip(id: number): Promise<void> {
  await api.delete(`/trips/${id}`);
}
