<template>
  <div class="register-page">
    <!-- Header Section -->
    <header class="header">
      <h1 class="main-title">สมัครสมาชิก</h1>
    </header>

    <!-- Main Content -->
    <main class="register-page__main">
      <div class="register-card">
        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="register-form">
          <!-- Display Name -->
          <div class="form-group">
            <label for="displayName" class="form-label">ชื่อที่แสดง</label>
            <input
              id="displayName"
              v-model="formData.displayName"
              type="text"
              class="form-input"
              :class="{ 'form-input--error': errors.displayName }"
              placeholder="กรุณากรอกชื่อที่แสดง"
              @blur="validateDisplayName"
              @input="clearError('displayName')"
            />
            <p v-if="errors.displayName" class="form-error">
              {{ errors.displayName }}
            </p>
          </div>

          <!-- Email -->
          <div class="form-group">
            <label for="email" class="form-label">อีเมล</label>
            <input
              id="email"
              v-model="formData.email"
              type="email"
              class="form-input"
              :class="{ 'form-input--error': errors.email }"
              placeholder="example@email.com"
              @blur="validateEmail"
              @input="clearError('email')"
            />
            <p v-if="errors.email" class="form-error">
              {{ errors.email }}
            </p>
          </div>

          <!-- Password -->
          <div class="form-group">
            <label for="password" class="form-label">รหัสผ่าน</label>
            <input
              id="password"
              v-model="formData.password"
              type="password"
              class="form-input"
              :class="{ 'form-input--error': errors.password }"
              placeholder="กรุณากรอกรหัสผ่าน"
              @blur="validatePassword"
              @input="clearError('password')"
            />
            <p v-if="errors.password" class="form-error">
              {{ errors.password }}
            </p>
            <p
              v-if="passwordHint && !errors.password && formData.password"
              class="form-hint"
            >
              {{ passwordHint }}
            </p>
          </div>

          <!-- Confirm Password -->
          <div class="form-group">
            <label for="confirmPassword" class="form-label"
              >ยืนยันรหัสผ่าน</label
            >
            <input
              id="confirmPassword"
              v-model="formData.confirmPassword"
              type="password"
              class="form-input"
              :class="{ 'form-input--error': errors.confirmPassword }"
              placeholder="กรุณากรอกรหัสผ่านอีกครั้ง"
              @blur="validateConfirmPassword"
              @input="clearError('confirmPassword')"
            />
            <p v-if="errors.confirmPassword" class="form-error">
              {{ errors.confirmPassword }}
            </p>
          </div>

          <!-- Server Error -->
          <div v-if="serverError" class="form-server-error">
            <p>{{ serverError }}</p>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            class="submit-button"
            :disabled="isSubmitting || !isFormValid"
          >
            <span v-if="isSubmitting" class="button-spinner"></span>
            <span v-else>สมัครสมาชิก</span>
          </button>

          <!-- Login Link -->
          <div class="form-footer">
            <p class="form-footer-text">
              มีบัญชีอยู่แล้ว?
              <router-link to="/login" class="form-footer-link">
                เข้าสู่ระบบ
              </router-link>
            </p>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const authStore = useAuthStore();

// Form Data
const formData = ref({
  displayName: "",
  email: "",
  password: "",
  confirmPassword: "",
});

// Validation Errors
const errors = ref<Record<string, string>>({});
const serverError = ref<string | null>(null);
const isSubmitting = ref(false);
const passwordHint = ref<string | null>(null);

/**
 * Validate Display Name
 */
function validateDisplayName() {
  const value = formData.value.displayName.trim();
  if (!value) {
    errors.value.displayName = "กรุณากรอกชื่อที่แสดง";
    return false;
  }
  if (value.length < 2) {
    errors.value.displayName = "ชื่อที่แสดงต้องมีอย่างน้อย 2 ตัวอักษร";
    return false;
  }
  if (value.length > 50) {
    errors.value.displayName = "ชื่อที่แสดงต้องไม่เกิน 50 ตัวอักษร";
    return false;
  }
  delete errors.value.displayName;
  return true;
}

/**
 * Validate Email
 */
function validateEmail() {
  const value = formData.value.email.trim();
  if (!value) {
    errors.value.email = "กรุณากรอกอีเมล";
    return false;
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(value)) {
    errors.value.email = "รูปแบบอีเมลไม่ถูกต้อง";
    return false;
  }
  delete errors.value.email;
  return true;
}

/**
 * Calculate Password Strength
 */
function calculatePasswordStrength(
  password: string
): "weak" | "medium" | "strong" {
  if (!password) return "weak";

  let score = 0;
  const hasUpperCase = /[A-Z]/.test(password);
  const hasLowerCase = /[a-z]/.test(password);
  const hasNumber = /[0-9]/.test(password);
  const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>\[\]\\\/_+\-=~`]/.test(password);

  if (hasUpperCase) score++;
  if (hasLowerCase) score++;
  if (hasNumber) score++;
  if (hasSpecialChar) score++;
  if (password.length >= 12) score++;

  if (score <= 2) return "weak";
  if (score <= 3) return "medium";
  return "strong";
}

/**
 * Validate Password
 */
function validatePassword() {
  const value = formData.value.password;
  if (!value) {
    errors.value.password = "กรุณากรอกรหัสผ่าน";
    passwordHint.value = null;
    return false;
  }
  if (value.length < 8) {
    errors.value.password = "รหัสผ่านต้องมีอย่างน้อย 8 ตัวอักษร";
    passwordHint.value = null;
    return false;
  }
  if (value.length > 100) {
    errors.value.password = "รหัสผ่านต้องไม่เกิน 100 ตัวอักษร";
    passwordHint.value = null;
    return false;
  }

  // Calculate password strength and show hint
  const strength = calculatePasswordStrength(value);
  if (strength === "weak") {
    passwordHint.value =
      "แนะนำ: เพิ่มตัวพิมพ์ใหญ่, ตัวเลข, หรืออักขระพิเศษเพื่อความปลอดภัย";
  } else if (strength === "medium") {
    passwordHint.value = "แนะนำ: เพิ่มอักขระพิเศษเพื่อความปลอดภัยมากขึ้น";
  } else {
    passwordHint.value = null;
  }

  delete errors.value.password;
  return true;
}

/**
 * Validate Confirm Password
 */
function validateConfirmPassword() {
  const value = formData.value.confirmPassword;
  if (!value) {
    errors.value.confirmPassword = "กรุณายืนยันรหัสผ่าน";
    return false;
  }
  if (value !== formData.value.password) {
    errors.value.confirmPassword = "รหัสผ่านไม่ตรงกัน";
    return false;
  }
  delete errors.value.confirmPassword;
  return true;
}

/**
 * Clear Error
 */
function clearError(field: string) {
  delete errors.value[field];
  if (field === "password") {
    passwordHint.value = null;
  }
  if (serverError.value) {
    serverError.value = null;
  }
}

/**
 * Validate All Fields
 */
function validateAll(): boolean {
  const isValid =
    validateDisplayName() &&
    validateEmail() &&
    validatePassword() &&
    validateConfirmPassword();
  return isValid;
}

/**
 * Check if form is valid
 */
const isFormValid = computed(() => {
  return (
    formData.value.displayName.trim() &&
    formData.value.email.trim() &&
    formData.value.password &&
    formData.value.confirmPassword &&
    Object.keys(errors.value).length === 0
  );
});

/**
 * Handle Form Submit
 */
async function handleSubmit() {
  // Clear previous errors
  serverError.value = null;

  // Validate all fields
  if (!validateAll()) {
    return;
  }

  isSubmitting.value = true;

  try {
    // Call register API
    await authStore.register(
      formData.value.email.trim(),
      formData.value.password,
      formData.value.displayName.trim()
    );

    // Redirect to landing page after successful registration
    router.push("/");
  } catch (error: any) {
    // Handle error from backend
    const errorMessage =
      error.response?.data?.message ||
      error.message ||
      "เกิดข้อผิดพลาดในการสมัครสมาชิก";
    serverError.value = errorMessage;
    console.error("Registration error:", error);
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
@reference "tailwindcss";

/* Register Page Container */
.register-page {
  @apply min-h-screen;
  background: var(--color-surface-50); /* Cloud Lilac */
}

/* Header Section */
.header {
  @apply text-center py-8 px-4 bg-white;
}

.main-title {
  @apply mb-6 text-[var(--color-brand-600)];
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 2.5rem;
  line-height: 1.2;
}

/* Main Content */
.register-page__main {
  @apply max-w-md mx-auto py-12 px-4;
}

/* Register Card */
.register-card {
  @apply bg-white rounded-2xl shadow-lg p-8;
  border: 1px solid #e5e7eb;
}

/* Form */
.register-form {
  @apply flex flex-col gap-6;
}

/* Form Group */
.form-group {
  @apply flex flex-col;
}

.form-label {
  @apply mb-2 text-sm font-medium;
  color: #374151; /* Text Primary */
  font-family: var(--font-sans);
}

/* Form Input */
.form-input {
  @apply w-full py-3 px-4 border rounded-lg text-base outline-none transition-colors duration-200;
  border-color: #d1d5db; /* Border Gray */
  color: #1f2937; /* Text Primary */
  font-family: var(--font-sans);
  background: #ffffff;
}

.form-input:focus {
  border-color: var(--color-brand-500); /* Neon Orchid */
  outline: 2px solid var(--color-brand-500);
  outline-offset: 2px;
}

.form-input::placeholder {
  color: #9ca3af; /* Placeholder Gray */
}

.form-input--error {
  border-color: var(--color-signal-warm-500); /* Sunset Clay */
}

.form-input--error:focus {
  border-color: var(--color-signal-warm-500);
  outline-color: var(--color-signal-warm-500);
}

/* Form Error */
.form-error {
  @apply mt-1 text-sm;
  color: var(--color-signal-warm-500); /* Sunset Clay */
  font-family: var(--font-sans);
}

/* Form Hint */
.form-hint {
  @apply mt-1 text-sm;
  color: #6b7280; /* Text Secondary */
  font-family: var(--font-sans);
}

/* Server Error */
.form-server-error {
  @apply p-4 rounded-lg bg-red-50 border border-red-200;
}

.form-server-error p {
  @apply text-sm;
  color: var(--color-signal-warm-500); /* Sunset Clay */
  font-family: var(--font-sans);
}

/* Submit Button */
.submit-button {
  @apply w-full py-3 px-6 rounded-lg text-base font-medium text-white cursor-pointer transition-colors duration-200 flex items-center justify-center;
  background: var(--color-brand-600); /* Royal Violet */
  font-family: var(--font-sans);
  min-height: 48px;
}

.submit-button:hover:not(:disabled) {
  background: var(--color-brand-800); /* Aubergine Ink */
}

.submit-button:disabled {
  @apply opacity-50 cursor-not-allowed;
}

/* Button Spinner */
.button-spinner {
  @apply w-5 h-5 border-2 border-white border-t-transparent rounded-full;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Form Footer */
.form-footer {
  @apply text-center mt-4;
}

.form-footer-text {
  @apply text-sm;
  color: #6b7280; /* Text Secondary */
  font-family: var(--font-sans);
}

.form-footer-link {
  @apply font-medium no-underline transition-colors duration-200;
  color: var(--color-brand-600); /* Royal Violet */
}

.form-footer-link:hover {
  color: var(--color-brand-800); /* Aubergine Ink */
}

/* Responsive Design */
@media (max-width: 768px) {
  .header {
    @apply py-6 px-4;
  }

  .main-title {
    @apply mb-4;
    font-size: 1.875rem; /* 30px */
  }

  .register-page__main {
    @apply py-8 px-4;
  }

  .register-card {
    @apply p-6;
  }
}
</style>
