import api from "./client";

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  displayName: string;
}

export interface ProfileResponse {
  email: string;
}

/**
 * Register — สมัครสมาชิกใหม่
 */
export async function register(
  email: string,
  password: string,
  displayName: string,
): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>("/auth/register", {
    email,
    password,
    displayName,
  });
  return response.data;
}

/**
 * Login — เข้าสู่ระบบ
 */
export async function login(
  email: string,
  password: string,
): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>("/auth/login", {
    email,
    password,
  });
  return response.data;
}

/**
 * Logout — ออกจากระบบ
 * ฝั่ง backend ไม่ต้องลบ session (JWT stateless) แต่ frontend จะลบ token เอง
 */
export async function logout(): Promise<void> {
  await api.post("/auth/logout");
}

/**
 * Get Profile — ตรวจสอบผู้ใช้ปัจจุบันจาก JWT
 * ต้องแนบ JWT token ใน Authorization header (interceptor จัดการให้อัตโนมัติ)
 */
export async function getProfile(): Promise<ProfileResponse> {
  const response = await api.get<ProfileResponse>("/auth/me");
  return response.data;
}

/**
 * Change Password — เปลี่ยนรหัสผ่าน (ต้อง login แล้ว)
 */
export async function changePassword(
  currentPassword: string,
  newPassword: string,
): Promise<void> {
  await api.put("/auth/change-password", {
    currentPassword,
    newPassword,
  });
}
