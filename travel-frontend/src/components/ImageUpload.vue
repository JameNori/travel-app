<template>
  <div class="image-upload">
    <!-- Upload Area -->
    <div
      class="image-upload__dropzone"
      :class="{
        'image-upload__dropzone--dragover': isDragging,
        'image-upload__dropzone--disabled': isUploading,
      }"
      @drop.prevent="handleDrop"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @click="triggerFileInput"
    >
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        multiple
        class="image-upload__input"
        @change="handleFileSelect"
        :disabled="isUploading"
      />

      <div v-if="!isUploading" class="image-upload__content">
        <svg
          class="image-upload__icon"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
          />
        </svg>
        <p class="image-upload__text">
          <span class="image-upload__text-primary">คลิกหรือลากไฟล์</span>
          เพื่ออัปโหลดรูปภาพ
        </p>
        <p class="image-upload__text-secondary">
          รองรับ JPG, PNG, GIF (สูงสุด 5MB ต่อไฟล์)
        </p>
      </div>

      <div v-else class="image-upload__uploading">
        <div class="image-upload__spinner"></div>
        <p class="image-upload__text">กำลังอัปโหลด...</p>
      </div>
    </div>

    <!-- Image Preview Grid -->
    <div v-if="previewUrls.length > 0 || imageUrls.length > 0" class="image-upload__preview-grid">
      <!-- Preview from local files (กำลังอัปโหลด) -->
      <div
        v-for="(preview, index) in previewUrls"
        :key="`preview-${preview.id}`"
        class="image-upload__preview-item"
      >
        <img
          :src="preview.url"
          :alt="`Preview ${index + 1}`"
          class="image-upload__preview-image"
        />
        <div class="image-upload__upload-overlay">
          <div class="image-upload__spinner-small"></div>
          <p class="image-upload__upload-text">กำลังอัปโหลด...</p>
        </div>
        <button
          type="button"
          class="image-upload__remove-button"
          @click="cancelUpload(preview.id)"
          :disabled="isUploading"
          aria-label="ยกเลิก"
        >
          <svg
            class="image-upload__remove-icon"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <!-- Preview from uploaded URLs (อัปโหลดเสร็จแล้ว) -->
      <div
        v-for="(url, index) in imageUrls"
        :key="`uploaded-${index}`"
        class="image-upload__preview-item"
      >
        <img
          :src="url"
          :alt="`Preview ${index + 1}`"
          class="image-upload__preview-image"
          @error="handleImageError(index)"
        />
        <button
          type="button"
          class="image-upload__remove-button"
          @click="removeImage(index)"
          :disabled="isUploading"
          aria-label="ลบรูปภาพ"
        >
          <svg
            class="image-upload__remove-icon"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>
    </div>

    <!-- Error Message -->
    <div v-if="errorMessage" class="image-upload__error">
      <svg
        class="image-upload__error-icon"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <p class="image-upload__error-text">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { uploadFile } from "../api/file";

interface Props {
  modelValue: string[];
  maxFiles?: number;
}

interface Emits {
  (e: "update:modelValue", value: string[]): void;
}

interface PreviewItem {
  id: string;
  url: string;
  file: File;
}

const props = withDefaults(defineProps<Props>(), {
  maxFiles: 10,
});

const emit = defineEmits<Emits>();

const fileInputRef = ref<HTMLInputElement | null>(null);
const isDragging = ref(false);
const isUploading = ref(false);
const errorMessage = ref("");
const imageUrls = ref<string[]>([]);
const previewUrls = ref<PreviewItem[]>([]); // เพิ่ม preview URLs

// Sync with modelValue
watch(
  () => props.modelValue,
  (newValue) => {
    imageUrls.value = newValue ? [...newValue] : [];
  },
  { immediate: true }
);

// Emit changes
watch(imageUrls, (newUrls) => {
  emit("update:modelValue", newUrls);
});

/**
 * Trigger file input click
 */
function triggerFileInput() {
  if (isUploading.value) return;
  fileInputRef.value?.click();
}

/**
 * Handle file selection from input
 */
function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    handleFiles(Array.from(target.files));
  }
  // Reset input value to allow selecting the same file again
  if (target) {
    target.value = "";
  }
}

/**
 * Handle file drop
 */
function handleDrop(event: DragEvent) {
  isDragging.value = false;
  if (isUploading.value) return;

  const files = event.dataTransfer?.files;
  if (files && files.length > 0) {
    handleFiles(Array.from(files));
  }
}

/**
 * Handle files (from input or drop)
 */
async function handleFiles(files: File[]) {
  errorMessage.value = "";

  // Validate file count (รวม preview ที่กำลังอัปโหลด)
  const remainingSlots = props.maxFiles - imageUrls.value.length - previewUrls.value.length;
  if (files.length > remainingSlots) {
    errorMessage.value = `สามารถอัปโหลดได้สูงสุด ${props.maxFiles} รูป (เหลือ ${remainingSlots} รูป)`;
    return;
  }

  // Filter image files only
  const imageFiles = files.filter((file) => file.type.startsWith("image/"));

  if (imageFiles.length === 0) {
    errorMessage.value = "กรุณาเลือกไฟล์รูปภาพเท่านั้น";
    return;
  }

  if (imageFiles.length < files.length) {
    errorMessage.value = "บางไฟล์ไม่ใช่รูปภาพและจะไม่ถูกอัปโหลด";
  }

  // Validate file size (5MB = 5 * 1024 * 1024 bytes)
  const maxSize = 5 * 1024 * 1024;
  const validFiles = imageFiles.filter((file) => {
    if (file.size > maxSize) {
      errorMessage.value = `ไฟล์ ${file.name} มีขนาดเกิน 5MB`;
      return false;
    }
    return true;
  });

  if (validFiles.length === 0) {
    return;
  }

  // สร้าง preview URLs จาก local files ทันที
  const newPreviews: PreviewItem[] = validFiles.map((file) => {
    const previewUrl = URL.createObjectURL(file);
    return {
      id: `${Date.now()}-${Math.random()}`,
      url: previewUrl,
      file: file,
    };
  });

  // เพิ่ม preview URLs เพื่อแสดงรูปทันที
  previewUrls.value = [...previewUrls.value, ...newPreviews];

  // อัปโหลดไฟล์แบบ background (ไม่ block UI)
  isUploading.value = true;
  errorMessage.value = "";

  try {
    // อัปโหลดทีละไฟล์พร้อมแสดง progress
    const uploadPromises = newPreviews.map(async (preview) => {
      try {
        const url = await uploadFile(preview.file);
        // ลบ preview และเพิ่ม URL ที่อัปโหลดเสร็จแล้ว
        const previewIndex = previewUrls.value.findIndex((p) => p.id === preview.id);
        if (previewIndex !== -1) {
          previewUrls.value.splice(previewIndex, 1);
          // ลบ object URL เพื่อป้องกัน memory leak
          URL.revokeObjectURL(preview.url);
        }
        return url;
      } catch (err) {
        // ถ้าอัปโหลดล้มเหลว ให้ลบ preview
        const previewIndex = previewUrls.value.findIndex((p) => p.id === preview.id);
        if (previewIndex !== -1) {
          previewUrls.value.splice(previewIndex, 1);
          URL.revokeObjectURL(preview.url);
        }
        throw err;
      }
    });

    const urls = await Promise.all(uploadPromises);
    // เพิ่ม URLs ที่อัปโหลดเสร็จแล้ว
    imageUrls.value = [...imageUrls.value, ...urls];
  } catch (err: any) {
    const errorMsg =
      err.response?.data?.details ||
      err.response?.data?.message ||
      err.message ||
      "เกิดข้อผิดพลาดในการอัปโหลดรูปภาพ";
    errorMessage.value = errorMsg;
    console.error("Error uploading files:", err);
  } finally {
    isUploading.value = false;
  }
}

/**
 * Cancel upload (ลบ preview)
 */
function cancelUpload(previewId: string) {
  if (isUploading.value) return;
  const previewIndex = previewUrls.value.findIndex((p) => p.id === previewId);
  if (previewIndex !== -1) {
    const preview = previewUrls.value[previewIndex];
    if (preview) {
      URL.revokeObjectURL(preview.url);
      previewUrls.value.splice(previewIndex, 1);
      errorMessage.value = "";
    }
  }
}

/**
 * Remove image
 */
function removeImage(index: number) {
  if (isUploading.value) return;
  imageUrls.value.splice(index, 1);
  errorMessage.value = "";
}

/**
 * Handle image load error
 */
function handleImageError(index: number) {
  // Remove invalid image URL
  imageUrls.value.splice(index, 1);
  errorMessage.value = "ไม่สามารถแสดงรูปภาพนี้ได้";
}
</script>

<style scoped>
@reference "tailwindcss";

/* Image Upload Container */
.image-upload {
  @apply w-full;
}

/* Dropzone */
.image-upload__dropzone {
  @apply relative border-2 border-dashed border-gray-300 rounded-lg p-8 text-center cursor-pointer transition-colors duration-200;
  background-color: #f9fafb;
}

.image-upload__dropzone:hover {
  @apply border-gray-400;
  background-color: #f3f4f6;
}

.image-upload__dropzone--dragover {
  border-color: var(--color-brand-500);
  background-color: var(--color-surface-50);
}

.image-upload__dropzone--disabled {
  @apply opacity-50 cursor-not-allowed;
}

/* File Input (Hidden) */
.image-upload__input {
  @apply hidden;
}

/* Dropzone Content */
.image-upload__content {
  @apply flex flex-col items-center justify-center;
}

.image-upload__icon {
  @apply w-12 h-12 text-gray-400 mb-4;
}

.image-upload__text {
  @apply text-sm text-gray-600 mb-1;
}

.image-upload__text-primary {
  @apply font-medium;
  color: var(--color-brand-600);
}

.image-upload__text-secondary {
  @apply text-xs text-gray-500 mt-2;
}

/* Uploading State */
.image-upload__uploading {
  @apply flex flex-col items-center justify-center;
}

.image-upload__spinner {
  @apply w-8 h-8 border-4 rounded-full animate-spin mb-2;
  border-color: var(--color-surface-100);
  border-top-color: var(--color-brand-600);
}

/* Preview Grid */
.image-upload__preview-grid {
  @apply grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mt-4;
}

.image-upload__preview-item {
  @apply relative aspect-square rounded-lg overflow-hidden border border-gray-200;
}

.image-upload__preview-image {
  @apply w-full h-full object-cover;
}

.image-upload__remove-button {
  @apply absolute top-2 right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center opacity-0 transition-opacity duration-200 hover:bg-red-600;
}

.image-upload__preview-item:hover .image-upload__remove-button {
  @apply opacity-100;
}

.image-upload__remove-button:disabled {
  @apply opacity-50 cursor-not-allowed;
}

.image-upload__remove-icon {
  @apply w-4 h-4;
}

/* Upload Overlay (สำหรับ preview ที่กำลังอัปโหลด) */
.image-upload__upload-overlay {
  @apply absolute inset-0 rounded-lg flex flex-col items-center justify-center;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 10;
}

.image-upload__spinner-small {
  @apply w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin mb-2;
}

.image-upload__upload-text {
  @apply text-white text-xs font-medium;
}

/* Error Message */
.image-upload__error {
  @apply flex items-center gap-2 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg;
}

.image-upload__error-icon {
  @apply w-5 h-5 text-red-500 flex-shrink-0;
}

.image-upload__error-text {
  @apply text-sm text-red-700;
}

/* Responsive */
@media (max-width: 640px) {
  .image-upload__dropzone {
    @apply p-6;
  }

  .image-upload__icon {
    @apply w-10 h-10 mb-3;
  }

  .image-upload__preview-grid {
    @apply grid-cols-2 gap-3;
  }
}
</style>
