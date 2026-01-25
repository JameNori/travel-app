<template>
  <div class="dashboard-page">
    <!-- Navbar -->
    <Navbar />

    <!-- Main Content -->
    <main class="dashboard-page__main">
      <!-- Header Section -->
      <header class="dashboard-header">
        <h1 class="dashboard-title">Dashboard</h1>
        <button
          type="button"
          class="dashboard-create-button"
          @click="openCreateModal"
        >
          <svg
            class="create-button-icon"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 4v16m8-8H4"
            />
          </svg>
          สร้างทริปใหม่
        </button>
      </header>

      <!-- Loading State -->
      <div v-if="isLoading" class="dashboard-loading">
        <div class="dashboard-spinner"></div>
        <p>กำลังโหลดทริป...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="dashboard-error">
        <p>เกิดข้อผิดพลาด: {{ error }}</p>
        <button @click="fetchMyTrips" class="dashboard-retry-button">
          ลองอีกครั้ง
        </button>
      </div>

      <!-- Empty State -->
      <div v-else-if="trips.length === 0" class="dashboard-empty">
        <svg
          class="empty-icon"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
        <p class="empty-title">ยังไม่มีทริป</p>
        <p class="empty-description">เริ่มสร้างทริปแรกของคุณเลย!</p>
        <button
          type="button"
          class="dashboard-create-button dashboard-create-button--center"
          @click="openCreateModal"
        >
          <svg
            class="create-button-icon"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 4v16m8-8H4"
            />
          </svg>
          สร้างทริปใหม่
        </button>
      </div>

      <!-- Trip List -->
      <div v-else class="dashboard-trips">
        <TripCard
          v-for="trip in trips"
          :key="trip.id"
          :trip="trip"
          :show-actions="true"
          :show-meta="true"
          from="dashboard"
          :truncate-length="100"
          @edit="openEditModal"
          @delete="openDeleteDialog"
        />
      </div>
    </main>

    <!-- Create/Edit Trip Modal -->
    <transition name="modal">
      <div
        v-if="isModalOpen"
        class="modal-overlay"
        @click="closeModal"
      >
        <div class="modal-container" @click.stop>
          <div class="modal-header">
            <h2 class="modal-title">
              {{ isEditMode ? "แก้ไขทริป" : "สร้างทริปใหม่" }}
            </h2>
            <button
              type="button"
              class="modal-close-button"
              @click="closeModal"
              aria-label="ปิด"
            >
              <svg
                class="close-icon"
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

          <form @submit.prevent="handleSubmit" class="modal-form">
            <!-- Title -->
            <div class="form-group">
              <label for="title" class="form-label">
                ชื่อทริป <span class="required">*</span>
              </label>
              <input
                id="title"
                v-model="formData.title"
                type="text"
                class="form-input"
                placeholder="เช่น เที่ยวฟินแลนด์"
                required
              />
            </div>

            <!-- Description -->
            <div class="form-group">
              <label for="description" class="form-label">รายละเอียด</label>
              <textarea
                id="description"
                v-model="formData.description"
                class="form-textarea"
                rows="4"
                placeholder="อธิบายรายละเอียดทริป..."
              ></textarea>
            </div>

            <!-- Photos (File Upload) -->
            <div class="form-group">
              <label class="form-label">รูปภาพ</label>
              <ImageUpload
                v-model="photosArray"
                :max-files="10"
              />
            </div>

            <!-- Tags -->
            <div class="form-group">
              <label for="tags" class="form-label">แท็ก</label>
              <div
                v-for="(_, index) in formData.tags"
                :key="index"
                class="form-tag-input"
              >
                <input
                  v-model="formData.tags![index]"
                  type="text"
                  class="form-input"
                  placeholder="เช่น ธรรมชาติ, ทะเล"
                />
                <button
                  type="button"
                  class="form-remove-button"
                  @click="removeTag(index)"
                  aria-label="ลบแท็ก"
                >
                  <svg
                    class="remove-icon"
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
              <button
                type="button"
                class="form-add-button"
                @click="addTag"
              >
                + เพิ่มแท็ก
              </button>
            </div>

            <!-- Latitude -->
            <div class="form-group">
              <label for="latitude" class="form-label">
                ละติจูด <span class="required">*</span>
              </label>
              <input
                id="latitude"
                v-model.number="formData.latitude"
                type="number"
                step="any"
                class="form-input"
                placeholder="เช่น 13.7563"
                required
              />
            </div>

            <!-- Longitude -->
            <div class="form-group">
              <label for="longitude" class="form-label">
                ลองจิจูด <span class="required">*</span>
              </label>
              <input
                id="longitude"
                v-model.number="formData.longitude"
                type="number"
                step="any"
                class="form-input"
                placeholder="เช่น 100.5018"
                required
              />
            </div>

            <!-- Form Actions -->
            <div class="modal-actions">
              <button
                type="button"
                class="modal-button modal-button--cancel"
                @click="closeModal"
              >
                ยกเลิก
              </button>
              <button
                type="submit"
                class="modal-button modal-button--submit"
                :disabled="isSubmitting"
              >
                <span v-if="isSubmitting">กำลังบันทึก...</span>
                <span v-else>{{ isEditMode ? "บันทึก" : "สร้าง" }}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </transition>

    <!-- Close Modal Confirmation Dialog -->
    <transition name="modal">
      <div
        v-if="showCloseConfirmation"
        class="modal-overlay"
        @click="cancelCloseModal"
      >
        <div class="delete-dialog" @click.stop>
          <h3 class="delete-dialog-title">ยืนยันการปิด</h3>
          <p class="delete-dialog-message">
            คุณมีข้อมูลที่ยังไม่ได้บันทึก
            <br />
            คุณแน่ใจหรือไม่ว่าต้องการปิดฟอร์มนี้?
          </p>
          <div class="delete-dialog-actions">
            <button
              type="button"
              class="delete-button delete-button--cancel"
              @click="cancelCloseModal"
            >
              ยกเลิก
            </button>
            <button
              type="button"
              class="delete-button delete-button--confirm"
              @click="forceCloseModal"
            >
              ปิดฟอร์ม
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- Delete Confirmation Dialog -->
    <transition name="modal">
      <div
        v-if="isDeleteDialogOpen"
        class="modal-overlay"
        @click="closeDeleteDialog"
      >
        <div class="delete-dialog" @click.stop>
          <h3 class="delete-dialog-title">ยืนยันการลบ</h3>
          <p class="delete-dialog-message">
            คุณแน่ใจหรือไม่ว่าต้องการลบทริป "{{ tripToDelete?.title }}"?
            <br />
            การกระทำนี้ไม่สามารถยกเลิกได้
          </p>
          <div class="delete-dialog-actions">
            <button
              type="button"
              class="delete-button delete-button--cancel"
              @click="closeDeleteDialog"
            >
              ยกเลิก
            </button>
            <button
              type="button"
              class="delete-button delete-button--confirm"
              @click="handleDelete"
              :disabled="isDeleting"
            >
              <span v-if="isDeleting">กำลังลบ...</span>
              <span v-else>ลบ</span>
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- Error Dialog -->
    <ErrorDialog
      :is-open="isErrorDialogOpen"
      :message="errorMessage"
      @close="closeErrorDialog"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { getMyTrips, createTrip, updateTrip, deleteTrip } from "../api/trip";
import type { Trip, TripRequest } from "../api/trip";
import Navbar from "../components/Navbar.vue";
import ErrorDialog from "../components/ErrorDialog.vue";
import ImageUpload from "../components/ImageUpload.vue";
import TripCard from "../components/TripCard.vue";

// State
const trips = ref<Trip[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);

// Modal state
const isModalOpen = ref(false);
const isEditMode = ref(false);
const editingTripId = ref<number | null>(null);
const isSubmitting = ref(false);

// Delete dialog state
const isDeleteDialogOpen = ref(false);
const tripToDelete = ref<Trip | null>(null);
const isDeleting = ref(false);

// Modal close confirmation state
const showCloseConfirmation = ref(false);

// Error dialog state
const isErrorDialogOpen = ref(false);
const errorMessage = ref("");

// Form data
const formData = ref<TripRequest>({
  title: "",
  description: "",
  photos: [] as string[],
  tags: [] as string[],
  latitude: 0,
  longitude: 0,
});

// Computed property for photos to ensure it's always an array
const photosArray = computed({
  get: () => formData.value.photos || [],
  set: (value: string[]) => {
    formData.value.photos = value;
  },
});

/**
 * Fetch my trips from API
 */
async function fetchMyTrips() {
  isLoading.value = true;
  error.value = null;

  try {
    const data = await getMyTrips();
    trips.value = data;
  } catch (err: any) {
    error.value =
      err.response?.data?.message ||
      err.message ||
      "เกิดข้อผิดพลาดในการโหลดทริป";
    console.error("Error fetching my trips:", err);
  } finally {
    isLoading.value = false;
  }
}

/**
 * Open create modal
 */
function openCreateModal() {
  isEditMode.value = false;
  editingTripId.value = null;
  resetForm();
  isModalOpen.value = true;
}

/**
 * Open edit modal
 */
function openEditModal(trip: Trip) {
  isEditMode.value = true;
  editingTripId.value = trip.id;
  formData.value = {
    title: trip.title,
    description: trip.description || "",
    photos: trip.photos ? [...trip.photos] : [],
    tags: trip.tags ? [...trip.tags] : [],
    latitude: trip.latitude || 0,
    longitude: trip.longitude || 0,
  };
  isModalOpen.value = true;
}

/**
 * Check if form has unsaved changes
 */
function hasUnsavedChanges(): boolean {
  // Check if any field has been filled
  if (formData.value.title.trim() !== "") return true;
  if (formData.value.description?.trim() !== "") return true;
  if (formData.value.photos && formData.value.photos.some((p) => p.trim() !== "")) return true;
  if (formData.value.tags && formData.value.tags.some((t) => t.trim() !== "")) return true;
  if (formData.value.latitude !== 0) return true;
  if (formData.value.longitude !== 0) return true;
  return false;
}

/**
 * Close modal with confirmation if needed
 */
function closeModal() {
  if (hasUnsavedChanges()) {
    // Show confirmation dialog
    showCloseConfirmation.value = true;
  } else {
    // No changes, close immediately
    forceCloseModal();
  }
}

/**
 * Force close modal (after confirmation)
 */
function forceCloseModal() {
  isModalOpen.value = false;
  showCloseConfirmation.value = false;
  resetForm();
}

/**
 * Cancel close modal (user chose to continue editing)
 */
function cancelCloseModal() {
  showCloseConfirmation.value = false;
}

/**
 * Reset form data
 */
function resetForm() {
  formData.value = {
    title: "",
    description: "",
    photos: [],
    tags: [],
    latitude: 0,
    longitude: 0,
  };
}


/**
 * Add tag input
 */
function addTag() {
  formData.value.tags = formData.value.tags || [];
  formData.value.tags.push("");
}

/**
 * Remove tag input
 */
function removeTag(index: number) {
  if (formData.value.tags) {
    formData.value.tags.splice(index, 1);
  }
}

/**
 * Format error message from backend response
 */
function formatErrorMessage(err: any): string {
  const response = err.response?.data;
  
  if (!response) {
    return err.message || "เกิดข้อผิดพลาดที่ไม่ทราบสาเหตุ";
  }

  // If there are validation details (from GlobalExceptionHandler)
  if (response.details && typeof response.details === "object") {
    const details = response.details;
    const errorMessages: string[] = [];
    
    // Format field errors
    Object.keys(details).forEach((field) => {
      const fieldName = getFieldDisplayName(field);
      errorMessages.push(`${fieldName}: ${details[field]}`);
    });
    
    if (errorMessages.length > 0) {
      return `กรุณาตรวจสอบข้อมูลต่อไปนี้:\n\n${errorMessages.join("\n")}`;
    }
  }

  // Fallback to error message or details
  return response.message || response.details || response.error || "เกิดข้อผิดพลาด";
}

/**
 * Get display name for form field
 */
function getFieldDisplayName(field: string): string {
  const fieldNames: Record<string, string> = {
    title: "ชื่อทริป",
    description: "รายละเอียด",
    latitude: "ละติจูด",
    longitude: "ลองจิจูด",
    photos: "รูปภาพ",
    tags: "แท็ก",
  };
  return fieldNames[field] || field;
}

/**
 * Handle form submit (create or update)
 */
async function handleSubmit() {
  isSubmitting.value = true;

  try {
    // Clean up empty strings
    const cleanPhotos = formData.value.photos?.filter((p) => p.trim() !== "") || [];
    const cleanTags = formData.value.tags?.filter((t) => t.trim() !== "") || [];

    const tripData: TripRequest = {
      title: formData.value.title.trim(),
      description: formData.value.description?.trim() || undefined,
      photos: cleanPhotos.length > 0 ? cleanPhotos : undefined,
      tags: cleanTags.length > 0 ? cleanTags : undefined,
      latitude: formData.value.latitude,
      longitude: formData.value.longitude,
    };

    if (isEditMode.value && editingTripId.value) {
      // Update trip
      await updateTrip(editingTripId.value, tripData);
    } else {
      // Create trip
      await createTrip(tripData);
    }

    // Refresh trips list
    await fetchMyTrips();
    forceCloseModal();
  } catch (err: any) {
    const formattedError = formatErrorMessage(err);
    error.value = formattedError;
    errorMessage.value = formattedError;
    console.error("Error saving trip:", err);
    console.error("Error response:", err.response?.data);
    isErrorDialogOpen.value = true;
  } finally {
    isSubmitting.value = false;
  }
}

/**
 * Open delete confirmation dialog
 */
function openDeleteDialog(trip: Trip) {
  tripToDelete.value = trip;
  isDeleteDialogOpen.value = true;
}

/**
 * Close delete dialog
 */
function closeDeleteDialog() {
  isDeleteDialogOpen.value = false;
  tripToDelete.value = null;
}

/**
 * Handle delete trip
 */
async function handleDelete() {
  if (!tripToDelete.value) return;

  isDeleting.value = true;

  try {
    await deleteTrip(tripToDelete.value.id);
    await fetchMyTrips();
    closeDeleteDialog();
  } catch (err: any) {
    const formattedError = formatErrorMessage(err);
    error.value = formattedError;
    errorMessage.value = formattedError;
    console.error("Error deleting trip:", err);
    console.error("Error response:", err.response?.data);
    isErrorDialogOpen.value = true;
  } finally {
    isDeleting.value = false;
  }
}

/**
 * Close error dialog
 */
function closeErrorDialog() {
  isErrorDialogOpen.value = false;
  errorMessage.value = "";
}


// Fetch trips on mount
onMounted(() => {
  fetchMyTrips();
});
</script>

<style scoped>
@reference "tailwindcss";

/* Dashboard Page */
.dashboard-page {
  @apply min-h-screen;
  background: var(--color-surface-50);
}

.dashboard-page__main {
  @apply max-w-6xl mx-auto py-12 px-4;
}

/* Header */
.dashboard-header {
  @apply flex justify-between items-center mb-8;
}

.dashboard-title {
  @apply text-[var(--color-brand-600)] m-0;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 2.5rem;
}

.dashboard-create-button {
  @apply flex items-center gap-2 px-6 py-3 rounded-lg font-medium transition-colors duration-200;
  background: var(--color-brand-600);
  color: white;
  font-family: var(--font-sans);
}

.dashboard-create-button:hover {
  background: var(--color-brand-800);
}

.dashboard-create-button--center {
  @apply mx-auto;
}

.create-button-icon {
  @apply w-5 h-5;
}

/* Loading State */
.dashboard-loading {
  @apply text-center py-16 px-4;
}

.dashboard-spinner {
  @apply w-12 h-12 rounded-full mx-auto mb-4;
  border: 4px solid var(--color-surface-200);
  border-top-color: var(--color-brand-600);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Error State */
.dashboard-error {
  @apply text-center py-16 px-4;
}

.dashboard-retry-button {
  @apply mt-4 px-6 py-2 rounded-lg font-medium transition-colors duration-200;
  background: var(--color-brand-600);
  color: white;
  font-family: var(--font-sans);
}

.dashboard-retry-button:hover {
  background: var(--color-brand-800);
}

/* Empty State */
.dashboard-empty {
  @apply text-center py-16 px-4;
}

.empty-icon {
  @apply w-16 h-16 mx-auto mb-4;
  color: #9ca3af;
}

.empty-title {
  @apply text-xl font-semibold mb-2;
  color: #1f2937;
  font-family: var(--font-display);
}

.empty-description {
  @apply text-gray-500 mb-6;
  font-family: var(--font-sans);
}

/* Trip List */
.dashboard-trips {
  @apply space-y-6;
}

/* Modal */
.modal-overlay {
  @apply fixed inset-0 z-50 flex items-center justify-center p-4;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-container {
  @apply bg-white rounded-2xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto;
}

.modal-header {
  @apply flex justify-between items-center p-6 border-b border-gray-200;
}

.modal-title {
  @apply text-xl font-semibold;
  color: var(--color-brand-600);
  font-family: var(--font-display);
}

.modal-close-button {
  @apply p-2 rounded-lg transition-colors duration-200;
  color: #6b7280;
}

.modal-close-button:hover {
  @apply bg-gray-100;
}

.close-icon {
  @apply w-5 h-5;
}

.modal-form {
  @apply p-6 space-y-4;
}

.form-group {
  @apply space-y-2;
}

.form-label {
  @apply block text-sm font-medium;
  color: #1f2937;
  font-family: var(--font-sans);
}

.required {
  @apply text-red-500;
}

.form-input,
.form-textarea {
  @apply w-full px-4 py-2 border border-gray-300 rounded-lg transition-colors duration-200;
  font-family: var(--font-sans);
}

.form-input:focus,
.form-textarea:focus {
  @apply outline-none border-[var(--color-brand-600)];
  box-shadow: 0 0 0 3px rgba(95, 75, 139, 0.1);
}

.form-textarea {
  @apply resize-none;
}

.form-photo-input,
.form-tag-input {
  @apply flex gap-2 mb-2;
}

.form-remove-button {
  @apply p-2 rounded-lg transition-colors duration-200;
  color: #6b7280;
}

.form-remove-button:hover {
  @apply bg-gray-100;
}

.remove-icon {
  @apply w-4 h-4;
}

.form-add-button {
  @apply px-4 py-2 rounded-lg font-medium transition-colors duration-200;
  background: var(--color-surface-100);
  color: var(--color-brand-600);
  font-family: var(--font-sans);
}

.form-add-button:hover {
  @apply bg-[var(--color-surface-200)];
}

.modal-actions {
  @apply flex justify-end gap-3 pt-4 border-t border-gray-200;
}

.modal-button {
  @apply px-6 py-2 rounded-lg font-medium transition-colors duration-200;
  font-family: var(--font-sans);
}

.modal-button--cancel {
  @apply bg-gray-100 text-gray-700;
}

.modal-button--cancel:hover {
  @apply bg-gray-200;
}

.modal-button--submit {
  background: var(--color-brand-600);
  color: white;
}

.modal-button--submit:hover {
  background: var(--color-brand-800);
}

.modal-button--submit:disabled {
  @apply opacity-50 cursor-not-allowed;
}

/* Delete Dialog */
.delete-dialog {
  @apply bg-white rounded-2xl shadow-xl max-w-md w-full p-6;
}

.delete-dialog-title {
  @apply text-xl font-semibold mb-4;
  color: #1f2937;
  font-family: var(--font-display);
}

.delete-dialog-message {
  @apply text-gray-600 mb-6;
  font-family: var(--font-sans);
  line-height: 1.6;
}

.delete-dialog-actions {
  @apply flex justify-end gap-3;
}

.delete-button {
  @apply px-6 py-2 rounded-lg font-medium transition-colors duration-200;
  font-family: var(--font-sans);
}

.delete-button--cancel {
  @apply bg-gray-100 text-gray-700;
}

.delete-button--cancel:hover {
  @apply bg-gray-200;
}

.delete-button--confirm {
  @apply bg-red-600 text-white;
}

.delete-button--confirm:hover {
  @apply bg-red-700;
}

.delete-button--confirm:disabled {
  @apply opacity-50 cursor-not-allowed;
}

/* Modal Transitions */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-container,
.modal-enter-active .delete-dialog,
.modal-leave-active .modal-container,
.modal-leave-active .delete-dialog {
  transition: transform 0.2s ease;
}

.modal-enter-from .modal-container,
.modal-enter-from .delete-dialog,
.modal-leave-to .modal-container,
.modal-leave-to .delete-dialog {
  transform: scale(0.95);
}

/* Responsive */
@media (max-width: 768px) {
  .dashboard-header {
    @apply flex-col gap-4 items-start;
  }

  .dashboard-title {
    font-size: 2rem;
  }

  .modal-container {
    @apply max-w-full;
  }
}
</style>
