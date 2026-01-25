<template>
  <transition name="modal">
    <div
      v-if="isOpen"
      class="error-dialog-overlay"
      @click="handleClose"
    >
      <div class="error-dialog" @click.stop>
        <!-- Error Icon -->
        <div class="error-dialog-icon-wrapper">
          <svg
            class="error-dialog-icon"
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
        </div>

        <!-- Error Title -->
        <h3 class="error-dialog-title">เกิดข้อผิดพลาด</h3>

        <!-- Error Message -->
        <div class="error-dialog-message">
          <p
            v-for="(line, index) in messageLines"
            :key="index"
            class="error-message-line"
          >
            {{ line }}
          </p>
        </div>

        <!-- Action Button -->
        <div class="error-dialog-actions">
          <button
            type="button"
            class="error-dialog-button"
            @click="handleClose"
          >
            ตกลง
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { computed } from "vue";

interface Props {
  isOpen: boolean;
  message: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  close: [];
}>();

/**
 * Split message into lines for better display
 */
const messageLines = computed(() => {
  if (!props.message) return [];
  return props.message.split("\n").filter((line) => line.trim() !== "");
});

/**
 * Handle close dialog
 */
function handleClose() {
  emit("close");
}
</script>

<style scoped>
@reference "tailwindcss";

/* Error Dialog Overlay */
.error-dialog-overlay {
  @apply fixed inset-0 z-50 flex items-center justify-center p-4;
  background-color: rgba(0, 0, 0, 0.5);
}

/* Error Dialog */
.error-dialog {
  @apply bg-white rounded-2xl shadow-xl max-w-md w-full p-6;
  animation: slideIn 0.2s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Error Icon */
.error-dialog-icon-wrapper {
  @apply flex justify-center mb-4;
}

.error-dialog-icon {
  @apply w-12 h-12;
  color: #ef4444; /* red-500 */
}

/* Error Title */
.error-dialog-title {
  @apply text-xl font-semibold mb-4 text-center;
  color: #1f2937; /* Text Primary */
  font-family: var(--font-display);
}

/* Error Message */
.error-dialog-message {
  @apply mb-6;
  max-height: 300px;
  overflow-y: auto;
}

.error-message-line {
  @apply text-sm leading-relaxed mb-2;
  color: #6b7280; /* Text Secondary */
  font-family: var(--font-sans);
  white-space: pre-wrap;
  word-break: break-word;
}

.error-message-line:last-child {
  @apply mb-0;
}

/* Actions */
.error-dialog-actions {
  @apply flex justify-end;
}

.error-dialog-button {
  @apply px-6 py-2 rounded-lg font-medium transition-colors duration-200;
  background: var(--color-brand-600);
  color: white;
  font-family: var(--font-sans);
}

.error-dialog-button:hover {
  background: var(--color-brand-800);
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

.modal-enter-active .error-dialog,
.modal-leave-active .error-dialog {
  transition: transform 0.2s ease;
}

.modal-enter-from .error-dialog,
.modal-leave-to .error-dialog {
  transform: scale(0.95);
}

/* Responsive */
@media (max-width: 768px) {
  .error-dialog {
    @apply max-w-full;
  }
}
</style>
