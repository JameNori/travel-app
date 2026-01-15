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
          <a
            v-if="trip.url"
            :href="trip.url"
            target="_blank"
            rel="noopener noreferrer"
            class="title-link"
          >
            {{ trip.title }}
          </a>
          <span v-else class="title-link">{{ trip.title }}</span>
        </h2>

        <!-- Description -->
        <p class="card-description">{{ truncatedDescription }}</p>

        <!-- Card Actions: Read More + Category Tags -->
        <div class="card-actions">
          <a
            v-if="trip.url"
            :href="trip.url"
            target="_blank"
            rel="noopener noreferrer"
            class="read-more"
          >
            อ่านต่อ
          </a>
          <span v-else class="read-more read-more-disabled"> อ่านต่อ </span>
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

        <!-- Thumbnail Images -->
        <div v-if="thumbnailImages.length > 0" class="thumbnail-images">
          <img
            v-for="(photo, index) in thumbnailImages"
            :key="index"
            :src="photo"
            :alt="`${trip.title} - Photo ${index + 2}`"
            class="thumbnail"
            @error="handleImageError"
          />
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
}

const props = withDefaults(defineProps<Props>(), {
  hasShareIcon: false,
});

// ใช้รูปแรกเป็น main image หรือ placeholder
const mainImage = computed(() => {
  return props.trip.photos && props.trip.photos.length > 0
    ? props.trip.photos[0]
    : null;
});

/**
 * จำกัดความยาวของ description ไม่เกิน 100 ตัวอักษร
 * ใช้สำหรับแสดงข้อความสั้น 2-3 บรรทัด
 */
function truncateDescription(text?: string, maxLength = 100): string {
  if (!text) return "";
  if (text.length <= maxLength) return text;
  return text.slice(0, maxLength) + "...";
}

const truncatedDescription = computed(() => {
  return truncateDescription(props.trip.description);
});

// ใช้รูปถัดไป (slice(1, 4)) เป็น thumbnails
const thumbnailImages = computed(() => {
  if (!props.trip.photos || props.trip.photos.length <= 1) {
    return [];
  }
  return props.trip.photos.slice(1, 4);
});

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
  @apply flex-shrink-0 w-[300px] overflow-hidden flex items-center justify-center;
}

.main-image {
  @apply w-full h-[250px] object-cover rounded-xl transition-transform duration-300;
}

.trip-card:hover .main-image {
  transform: scale(1.02);
}

.image-placeholder {
  @apply w-full h-[250px] flex items-center justify-center bg-gray-100;
}

.placeholder-text {
  @apply text-gray-400 text-sm;
}

/* Card Details (Right Side) */
.card-details {
  @apply flex-1 p-6 flex flex-col;
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
  /* จำกัดความสูงเพื่อให้แสดง 2-3 บรรทัด */
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

.read-more-disabled {
  @apply text-gray-400 cursor-not-allowed;
  pointer-events: none;
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
.thumbnail-images {
  @apply flex gap-3;
}

.thumbnail {
  @apply w-20 h-20 object-cover rounded-xl transition-transform duration-200;
}

.thumbnail:hover {
  transform: scale(1.05);
}

/* Responsive Design */
@media (max-width: 768px) {
  .card-content {
    @apply flex-col;
  }

  .main-image-container {
    @apply w-full;
  }

  .main-image {
    @apply h-48;
  }

  .card-details {
    @apply p-4;
  }

  .thumbnail-images {
    @apply justify-center;
  }
}
</style>
