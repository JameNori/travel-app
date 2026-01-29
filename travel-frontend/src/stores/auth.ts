import { defineStore } from "pinia";
import { ref } from "vue";
import * as authAPI from "../api/auth";

export const useAuthStore = defineStore("auth", () => {
  // อ่าน token จาก localStorage ตอน initialize เพื่อ persist state หลัง refresh
  const token = ref<string | null>(localStorage.getItem("token"));
  // ใช้ "null" string เป็น fallback เพื่อป้องกัน JSON.parse error เมื่อ localStorage ว่าง
  const user = ref<any | null>(
    JSON.parse(localStorage.getItem("user") || "null"),
  );
  // sync isAuthenticated กับ token เพื่อให้ reactive และไม่ต้อง check token.value ทุกครั้ง
  const isAuthenticated = ref<boolean>(!!token.value);

  /**
   * Login — เข้าสู่ระบบ
   * เรียก API login และเก็บ token/user ใน store + localStorage
   */
  async function login(email: string, password: string) {
    try {
      const response = await authAPI.login(email, password);
      token.value = response.token;
      user.value = {
        userId: response.userId,
        email: response.email,
        displayName: response.displayName,
      };
      isAuthenticated.value = true;
      localStorage.setItem("token", response.token);
      localStorage.setItem("user", JSON.stringify(user.value));
      return response;
    } catch (error) {
      throw error;
    }
  }

  /**
   * Register — สมัครสมาชิกใหม่
   * เรียก API register และเก็บ token/user ใน store + localStorage
   */
  async function register(
    email: string,
    password: string,
    displayName: string,
  ) {
    try {
      const response = await authAPI.register(email, password, displayName);
      token.value = response.token;
      user.value = {
        userId: response.userId,
        email: response.email,
        displayName: response.displayName,
      };
      isAuthenticated.value = true;
      localStorage.setItem("token", response.token);
      localStorage.setItem("user", JSON.stringify(user.value));
      return response;
    } catch (error) {
      throw error;
    }
  }

  /**
   * Logout — ออกจากระบบ
   * เรียก API logout และลบ token/user จาก store + localStorage
   */
  async function logout() {
    try {
      await authAPI.logout();
    } catch (error) {
      // ยังคงลบ token แม้ API error เพื่อให้ user logout ได้เสมอ
      console.error("Logout error:", error);
    } finally {
      token.value = null;
      user.value = null;
      isAuthenticated.value = false;
      localStorage.removeItem("token");
      localStorage.removeItem("user");
    }
  }

  /**
   * Change Password — เปลี่ยนรหัสผ่าน (ต้อง login แล้ว)
   */
  async function changePassword(currentPassword: string, newPassword: string) {
    await authAPI.changePassword(currentPassword, newPassword);
  }

  /**
   * Fetch Profile — ดึงข้อมูลผู้ใช้ปัจจุบันจาก JWT
   * ใช้ตรวจสอบว่า token ยัง valid หรือไม่ และอัปเดต user info
   */
  async function fetchProfile() {
    try {
      const profile = await authAPI.getProfile();
      // อัปเดต email ถ้ามีการเปลี่ยนแปลง (แต่ไม่เปลี่ยน userId/displayName เพราะ API /me ส่งแค่ email)
      if (user.value) {
        user.value.email = profile.email;
        localStorage.setItem("user", JSON.stringify(user.value));
      }
      return profile;
    } catch (error: any) {
      // ถ้า token หมดอายุหรือไม่ถูกต้อง ให้ logout
      if (error.response?.status === 401 || error.response?.status === 403) {
        await logout();
      }
      throw error;
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    register,
    logout,
    changePassword,
    fetchProfile,
  };
});
