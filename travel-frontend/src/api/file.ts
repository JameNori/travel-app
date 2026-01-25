import api from "./client";

/**
 * อัปโหลดไฟล์รูปภาพไปยัง Supabase Storage
 * Protected endpoint - ต้องแนบ JWT token (interceptor จัดการให้อัตโนมัติ)
 * 
 * @param file File object จาก input หรือ drag & drop
 * @returns Promise<string> public URL ของไฟล์ที่อัปโหลดสำเร็จ
 */
export async function uploadFile(file: File): Promise<string> {
  const formData = new FormData();
  formData.append("file", file);

  const response = await api.post<string>("/files/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });

  return response.data;
}
