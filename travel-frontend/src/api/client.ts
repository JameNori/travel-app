import axios from "axios";

// สร้าง axios instance กลางเพื่อ reuse configuration และ interceptor
// baseURL จาก env เพื่อแยก config ระหว่าง dev/prod
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request Interceptor: แนบ JWT token จาก localStorage อัตโนมัติทุก request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Handle 401 (Unauthorized) เพื่อ clear token และเตรียม redirect
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token หมดอายุหรือไม่ถูกต้อง - ลบ token และ user เพื่อความปลอดภัย
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      // ไว้เพิ่ม redirect ไป login ในอนาคตเมื่อมี auth pages
      // window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;
