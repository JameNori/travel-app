<template>
  <div class="searchbar-container">
    <label v-if="label" class="searchbar-label" :for="inputId">
      {{ label }}
    </label>
    <div class="searchbar-input-wrapper">
      <input
        :id="inputId"
        v-model="localQuery"
        type="text"
        class="searchbar-input"
        :placeholder="placeholder"
        @input="handleInput"
        @keydown="handleKeyDown"
      />
      <div class="searchbar-actions">
        <!-- Loading Spinner -->
        <div v-if="loading" class="searchbar-loading">
          <div class="searchbar-spinner"></div>
        </div>
        <!-- Clear Button -->
        <button
          v-else-if="localQuery && showClearButton"
          type="button"
          class="searchbar-clear"
          @click="handleClear"
          aria-label="ล้างคำค้นหา"
        >
          <svg
            class="searchbar-clear-icon"
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
      <div class="searchbar-separator"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";

interface Props {
  modelValue?: string;
  placeholder?: string;
  label?: string;
  debounceMs?: number;
  inputId?: string;
  loading?: boolean;
  showClearButton?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: "",
  placeholder: "หาที่เที่ยวแล้วไปกัน...",
  label: "",
  debounceMs: 300,
  inputId: "searchbar-input",
  loading: false,
  showClearButton: true,
});

const emit = defineEmits<{
  "update:modelValue": [value: string];
  search: [query: string];
  clear: [];
}>();

const localQuery = ref(props.modelValue || "");
let searchTimeout: ReturnType<typeof setTimeout> | null = null;

// Watch external modelValue changes
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue !== localQuery.value) {
      localQuery.value = newValue || "";
    }
  }
);

/**
 * Handle input with debounce
 * Emit update:modelValue immediately for v-model binding
 * Emit search event after debounce delay
 */
function handleInput() {
  // Emit v-model update immediately
  emit("update:modelValue", localQuery.value);

  // Clear previous timeout
  if (searchTimeout) {
    clearTimeout(searchTimeout);
  }

  // Set new timeout for search event
  searchTimeout = setTimeout(() => {
    const query = localQuery.value.trim();
    emit("search", query);
  }, props.debounceMs);
}

/**
 * Handle Enter key press
 * Trigger search immediately without debounce
 */
function handleKeyDown(e: KeyboardEvent) {
  if (e.key === "Enter") {
    e.preventDefault();
    // Clear debounce timeout
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }
    // Emit search immediately
    const query = localQuery.value.trim();
    emit("search", query);
  }
}

/**
 * Handle clear button click
 * Clear input and emit events
 */
function handleClear() {
  localQuery.value = "";
  emit("update:modelValue", "");
  emit("clear");
  // Clear debounce timeout
  if (searchTimeout) {
    clearTimeout(searchTimeout);
  }
  // Emit search with empty query to reset results
  emit("search", "");
}
</script>

<style scoped>
@reference "tailwindcss";

/* 
 * SearchBar Component
 * - Reusable search input component with loading state and clear button
 * - Follows design system from style.css
 * - Supports debounced search and Enter key
 */

.searchbar-container {
  @apply flex flex-col items-center w-full;
}

.searchbar-label {
  @apply mb-1 self-start;
  color: #6b7280; /* Text Secondary */
  font-family: var(--font-sans);
  font-size: 1rem;
  font-weight: 400;
}

.searchbar-input-wrapper {
  @apply w-full flex flex-col items-center relative;
}

.searchbar-input {
  @apply w-full py-1 px-4 pr-10 border-0 rounded-none text-base outline-none bg-white text-center;
  color: #1f2937; /* Text Primary */
  font-family: var(--font-sans);
  transition: opacity 0.2s;
}

.searchbar-input:disabled {
  @apply opacity-60 cursor-not-allowed;
}

.searchbar-input:focus {
  @apply border-0;
  outline: 2px solid var(--color-brand-500); /* Neon Orchid - active highlight */
  outline-offset: 2px;
}

.searchbar-input::placeholder {
  color: #9ca3af; /* Placeholder */
}

/* Actions Container (Loading + Clear Button) */
.searchbar-actions {
  @apply absolute right-2 top-1/2 transform -translate-y-1/2 flex items-center justify-center;
  width: 24px;
  height: 24px;
}

/* Loading Spinner */
.searchbar-loading {
  @apply flex items-center justify-center;
}

.searchbar-spinner {
  @apply w-5 h-5 rounded-full border-2;
  border-color: #e5e7eb; /* Border */
  border-top-color: var(--color-brand-600); /* Royal Violet */
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Clear Button */
.searchbar-clear {
  @apply w-5 h-5 flex items-center justify-center cursor-pointer rounded-full transition-colors duration-200;
  color: #9ca3af; /* Placeholder color */
  background: transparent;
  border: none;
  padding: 0;
}

.searchbar-clear:hover {
  @apply bg-gray-100;
  color: #6b7280; /* Text Secondary */
}

.searchbar-clear:active {
  @apply bg-gray-200;
}

.searchbar-clear-icon {
  @apply w-4 h-4;
  stroke-width: 2.5;
}

.searchbar-separator {
  @apply w-full mt-1;
  height: 1px;
  background: #e5e7eb; /* Border */
}

/* Responsive Design */
@media (max-width: 768px) {
  .searchbar-input {
    @apply text-sm pr-9;
  }

  .searchbar-actions {
    @apply right-1.5;
    width: 20px;
    height: 20px;
  }

  .searchbar-spinner {
    @apply w-4 h-4;
  }

  .searchbar-clear {
    @apply w-4 h-4;
  }

  .searchbar-clear-icon {
    @apply w-3.5 h-3.5;
  }
}
</style>
