import { defineStore } from "pinia";
import { ref } from "vue";

export const useAuthStore = defineStore("auth", () => {
  // อ่าน token จาก localStorage ตอน initialize เพื่อ persist state หลัง refresh
  const token = ref<string | null>(localStorage.getItem("token"));
  // ใช้ "null" string เป็น fallback เพื่อป้องกัน JSON.parse error เมื่อ localStorage ว่าง
  const user = ref<any | null>(
    JSON.parse(localStorage.getItem("user") || "null")
  );
  // sync isAuthenticated กับ token เพื่อให้ reactive และไม่ต้อง check token.value ทุกครั้ง
  const isAuthenticated = ref<boolean>(!!token.value);

  function setToken(newToken: string | null) {
    token.value = newToken;
    isAuthenticated.value = !!newToken;
    if (newToken) {
      localStorage.setItem("token", newToken);
    } else {
      // ลบ token จาก localStorage เมื่อ logout เพื่อความปลอดภัย
      localStorage.removeItem("token");
    }
  }

  function setUser(newUser: any | null) {
    user.value = newUser;
    if (newUser) {
      localStorage.setItem("user", JSON.stringify(newUser));
    } else {
      localStorage.removeItem("user");
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    setToken,
    setUser,
  };
});
