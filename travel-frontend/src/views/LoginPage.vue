<template>
  <div class="login-page">
    <!-- Header Section -->
    <header class="header">
      <h1 class="main-title">เข้าสู่ระบบ</h1>
    </header>

    <!-- Main Content -->
    <main class="login-page__main">
      <div class="login-card">
        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="login-form">
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
          </div>

          <!-- Server Error -->
          <div v-if="serverError" class="form-server-error">
            <p>{{ serverError }}</p>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            class="submit-button"
            :disabled="!isFormValid || isSubmitting"
          >
            <span v-if="isSubmitting" class="button-spinner"></span>
            <span v-else>เข้าสู่ระบบ</span>
          </button>
        </form>

        <!-- Form Footer -->
        <div class="form-footer">
          <p class="form-footer-text">
            ยังไม่มีบัญชี?
            <router-link to="/register" class="form-footer-link">
              สมัครสมาชิก
            </router-link>
          </p>
        </div>
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

const formData = ref({
  email: "",
  password: "",
});

const errors = ref<Record<string, string>>({});
const serverError = ref<string | null>(null);
const isSubmitting = ref(false);

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
 * Validate Password
 */
function validatePassword() {
  const value = formData.value.password;
  if (!value) {
    errors.value.password = "กรุณากรอกรหัสผ่าน";
    return false;
  }
  delete errors.value.password;
  return true;
}

/**
 * Clear Error
 */
function clearError(field: string) {
  delete errors.value[field];
  if (serverError.value) {
    serverError.value = null;
  }
}

/**
 * Validate All Fields
 */
function validateAll(): boolean {
  return validateEmail() && validatePassword();
}

/**
 * Check if form is valid
 */
const isFormValid = computed(() => {
  return (
    formData.value.email.trim() &&
    formData.value.password &&
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
    // Call login API
    await authStore.login(formData.value.email.trim(), formData.value.password);

    // Redirect to landing page after successful login
    router.push("/");
  } catch (error: any) {
    // Handle error from backend
    const errorMessage =
      error.response?.data?.details ||
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      "เกิดข้อผิดพลาดในการเข้าสู่ระบบ";
    serverError.value = errorMessage;
    console.error("Login error:", error);
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
@reference "tailwindcss";

/* Login Page Container */
.login-page {
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
.login-page__main {
  @apply max-w-md mx-auto py-12 px-4;
}

/* Login Card */
.login-card {
  @apply bg-white rounded-2xl shadow-lg p-8;
  border: 1px solid #e5e7eb;
}

/* Form */
.login-form {
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

  .login-page__main {
    @apply py-8 px-4;
  }

  .login-card {
    @apply p-6;
  }
}
</style>
