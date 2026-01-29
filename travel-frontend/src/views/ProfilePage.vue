<template>
  <div class="profile-page">
    <!-- Navbar -->
    <Navbar />

    <!-- Main Content -->
    <main class="profile-page__main">
      <!-- Header Section -->
      <header class="profile-header">
        <h1 class="profile-title">Profile</h1>
      </header>

      <!-- Loading State -->
      <div v-if="isLoading" class="profile-loading">
        <div class="profile-spinner"></div>
        <p>กำลังโหลดข้อมูล...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="profile-error">
        <p>เกิดข้อผิดพลาด: {{ error }}</p>
        <button @click="fetchProfileData" class="profile-retry-button">
          ลองอีกครั้ง
        </button>
      </div>

      <!-- Profile Content -->
      <div v-else-if="user" class="profile-content">
        <!-- Profile Card -->
        <div class="profile-card">
          <!-- Avatar Section -->
          <div class="profile-avatar-section">
            <div class="profile-avatar">
              <span class="avatar-initials">{{ userInitials }}</span>
            </div>
            <h2 class="profile-display-name">
              {{ user.displayName || "ผู้ใช้" }}
            </h2>
          </div>

          <!-- Profile Information -->
          <div class="profile-info">
            <!-- Display Name -->
            <div class="profile-info-item">
              <div class="profile-info-label">
                <svg
                  class="profile-info-icon"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                  />
                </svg>
                ชื่อที่แสดง
              </div>
              <div class="profile-info-value">
                {{ user.displayName || "ผู้ใช้" }}
              </div>
            </div>

            <!-- Email -->
            <div class="profile-info-item">
              <div class="profile-info-label">
                <svg
                  class="profile-info-icon"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                  />
                </svg>
                อีเมล
              </div>
              <div class="profile-info-value">{{ user.email }}</div>
            </div>

            <!-- Trip Count -->
            <div class="profile-info-item">
              <div class="profile-info-label">
                <svg
                  class="profile-info-icon"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"
                  />
                </svg>
                จำนวนทริป
              </div>
              <div class="profile-info-value">
                {{ tripCount }} {{ tripCount === 1 ? "ทริป" : "ทริป" }}
              </div>
            </div>
          </div>

          <!-- Change Password Section -->
          <div class="profile-change-password">
            <h3 class="profile-change-password-title">เปลี่ยนรหัสผ่าน</h3>
            <form
              @submit.prevent="handleChangePasswordSubmit"
              class="profile-change-password-form"
            >
              <div class="profile-form-group">
                <label for="currentPassword" class="profile-form-label">
                  รหัสผ่านปัจจุบัน
                </label>
                <input
                  id="currentPassword"
                  v-model="passwordForm.currentPassword"
                  type="password"
                  class="profile-form-input"
                  :class="{
                    'profile-form-input--error': passwordErrors.currentPassword,
                  }"
                  placeholder="กรุณากรอกรหัสผ่านปัจจุบัน"
                  autocomplete="current-password"
                  @blur="validateCurrentPassword"
                  @input="clearPasswordError('currentPassword')"
                />
                <p
                  v-if="passwordErrors.currentPassword"
                  class="profile-form-error"
                >
                  {{ passwordErrors.currentPassword }}
                </p>
              </div>
              <div class="profile-form-group">
                <label for="newPassword" class="profile-form-label">
                  รหัสผ่านใหม่
                </label>
                <input
                  id="newPassword"
                  v-model="passwordForm.newPassword"
                  type="password"
                  class="profile-form-input"
                  :class="{
                    'profile-form-input--error': passwordErrors.newPassword,
                  }"
                  placeholder="อย่างน้อย 8 ตัวอักษร"
                  autocomplete="new-password"
                  @blur="validateNewPassword"
                  @input="clearPasswordError('newPassword')"
                />
                <p v-if="passwordErrors.newPassword" class="profile-form-error">
                  {{ passwordErrors.newPassword }}
                </p>
              </div>
              <div class="profile-form-group">
                <label for="confirmPassword" class="profile-form-label">
                  ยืนยันรหัสผ่าน
                </label>
                <input
                  id="confirmPassword"
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  class="profile-form-input"
                  :class="{
                    'profile-form-input--error': passwordErrors.confirmPassword,
                  }"
                  placeholder="กรุณากรอกรหัสผ่านอีกครั้ง"
                  autocomplete="new-password"
                  @blur="validateConfirmPassword"
                  @input="clearPasswordError('confirmPassword')"
                />
                <p
                  v-if="passwordErrors.confirmPassword"
                  class="profile-form-error"
                >
                  {{ passwordErrors.confirmPassword }}
                </p>
              </div>
              <div v-if="passwordServerError" class="profile-form-server-error">
                <p>{{ passwordServerError }}</p>
              </div>
              <div v-if="passwordSuccessMessage" class="profile-form-success">
                <p>{{ passwordSuccessMessage }}</p>
              </div>
              <button
                type="submit"
                class="profile-change-password-button"
                :disabled="!isPasswordFormValid || isPasswordSubmitting"
              >
                <span
                  v-if="isPasswordSubmitting"
                  class="profile-button-spinner"
                ></span>
                <span v-else>เปลี่ยนรหัสผ่าน</span>
              </button>
            </form>
          </div>

          <!-- Action Buttons -->
          <!-- <div class="profile-actions">
            <router-link to="/dashboard" class="profile-action-button">
              <svg
                class="profile-action-icon"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                />
              </svg>
              ไปที่ Dashboard
            </router-link>
          </div> -->
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useAuthStore } from "../stores/auth";
import { getMyTrips } from "../api/trip";
import Navbar from "../components/Navbar.vue";

const authStore = useAuthStore();

const isLoading = ref(false);
const error = ref<string | null>(null);
const tripCount = ref(0);

// Change Password form
const passwordForm = ref({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});
const passwordErrors = ref<Record<string, string>>({});
const passwordServerError = ref<string | null>(null);
const passwordSuccessMessage = ref<string | null>(null);
const isPasswordSubmitting = ref(false);

// Get user from auth store
const user = computed(() => authStore.user);

/**
 * Get user initials for avatar
 */
const userInitials = computed(() => {
  if (!user.value?.displayName) return "U";
  const names = user.value.displayName.trim().split(" ");
  if (names.length >= 2) {
    return (names[0][0] + names[names.length - 1][0]).toUpperCase();
  }
  return user.value.displayName[0].toUpperCase();
});

/**
 * Fetch profile data (trip count)
 */
async function fetchProfileData() {
  isLoading.value = true;
  error.value = null;

  try {
    // Fetch user's trips to count
    const trips = await getMyTrips();
    tripCount.value = trips.length;
  } catch (err: any) {
    error.value =
      err.response?.data?.message ||
      err.message ||
      "เกิดข้อผิดพลาดในการโหลดข้อมูล";
    console.error("Error fetching profile data:", err);
  } finally {
    isLoading.value = false;
  }
}

// --- Change Password validation & submit ---
function validateCurrentPassword() {
  const v = passwordForm.value.currentPassword?.trim() ?? "";
  if (!v) {
    passwordErrors.value.currentPassword = "กรุณากรอกรหัสผ่านปัจจุบัน";
    return;
  }
  delete passwordErrors.value.currentPassword;
}

function validateNewPassword() {
  const v = passwordForm.value.newPassword ?? "";
  if (!v) {
    passwordErrors.value.newPassword = "กรุณากรอกรหัสผ่านใหม่";
    return;
  }
  if (v.length < 8) {
    passwordErrors.value.newPassword = "รหัสผ่านใหม่ต้องมีอย่างน้อย 8 ตัวอักษร";
    return;
  }
  if (v.length > 100) {
    passwordErrors.value.newPassword = "รหัสผ่านใหม่ต้องไม่เกิน 100 ตัวอักษร";
    return;
  }
  delete passwordErrors.value.newPassword;
  if (passwordForm.value.confirmPassword) validateConfirmPassword();
}

function validateConfirmPassword() {
  const newP = passwordForm.value.newPassword ?? "";
  const confirmP = passwordForm.value.confirmPassword ?? "";
  if (!confirmP) {
    passwordErrors.value.confirmPassword = "กรุณายืนยันรหัสผ่าน";
    return;
  }
  if (newP !== confirmP) {
    passwordErrors.value.confirmPassword = "รหัสผ่านไม่ตรงกัน";
    return;
  }
  delete passwordErrors.value.confirmPassword;
}

function clearPasswordError(field: string) {
  delete passwordErrors.value[field];
  passwordServerError.value = null;
}

const isPasswordFormValid = computed(() => {
  const p = passwordForm.value;
  const cur = (p.currentPassword ?? "").trim();
  const newP = p.newPassword ?? "";
  const conf = p.confirmPassword ?? "";
  if (!cur || !newP || !conf) return false;
  if (newP.length < 8 || newP.length > 100) return false;
  if (newP !== conf) return false;
  return true;
});

async function handleChangePasswordSubmit() {
  validateCurrentPassword();
  validateNewPassword();
  validateConfirmPassword();
  if (Object.keys(passwordErrors.value).length > 0) return;

  passwordServerError.value = null;
  passwordSuccessMessage.value = null;
  isPasswordSubmitting.value = true;

  try {
    await authStore.changePassword(
      passwordForm.value.currentPassword.trim(),
      passwordForm.value.newPassword,
    );
    passwordForm.value = {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    };
    passwordSuccessMessage.value = "เปลี่ยนรหัสผ่านเรียบร้อยแล้ว";
  } catch (err: any) {
    const data = err.response?.data;
    const status = err.response?.status;
    // Backend ส่งข้อความใน details (เช่น รหัสปัจจุบันผิด) ไม่ใช่ message
    const details = data?.details;
    const message = data?.message;

    if (status === 401 || status === 403) {
      passwordServerError.value =
        "Session หมดอายุหรือไม่มีสิทธิ์ กรุณาเข้าสู่ระบบใหม่";
    } else if (details || message) {
      passwordServerError.value =
        typeof details === "string" ? details : message || "";
    } else {
      passwordServerError.value =
        err.message || "เกิดข้อผิดพลาดในการเปลี่ยนรหัสผ่าน";
    }
  } finally {
    isPasswordSubmitting.value = false;
  }
}

// Fetch data on mount
onMounted(() => {
  fetchProfileData();
});
</script>

<style scoped>
@reference "tailwindcss";

/* Profile Page */
.profile-page {
  @apply min-h-screen;
  background: var(--color-surface-50);
}

.profile-page__main {
  @apply max-w-4xl mx-auto py-12 px-4;
}

/* Header */
.profile-header {
  @apply mb-8;
}

.profile-title {
  @apply text-[var(--color-brand-600)] m-0;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 2.5rem;
}

/* Loading State */
.profile-loading {
  @apply text-center py-16 px-4;
}

.profile-spinner {
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

.profile-loading p {
  @apply text-sm text-gray-500;
  font-family: var(--font-sans);
}

/* Error State */
.profile-error {
  @apply text-center py-16 px-4;
}

.profile-error p {
  @apply mb-4 text-red-600;
  font-family: var(--font-sans);
}

.profile-retry-button {
  @apply px-6 py-2 rounded-lg font-medium transition-colors duration-200;
  background: var(--color-brand-600);
  color: white;
  font-family: var(--font-sans);
}

.profile-retry-button:hover {
  background: var(--color-brand-800);
}

/* Profile Content */
.profile-content {
  @apply space-y-6;
}

/* Profile Card */
.profile-card {
  @apply bg-white rounded-2xl shadow-lg p-8;
  border: 1px solid #e5e7eb;
}

/* Avatar Section */
.profile-avatar-section {
  @apply text-center mb-8 pb-8 border-b border-gray-200;
}

.profile-avatar {
  @apply w-24 h-24 rounded-full mx-auto mb-4 flex items-center justify-center;
  background: var(--color-brand-600);
  color: white;
}

.avatar-initials {
  @apply text-3xl font-bold;
  font-family: var(--font-display);
}

.profile-display-name {
  @apply text-2xl font-semibold;
  color: var(--color-brand-600);
  font-family: var(--font-display);
}

/* Profile Information */
.profile-info {
  @apply space-y-4 mb-8;
}

.profile-info-item {
  @apply flex items-center justify-between py-4 border-b border-gray-100;
}

.profile-info-item:last-child {
  @apply border-b-0;
}

.profile-info-label {
  @apply flex items-center gap-3 text-sm font-medium;
  color: #6b7280;
  font-family: var(--font-sans);
}

.profile-info-icon {
  @apply w-5 h-5;
}

.profile-info-value {
  @apply text-base font-medium;
  color: #1f2937;
  font-family: var(--font-sans);
}

/* Profile Actions */
.profile-actions {
  @apply flex justify-center;
}

.profile-action-button {
  @apply flex items-center gap-2 px-6 py-3 rounded-lg font-medium transition-colors duration-200 no-underline;
  background: var(--color-brand-600);
  color: white;
  font-family: var(--font-sans);
}

.profile-action-button:hover {
  background: var(--color-brand-800);
}

.profile-action-icon {
  @apply w-5 h-5;
}

/* Change Password Section */
.profile-change-password {
  @apply pt-8 mt-8 border-t border-gray-200;
}

.profile-change-password-title {
  @apply text-lg font-semibold mb-4;
  color: var(--color-brand-600);
  font-family: var(--font-display);
}

.profile-change-password-form {
  @apply space-y-4 max-w-md;
}

.profile-form-group {
  @apply flex flex-col gap-1;
}

.profile-form-label {
  @apply text-sm font-medium;
  color: #374151;
  font-family: var(--font-sans);
}

.profile-form-input {
  @apply w-full px-4 py-2.5 rounded-lg border border-gray-300 text-gray-900;
  font-family: var(--font-sans);
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
}

.profile-form-input:focus {
  @apply outline-none;
  border-color: var(--color-brand-600);
  box-shadow: 0 0 0 3px rgba(95, 75, 139, 0.15);
}

.profile-form-input--error {
  @apply border-red-500;
}

.profile-form-error {
  @apply text-sm text-red-600;
  font-family: var(--font-sans);
}

.profile-form-server-error {
  @apply p-3 rounded-lg bg-red-50 border border-red-200;
}

.profile-form-server-error p {
  @apply text-sm text-red-700;
  font-family: var(--font-sans);
}

.profile-form-success {
  @apply p-3 rounded-lg bg-green-50 border border-green-200;
}

.profile-form-success p {
  @apply text-sm text-green-700;
  font-family: var(--font-sans);
}

.profile-change-password-button {
  @apply flex items-center justify-center gap-2 px-6 py-3 rounded-lg font-medium transition-colors duration-200;
  background: var(--color-brand-600);
  color: white;
  font-family: var(--font-sans);
}

.profile-change-password-button:hover:not(:disabled) {
  background: var(--color-brand-800);
}

.profile-change-password-button:disabled {
  @apply opacity-60 cursor-not-allowed;
}

.profile-button-spinner {
  @apply inline-block w-5 h-5 rounded-full border-2 border-white border-t-transparent;
  animation: spin 0.8s linear infinite;
}

/* Responsive Design */
@media (max-width: 768px) {
  .profile-page__main {
    @apply py-8 px-4;
  }

  .profile-title {
    font-size: 2rem;
  }

  .profile-card {
    @apply p-6;
  }

  .profile-avatar {
    @apply w-20 h-20;
  }

  .avatar-initials {
    @apply text-2xl;
  }

  .profile-display-name {
    @apply text-xl;
  }

  .profile-info-item {
    @apply flex-col items-start gap-2;
  }
}
</style>
