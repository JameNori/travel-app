<template>
  <div class="landing-page">
    <!-- Navbar -->
    <Navbar
      :show-search="true"
      search-label="ค้นหาที่เที่ยว"
      search-placeholder="หาที่เที่ยวแล้วไปกัน..."
      :search-loading="isLoading"
      @search="handleSearchFromComponent"
      @search-clear="handleClear"
    />

    <!-- Main Content -->
    <main class="landing-page__main">
      <!-- Loading State -->
      <div v-if="isLoading" class="landing-page__loading">
        <div class="landing-page__spinner"></div>
        <p>กำลังโหลดทริป...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="landing-page__error">
        <p>เกิดข้อผิดพลาด: {{ error }}</p>
        <button @click="() => fetchTrips()" class="landing-page__retry-button">
          ลองอีกครั้ง
        </button>
      </div>

      <!-- Empty State -->
      <div v-else-if="trips.length === 0" class="landing-page__empty">
        <p>ไม่พบทริป</p>
        <p v-if="searchQuery" class="landing-page__empty-hint">
          ลองค้นหาด้วยคำอื่น
        </p>
      </div>

      <!-- Trip List -->
      <div v-else class="landing-page__trips">
        <TripCard v-for="trip in trips" :key="trip.id" :trip="trip" />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getAllTrips } from "../api/trip";
import type { Trip } from "../api/trip";
import TripCard from "../components/TripCard.vue";
import Navbar from "../components/Navbar.vue";

const trips = ref<Trip[]>([]);
const isLoading = ref(false);
const error = ref<string | null>(null);
const searchQuery = ref("");

/**
 * Fetch trips from API
 * ใช้ getAllTrips() จาก trip API และรองรับ query parameter สำหรับ search
 */
async function fetchTrips(query?: string) {
  isLoading.value = true;
  error.value = null;

  try {
    const data = await getAllTrips(query);
    trips.value = data;
  } catch (err: any) {
    error.value =
      err.response?.data?.message ||
      err.message ||
      "เกิดข้อผิดพลาดในการโหลดทริป";
    console.error("Error fetching trips:", err);
    trips.value = []; // Clear trips on error
  } finally {
    isLoading.value = false;
  }
}

/**
 * Handle search from SearchBar component
 * เรียกเมื่อ SearchBar emit search event (หลัง debounce หรือ Enter)
 */
function handleSearchFromComponent(query: string) {
  fetchTrips(query || undefined);
}

/**
 * Handle clear button click from SearchBar
 * Reset search and fetch all trips
 */
function handleClear() {
  searchQuery.value = "";
  fetchTrips();
}

// Fetch trips on mount
onMounted(() => {
  fetchTrips();
});
</script>

<style scoped>
@reference "tailwindcss";

/* 
 * Design System:
 * - Brand Color: Ultra Violet
 * - Soft Accent: Lavender
 * - Background: #F9FAFB
 * - Border: #E5E7EB
 * - Text Primary: #1F2937
 * - Text Secondary: #6B7280
 * 
 * Typography:
 * - Title: Poppins / Prompt
 * - Body: Inter / Kanit
 */

/* Landing Page Container */
.landing-page {
  @apply min-h-screen;
  background: var(--color-surface-50); /* Cloud Lilac */
}

/* Main Content */
.landing-page__main {
  @apply max-w-6xl mx-auto py-12 px-4;
}

/* Loading State */
.landing-page__loading {
  @apply text-center py-16 px-4;
}

.landing-page__spinner {
  @apply w-12 h-12 rounded-full mx-auto mb-4;
  border: 4px solid #e5e7eb;
  border-top-color: var(--color-brand-600); /* Royal Violet */
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.landing-page__loading p {
  @apply text-sm;
  color: #6b7280; /* Text Secondary */
  font-family: var(--font-sans);
}

/* Error State */
.landing-page__error {
  @apply text-center py-16 px-4;
}

.landing-page__error p {
  @apply mb-4;
  color: var(--color-signal-warm-500); /* Sunset Clay - error signal */
  font-family: var(--font-sans);
}

.landing-page__retry-button {
  @apply px-6 py-2 rounded-md text-sm font-medium cursor-pointer transition-colors duration-200 bg-[var(--color-brand-600)] text-white;
  font-family: var(--font-sans);
}

.landing-page__retry-button:hover {
  @apply bg-[var(--color-brand-800)]; /* Aubergine Ink */
}

/* Empty State */
.landing-page__empty {
  @apply text-center py-16 px-4;
}

.landing-page__empty p {
  @apply mb-2;
  color: #6b7280; /* Text Secondary */
  font-family: var(--font-sans);
  font-size: 1rem;
}

.landing-page__empty-hint {
  @apply text-sm;
  color: #9ca3af;
  font-family: var(--font-sans);
}

/* Trip List */
.landing-page__trips {
  @apply flex flex-col gap-8;
}

/* Responsive Design */
@media (max-width: 768px) {
  .landing-page__main {
    @apply py-8 px-4;
  }

  .landing-page__trips {
    @apply gap-6;
  }
}
</style>
