<template>
  <article class="trip-card">
    <div class="card-content">
      <!-- Main Image (Left Side) -->
      <div class="main-image-container">
        <img
          v-if="mainImage"
          :src="mainImage"
          :alt="trip.title"
          class="main-image"
          loading="lazy"
          decoding="async"
          @error="handleImageError"
        />
        <div v-else class="image-placeholder">
          <span class="placeholder-text">No Image</span>
        </div>
      </div>

      <!-- Card Details (Right Side) -->
      <div class="card-details">
        <!-- Title -->
        <h2 class="card-title">
          <router-link
            :to="detailLink"
            class="title-link"
          >
            {{ trip.title }}
          </router-link>
        </h2>

        <!-- Description -->
        <p class="card-description">{{ truncatedDescription }}</p>

        <!-- Card Actions: Read More / Meta + Category Tags -->
        <div class="card-actions">
          <!-- Landing: แสดง "อ่านต่อ" | Dashboard: แสดง "สร้างเมื่อ" -->
          <router-link
            v-if="!showActions"
            :to="detailLink"
            class="read-more"
          >
            อ่านต่อ
          </router-link>
          <span v-else-if="showMeta && trip.createdAt" class="meta-date">
            สร้างเมื่อ: {{ formatDate(trip.createdAt) }}
          </span>
          <span v-else class="spacer"></span>
          
          <!-- Tags อยู่ด้านขวาเสมอ -->
          <div v-if="trip.tags && trip.tags.length > 0" class="category-tags">
            <span
              v-for="(tag, index) in trip.tags"
              :key="index"
              class="category-tag tag-default"
            >
              {{ tag }}
            </span>
          </div>
        </div>

        <!-- Bottom Row: Thumbnails + Action Buttons -->
        <div class="card-bottom-row">
          <!-- Thumbnail Images -->
          <div v-if="thumbnailImages.length > 0" class="thumbnail-images">
            <img
              v-for="(photo, index) in thumbnailImages"
              :key="index"
              :src="photo"
              :alt="`${trip.title} - Photo ${index + 2}`"
              class="thumbnail"
              loading="lazy"
              decoding="async"
              @error="handleImageError"
            />
          </div>
          <div v-else class="thumbnail-spacer"></div>

          <!-- Action Buttons (for Dashboard) - มุมขวาล่าง -->
          <div v-if="showActions" class="card-action-buttons">
            <button
              type="button"
              class="action-button action-button--edit"
              @click="handleEdit"
              aria-label="แก้ไขทริป"
            >
              <svg
                class="action-icon"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                />
              </svg>
              แก้ไข
            </button>
            <button
              type="button"
              class="action-button action-button--delete"
              @click="handleDelete"
              aria-label="ลบทริป"
            >
              <svg
                class="action-icon"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                />
              </svg>
              ลบ
            </button>
          </div>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Trip } from "../api/trip";

interface Props {
  trip: Trip;
  hasShareIcon?: boolean;
  showActions?: boolean;
  showMeta?: boolean;
  from?: string;
  truncateLength?: number;
}

const props = withDefaults(defineProps<Props>(), {
  hasShareIcon: false,
  showActions: false,
  showMeta: false,
  from: undefined,
  truncateLength: 100,
});

const emit = defineEmits<{
  edit: [trip: Trip];
  delete: [trip: Trip];
}>();

// ใช้รูปแรกเป็น main image หรือ placeholder
const mainImage = computed(() => {
  return props.trip.photos && props.trip.photos.length > 0
    ? props.trip.photos[0]
    : null;
});

/**
 * สร้าง detail link พร้อม query parameter
 */
const detailLink = computed(() => {
  const basePath = `/trips/${props.trip.id}`;
  if (props.from) {
    return `${basePath}?from=${props.from}`;
  }
  return basePath;
});

/**
 * จำกัดความยาวของ description
 * ใช้สำหรับแสดงข้อความสั้น 2-3 บรรทัด
 */
function truncateDescription(text?: string, maxLength?: number): string {
  if (!text) return "";
  const length = maxLength ?? props.truncateLength;
  if (text.length <= length) return text;
  return text.slice(0, length) + "...";
}

const truncatedDescription = computed(() => {
  return truncateDescription(props.trip.description, props.truncateLength);
});

// ใช้รูปถัดไป (slice(1, 4)) เป็น thumbnails
const thumbnailImages = computed(() => {
  if (!props.trip.photos || props.trip.photos.length <= 1) {
    return [];
  }
  return props.trip.photos.slice(1, 4);
});

/**
 * Format date for display
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
 * Handle edit button click
 */
function handleEdit() {
  emit("edit", props.trip);
}

/**
 * Handle delete button click
 */
function handleDelete() {
  emit("delete", props.trip);
}

// Handle image error (fallback to placeholder)
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement;
  img.style.display = "none";
}
</script>

<style scoped>
@reference "tailwindcss";
/* 
 * Design Decision:
 * - ใช้ Tailwind utility classes ผ่าน @apply เพื่อความสะอาดและ maintainability
 */

/* Card Container */
.trip-card {
  @apply bg-white rounded-2xl border border-gray-200 overflow-hidden transition-all duration-200 mb-8;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05);
}

.trip-card:hover {
  @apply shadow-lg;
  box-shadow: 0 4px 12px 0 rgba(95, 75, 139, 0.08);
  transform: translateY(-2px);
}

/* Card Content Layout */
.card-content {
  @apply flex relative;
}

/* Main Image Container (Left Side) */
.main-image-container {
  @apply flex-shrink-0 w-[300px] h-[250px] overflow-hidden;
}

.main-image {
  @apply w-full h-full object-cover rounded-2xl transition-transform duration-300;
}

.trip-card:hover .main-image {
  transform: scale(1.02);
}

.image-placeholder {
  @apply w-full h-full flex items-center justify-center bg-gray-100 rounded-2xl;
}

.placeholder-text {
  @apply text-gray-400 text-sm;
}

/* Card Details (Right Side) */
.card-details {
  @apply flex-1 pt-6 px-6 pb-4 flex flex-col;
}

/* Title */
.card-title {
  @apply mb-3;
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 1.25rem;
  line-height: 1.4;
}

.title-link {
  @apply no-underline;
  color: var(--color-brand-600); /* Royal Violet - Normal */
}

/* Description */
.card-description {
  @apply text-gray-500 mb-4 flex-1;
  font-family: var(--font-sans);
  font-weight: 400;
  font-size: 0.9375rem;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Card Actions: Read More + Category Tags */
.card-actions {
  @apply flex justify-between items-center mb-4;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.read-more {
  @apply no-underline font-medium whitespace-nowrap;
  color: var(--color-brand-600); /* Royal Violet - Normal */
  font-family: var(--font-sans);
  font-weight: 500;
  font-size: 0.9375rem;
}

.meta-date {
  @apply text-sm text-gray-500 whitespace-nowrap;
  font-family: var(--font-sans);
}

.spacer {
  @apply flex-shrink-0;
}


/* Category Tags */
.category-tags {
  @apply flex flex-wrap gap-2;
}

.category-tag {
  @apply px-3 py-1 rounded-full text-xs font-medium;
  font-family: var(--font-sans);
  font-weight: 500;
}

/* Tag Colors by Category */
.tag-sea {
  @apply bg-[var(--color-sky-blue)] text-white;
}

.tag-nature {
  @apply bg-[var(--color-forest-green)] text-white;
}

.tag-chill {
  @apply bg-[var(--color-sand-beige)] text-[#8B6914];
}

.tag-default {
  @apply bg-[var(--color-surface-100)] text-[var(--color-brand-600)]; /* Lavender Mist + Royal Violet */
}

/* Thumbnail Images */
/* Bottom Row: Thumbnails + Action Buttons */
.card-bottom-row {
  @apply flex justify-between items-end gap-4;
}

.thumbnail-images {
  @apply flex gap-3;
}

.thumbnail-spacer {
  @apply flex-1;
}

.thumbnail {
  @apply w-20 h-20 object-cover rounded-xl transition-transform duration-200;
}

.thumbnail:hover {
  transform: scale(1.05);
}

/* Action Buttons (for Dashboard) */
.card-action-buttons {
  @apply flex gap-2 flex-shrink-0;
}

.action-button {
  @apply flex items-center gap-1 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200;
  font-family: var(--font-sans);
}

.action-button--edit {
  background: var(--color-brand-600);
  color: white;
}

.action-button--edit:hover {
  background: var(--color-brand-800);
}

.action-button--delete {
  @apply bg-red-50 text-red-600;
}

.action-button--delete:hover {
  @apply bg-red-100;
}

.action-icon {
  @apply w-4 h-4;
}

/* Responsive Design */
@media (max-width: 768px) {
  .card-content {
    @apply flex-col;
  }

  .main-image-container {
    @apply w-full h-48;
  }

  .main-image {
    @apply rounded-2xl;
  }

  .image-placeholder {
    @apply rounded-2xl;
  }

  .card-details {
    @apply pt-4 px-4 pb-3;
  }

  .card-bottom-row {
    @apply flex-col items-stretch gap-3;
  }

  .thumbnail-images {
    @apply justify-center;
  }

  .card-action-buttons {
    @apply justify-center;
  }
}
</style>
