<template>
  <div class="trip-detail-page">
    <!-- Navbar -->
    <Navbar />

    <!-- Main Content -->
    <main class="trip-detail-page__main">
      <!-- Loading State -->
      <div v-if="isLoading" class="trip-detail-page__loading">
        <div class="trip-detail-page__spinner"></div>
        <p>กำลังโหลดรายละเอียดทริป...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="trip-detail-page__error">
        <p>เกิดข้อผิดพลาด: {{ error }}</p>
        <button @click="goBack" class="trip-detail-page__back-button">
          กลับไปหน้าก่อนหน้า
        </button>
      </div>

      <!-- Trip Detail Content -->
      <div v-else-if="trip" class="trip-detail-content">
        <!-- Back Button -->
        <button @click="goBack" class="trip-detail-content__back">
          ← กลับ
        </button>

        <!-- Title Section -->
        <header class="trip-detail-content__header">
          <h1 class="trip-detail-content__title">{{ trip.title }}</h1>
          <div v-if="trip.tags && trip.tags.length > 0" class="trip-detail-content__tags">
            <span
              v-for="(tag, index) in trip.tags"
              :key="index"
              class="trip-detail-content__tag"
            >
              {{ tag }}
            </span>
          </div>
        </header>

        <!-- Main Image Gallery -->
        <div v-if="trip.photos && trip.photos.length > 0" class="trip-detail-content__gallery">
          <div class="trip-detail-content__main-image">
            <img
              :src="mainImageUrl || trip.photos[0]"
              :alt="trip.title"
              class="main-image"
              @error="handleImageError"
            />
          </div>
          <div v-if="trip.photos.length > 1" class="trip-detail-content__thumbnail-section">
            <div class="trip-detail-content__thumbnail-grid">
              <!-- Thumbnail รูปแรก (รูปที่ 1) -->
              <img
                :src="trip.photos![0]!"
                :alt="`${trip.title} - Photo 1`"
                class="thumbnail-image"
                :class="{ 'thumbnail-image--active': isMainImageActive(trip.photos![0]!) }"
                @error="handleImageError"
                @click="handleThumbnailClick(trip.photos![0]!)"
              />
              <!-- Thumbnail รูปที่ 2-4 (แสดงแค่ 3 รูปแรกถ้ามีรูปมากกว่า 4) -->
              <img
                v-for="(photo, index) in displayedThumbnails"
                :key="index"
                :src="photo"
                :alt="`${trip.title} - Photo ${index + 2}`"
                class="thumbnail-image"
                :class="{ 'thumbnail-image--active': isMainImageActive(photo) }"
                @error="handleImageError"
                @click="handleThumbnailClick(photo)"
              />
            </div>
            <!-- Button "ดูรูปภาพเพิ่มเติม" -->
            <button
              v-if="shouldShowMoreButton && !showAllThumbnails"
              type="button"
              class="trip-detail-content__show-more-button"
              @click="showAllThumbnails = true"
            >
              ดูรูปภาพเพิ่มเติม ({{ remainingPhotosCount }} รูป)
            </button>
            <!-- Button "ซ่อนรูปภาพ" เมื่อแสดงทั้งหมดแล้ว -->
            <button
              v-if="shouldShowMoreButton && showAllThumbnails"
              type="button"
              class="trip-detail-content__show-more-button"
              @click="showAllThumbnails = false"
            >
              ซ่อนรูปภาพ
            </button>
          </div>
        </div>
        <div v-else class="trip-detail-content__no-image">
          <span class="no-image-text">ไม่มีรูปภาพ</span>
        </div>

        <!-- Description Section -->
        <section class="trip-detail-content__description">
          <h2 class="section-title">รายละเอียด</h2>
          <p v-if="trip.description" class="description-text">
            {{ trip.description }}
          </p>
          <p v-else class="description-text description-text--empty">
            ไม่มีรายละเอียด
          </p>
        </section>

        <!-- Map Section -->
        <section v-if="trip.latitude && trip.longitude" class="trip-detail-content__map">
          <h2 class="section-title">ตำแหน่งที่ตั้ง</h2>
          <div class="map-container">
            <a
              :href="mapUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="map-link"
            >
              <div class="map-placeholder">
                <svg
                  class="map-icon"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                  />
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
                <p class="map-placeholder-text">คลิกเพื่อดูแผนที่</p>
                <p class="map-coordinates">
                  {{ trip.latitude }}, {{ trip.longitude }}
                </p>
              </div>
            </a>
          </div>
        </section>

        <!-- Author & Metadata Section -->
        <section class="trip-detail-content__metadata">
          <div v-if="trip.author" class="metadata-item">
            <span class="metadata-label">ผู้สร้าง:</span>
            <span class="metadata-value">{{ trip.author.displayName }}</span>
          </div>
          <div v-if="trip.createdAt" class="metadata-item">
            <span class="metadata-label">สร้างเมื่อ:</span>
            <span class="metadata-value">{{ formatDate(trip.createdAt) }}</span>
          </div>
        </section>

        <!-- External URL Section -->
        <section v-if="trip.url" class="trip-detail-content__external-link">
          <a
            :href="trip.url"
            target="_blank"
            rel="noopener noreferrer"
            class="external-link-button"
          >
            <svg
              class="external-link-icon"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
              />
            </svg>
            อ่านเพิ่มเติม
          </a>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getTripById } from "../api/trip";
import type { Trip } from "../api/trip";
import Navbar from "../components/Navbar.vue";

const route = useRoute();
const router = useRouter();

const trip = ref<Trip | null>(null);
const isLoading = ref(false);
const error = ref<string | null>(null);
const mainImageUrl = ref<string | null>(null);
const showAllThumbnails = ref(false);

/**
 * Check if screen is mobile (width < 768px)
 */
const isMobile = ref(false);

function updateIsMobile() {
  isMobile.value = window.innerWidth < 768;
}

onMounted(() => {
  updateIsMobile();
  window.addEventListener("resize", updateIsMobile);
});

onUnmounted(() => {
  window.removeEventListener("resize", updateIsMobile);
});

/**
 * Get trip ID from route params
 */
const tripId = computed(() => {
  const id = route.params.id;
  if (typeof id === "string") {
    return parseInt(id, 10);
  }
  return null;
});

/**
 * Generate Google Maps URL for the location
 */
const mapUrl = computed(() => {
  if (!trip.value?.latitude || !trip.value?.longitude) return "";
  return `https://www.google.com/maps?q=${trip.value.latitude},${trip.value.longitude}`;
});

/**
 * Get thumbnails to display
 * Mobile: แสดง 2 รูป (รูปที่ 2-3) ถ้ามีรูปมากกว่า 3
 * Desktop: แสดง 3 รูป (รูปที่ 2-4) ถ้ามีรูปมากกว่า 4
 */
const displayedThumbnails = computed(() => {
  if (!trip.value?.photos || trip.value.photos.length <= 1) return [];
  // ถ้า showAllThumbnails = true แสดงทั้งหมด
  if (showAllThumbnails.value) {
    return trip.value.photos.slice(1);
  }
  // Mobile: แสดง 2 รูป (รูปที่ 2-3), Desktop: แสดง 3 รูป (รูปที่ 2-4)
  const maxThumbnails = isMobile.value ? 2 : 3;
  return trip.value.photos.slice(1, maxThumbnails + 1);
});

/**
 * Check if should show "ดูรูปภาพเพิ่มเติม" button
 * Mobile: แสดงเมื่อมีรูปมากกว่า 3
 * Desktop: แสดงเมื่อมีรูปมากกว่า 4
 */
const shouldShowMoreButton = computed(() => {
  if (!trip.value?.photos || trip.value.photos.length <= 1) return false;
  const threshold = isMobile.value ? 3 : 4;
  return trip.value.photos.length > threshold;
});

/**
 * Get remaining photos count for button text
 */
const remainingPhotosCount = computed(() => {
  if (!trip.value?.photos) return 0;
  const shownCount = isMobile.value ? 3 : 4; // Mobile: แสดง 3 รูป, Desktop: แสดง 4 รูป
  return trip.value.photos.length - shownCount;
});

/**
 * Fetch trip details from API
 */
async function fetchTripDetails() {
  if (!tripId.value) {
    error.value = "ไม่พบ ID ของทริป";
    return;
  }

  isLoading.value = true;
  error.value = null;

  try {
    const data = await getTripById(tripId.value);
    trip.value = data;
    // Set main image to first photo if available
    if (data.photos && data.photos.length > 0) {
      mainImageUrl.value = data.photos[0] || null;
    }
  } catch (err: any) {
    error.value =
      err.response?.data?.message ||
      err.message ||
      "เกิดข้อผิดพลาดในการโหลดรายละเอียดทริป";
    console.error("Error fetching trip details:", err);
  } finally {
    isLoading.value = false;
  }
}

/**
 * Check if the given photo URL is currently the main image
 */
function isMainImageActive(photoUrl: string): boolean {
  if (!mainImageUrl.value) {
    // ถ้า mainImageUrl ยังไม่ถูก set (default) ให้รูปแรกเป็น active
    return photoUrl === trip.value?.photos?.[0];
  }
  return mainImageUrl.value === photoUrl;
}

/**
 * Handle thumbnail click - toggle to reset if clicking active thumbnail
 */
function handleThumbnailClick(photoUrl: string) {
  // ถ้าคลิก thumbnail ที่ active อยู่แล้ว (คือรูปที่แสดงอยู่) ให้ reset กลับไปรูปแรก
  if (isMainImageActive(photoUrl)) {
    // Reset กลับไปรูปแรก (default)
    if (trip.value?.photos && trip.value.photos.length > 0) {
      mainImageUrl.value = trip.value.photos[0] || null;
    }
  } else {
    // เปลี่ยนรูปใหญ่เป็นรูปที่คลิก
    setMainImage(photoUrl);
  }
}

/**
 * Set main image when clicking thumbnail
 */
function setMainImage(url: string) {
  mainImageUrl.value = url;
}

/**
 * Handle image error
 */
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement;
  img.style.display = "none";
}

/**
 * Format date string to readable format
 */
function formatDate(dateString: string): string {
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString("th-TH", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  } catch {
    return dateString;
  }
}

/**
 * Navigate back to previous page
 * Falls back to home page if no history available or referrer is from external site
 */
function goBack() {
  // ตรวจสอบ query parameter 'from' เพื่อดูว่ามาจากหน้าไหน
  const from = route.query.from as string;
  
  // ถ้ามาจาก dashboard ให้กลับไปที่ dashboard
  if (from === 'dashboard') {
    router.push('/dashboard');
    return;
  }
  
  // Logic เดิมสำหรับกรณีอื่นๆ (landing page หรือ external site)
  const referrer = document.referrer;
  const currentOrigin = window.location.origin;
  
  // Check if referrer exists and is from the same origin (same app)
  if (referrer && referrer.startsWith(currentOrigin)) {
    // Referrer is from the same app, safe to go back
    router.back();
  } else {
    // No referrer or referrer is from external site (e.g., Google)
    // Fallback: go to home page
    router.push('/');
  }
}

// Fetch trip details on mount
onMounted(() => {
  fetchTripDetails();
});
</script>

<style scoped>
@reference "tailwindcss";

/* 
 * Trip Detail Page
 * - Display full trip information including images, description, map, and metadata
 * - Responsive design with modern UI
 */

.trip-detail-page {
  @apply min-h-screen;
  background: var(--color-surface-50); /* Cloud Lilac */
}

.trip-detail-page__main {
  @apply max-w-4xl mx-auto py-8 px-4;
}

/* Loading State */
.trip-detail-page__loading {
  @apply text-center py-16 px-4;
}

.trip-detail-page__spinner {
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

.trip-detail-page__loading p {
  @apply text-sm;
  color: #6b7280; /* Text Secondary */
  font-family: var(--font-sans);
}

/* Error State */
.trip-detail-page__error {
  @apply text-center py-16 px-4;
}

.trip-detail-page__error p {
  @apply mb-4;
  color: var(--color-signal-warm-500); /* Sunset Clay */
  font-family: var(--font-sans);
}

.trip-detail-page__back-button {
  @apply px-6 py-2 rounded-md text-sm font-medium cursor-pointer transition-colors duration-200 bg-[var(--color-brand-600)] text-white;
  font-family: var(--font-sans);
}

.trip-detail-page__back-button:hover {
  @apply bg-[var(--color-brand-800)]; /* Aubergine Ink */
}

/* Trip Detail Content */
.trip-detail-content {
  @apply bg-white rounded-2xl border border-gray-200 shadow-sm p-6 md:p-8;
}

/* Back Button */
.trip-detail-content__back {
  @apply mb-6 px-4 py-2 rounded-lg text-sm font-medium cursor-pointer transition-colors duration-200 bg-transparent text-gray-600 hover:bg-gray-100;
  font-family: var(--font-sans);
}

/* Header Section */
.trip-detail-content__header {
  @apply mb-6;
}

.trip-detail-content__title {
  @apply mb-4;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 2rem;
  line-height: 1.3;
  color: var(--color-brand-600); /* Royal Violet */
}

.trip-detail-content__tags {
  @apply flex flex-wrap gap-2;
}

.trip-detail-content__tag {
  @apply px-3 py-1 rounded-full text-xs font-medium;
  font-family: var(--font-sans);
  background: var(--color-surface-100); /* Lavender Mist */
  color: var(--color-brand-600); /* Royal Violet */
}

/* Image Gallery */
.trip-detail-content__gallery {
  @apply mb-6;
}

.trip-detail-content__main-image {
  @apply w-full mb-4 rounded-xl overflow-hidden;
  aspect-ratio: 16 / 9;
  background: #f3f4f6;
}

.main-image {
  @apply w-full h-full object-cover;
}

.trip-detail-content__thumbnail-section {
  @apply mb-6;
}

.trip-detail-content__thumbnail-grid {
  @apply grid gap-2 mb-4;
  grid-template-columns: repeat(3, 1fr); /* Mobile: 3 columns */
}

@media (min-width: 768px) {
  .trip-detail-content__thumbnail-grid {
    grid-template-columns: repeat(4, 1fr); /* Desktop: 4 columns */
  }
}

.thumbnail-image {
  @apply w-full h-24 object-cover rounded-lg cursor-pointer transition-all duration-200;
  aspect-ratio: 1;
  border: 2px solid transparent;
}

.thumbnail-image:hover {
  transform: scale(1.05);
  @apply shadow-md;
}

.thumbnail-image--active {
  border-color: var(--color-brand-600); /* Royal Violet */
  @apply shadow-lg;
  transform: scale(1.02);
}

/* Show More Button */
.trip-detail-content__show-more-button {
  @apply w-full py-2 px-4 rounded-lg text-sm font-medium cursor-pointer transition-colors duration-200;
  color: var(--color-brand-600); /* Royal Violet */
  background: var(--color-surface-100); /* Lavender Mist */
  border: 1px solid var(--color-brand-200);
  font-family: var(--font-sans);
}

.trip-detail-content__show-more-button:hover {
  @apply bg-[var(--color-surface-200)];
  color: var(--color-brand-800); /* Aubergine Ink */
  border-color: var(--color-brand-400);
}

.trip-detail-content__no-image {
  @apply w-full h-64 flex items-center justify-center bg-gray-100 rounded-xl mb-6;
}

.no-image-text {
  @apply text-gray-400 text-sm;
  font-family: var(--font-sans);
}

/* Description Section */
.trip-detail-content__description {
  @apply mb-6;
}

.section-title {
  @apply mb-3;
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 1.25rem;
  color: var(--color-brand-600); /* Royal Violet */
}

.description-text {
  @apply text-base leading-relaxed;
  color: #1f2937; /* Text Primary */
  font-family: var(--font-sans);
  white-space: pre-wrap;
}

.description-text--empty {
  @apply text-gray-400 italic;
}

/* Map Section */
.trip-detail-content__map {
  @apply mb-6;
}

.map-container {
  @apply w-full rounded-xl overflow-hidden;
}

.map-link {
  @apply block no-underline;
}

.map-placeholder {
  @apply w-full h-64 flex flex-col items-center justify-center bg-gray-100 cursor-pointer transition-colors duration-200;
}

.map-placeholder:hover {
  @apply bg-gray-200;
}

.map-icon {
  @apply w-12 h-12 mb-2;
  color: var(--color-brand-600); /* Royal Violet */
}

.map-placeholder-text {
  @apply text-sm font-medium mb-1;
  color: var(--color-brand-600); /* Royal Violet */
  font-family: var(--font-sans);
}

.map-coordinates {
  @apply text-xs;
  color: #6b7280; /* Text Secondary */
  font-family: var(--font-sans);
}

/* Metadata Section */
.trip-detail-content__metadata {
  @apply mb-6 pb-6 border-b border-gray-200;
}

.metadata-item {
  @apply flex items-center gap-2 mb-2;
}

.metadata-label {
  @apply text-sm font-medium;
  color: #6b7280; /* Text Secondary */
  font-family: var(--font-sans);
}

.metadata-value {
  @apply text-sm;
  color: #1f2937; /* Text Primary */
  font-family: var(--font-sans);
}

/* External Link Section */
.trip-detail-content__external-link {
  @apply flex justify-center;
}

.external-link-button {
  @apply flex items-center gap-2 px-6 py-3 rounded-lg text-sm font-medium no-underline transition-colors duration-200 bg-[var(--color-brand-600)] text-white;
  font-family: var(--font-sans);
}

.external-link-button:hover {
  @apply bg-[var(--color-brand-800)]; /* Aubergine Ink */
}

.external-link-icon {
  @apply w-5 h-5;
}

/* Responsive Design */
@media (max-width: 768px) {
  .trip-detail-page__main {
    @apply py-6 px-4;
  }

  .trip-detail-content {
    @apply p-4;
  }

  .trip-detail-content__title {
    font-size: 1.5rem;
  }

  .trip-detail-content__thumbnail-grid {
    @apply grid-cols-3 gap-2;
  }

  .thumbnail-image {
    @apply h-20;
  }
}
</style>
