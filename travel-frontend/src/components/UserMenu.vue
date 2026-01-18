<template>
  <div class="user-menu-wrapper" ref="menuWrapperRef">
    <!-- User Button / Trigger -->
    <button
      type="button"
      class="user-menu-trigger"
      :class="{ 'user-menu-trigger--active': isOpen }"
      @click="toggleMenu"
      aria-label="เมนูผู้ใช้"
      :aria-expanded="isOpen"
      aria-haspopup="true"
    >
      <span class="user-menu-display-name">
        {{ user?.displayName || "User" }}
      </span>
      <svg
        class="user-menu-icon"
        :class="{ 'user-menu-icon--rotated': isOpen }"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M19 9l-7 7-7-7"
        />
      </svg>
    </button>

    <!-- Dropdown Menu -->
    <transition name="dropdown">
      <div v-if="isOpen" class="user-menu-dropdown">
        <nav class="user-menu-nav">
          <router-link
            to="/dashboard"
            class="user-menu-item"
            @click="closeMenu"
          >
            <svg
              class="user-menu-item-icon"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
              />
            </svg>
            <span>Dashboard</span>
          </router-link>

          <router-link to="/profile" class="user-menu-item" @click="closeMenu">
            <svg
              class="user-menu-item-icon"
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
            <span>Profile</span>
          </router-link>

          <div class="user-menu-divider"></div>

          <button
            type="button"
            class="user-menu-item user-menu-item--danger"
            @click="handleLogout"
          >
            <svg
              class="user-menu-item-icon"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
              />
            </svg>
            <span>Logout</span>
          </button>
        </nav>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const authStore = useAuthStore();

const isOpen = ref(false);
const menuWrapperRef = ref<HTMLElement | null>(null);

// Get user from auth store
const user = computed(() => authStore.user);

/**
 * Toggle dropdown menu
 */
function toggleMenu() {
  isOpen.value = !isOpen.value;
}

/**
 * Close dropdown menu
 */
function closeMenu() {
  isOpen.value = false;
}

/**
 * Handle logout
 * Call authStore.logout() and redirect to landing page
 */
async function handleLogout() {
  try {
    await authStore.logout();
    closeMenu();
    router.push("/");
  } catch (error) {
    console.error("Logout error:", error);
    // Still close menu and redirect even if API call fails
    closeMenu();
    router.push("/");
  }
}

/**
 * Handle click outside to close menu
 * TODO: This will be implemented later with v-click-outside directive
 */
function handleClickOutside(event: MouseEvent) {
  if (
    menuWrapperRef.value &&
    !menuWrapperRef.value.contains(event.target as Node)
  ) {
    closeMenu();
  }
}

// Listen for click outside events
onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>

<style scoped>
@reference "tailwindcss";

/* 
 * UserMenu Component
 * - Full dropdown menu for authenticated users
 * - Displays Dashboard, Profile, and Logout options
 * - Follows design system from style.css
 */

.user-menu-wrapper {
  @apply relative;
}

/* Trigger Button */
.user-menu-trigger {
  @apply flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium cursor-pointer transition-colors duration-200;
  color: var(--color-brand-600); /* Royal Violet */
  background: transparent;
  border: 1px solid transparent;
  font-family: var(--font-sans);
}

.user-menu-trigger:hover {
  @apply bg-[var(--color-surface-100)]; /* Lavender Mist */
  color: var(--color-brand-800); /* Aubergine Ink */
}

.user-menu-trigger--active {
  @apply bg-[var(--color-surface-100)]; /* Lavender Mist */
  color: var(--color-brand-800); /* Aubergine Ink */
}

.user-menu-display-name {
  @apply max-w-[120px] truncate;
}

.user-menu-icon {
  @apply w-4 h-4 transition-transform duration-200;
}

.user-menu-icon--rotated {
  transform: rotate(180deg);
}

/* Dropdown Menu */
.user-menu-dropdown {
  @apply absolute right-0 mt-2 w-56 rounded-lg shadow-lg bg-white border border-gray-200 py-1 z-50;
  min-width: 14rem;
}

/* Navigation */
.user-menu-nav {
  @apply flex flex-col;
}

/* Menu Items */
.user-menu-item {
  @apply flex items-center gap-3 px-4 py-2.5 text-sm font-medium cursor-pointer transition-colors duration-200;
  color: #1f2937; /* Text Primary */
  background: transparent;
  border: none;
  text-decoration: none;
  font-family: var(--font-sans);
}

.user-menu-item:hover {
  @apply bg-[var(--color-surface-100)]; /* Lavender Mist */
  color: var(--color-brand-600); /* Royal Violet */
}

.user-menu-item--danger {
  color: var(--color-signal-warm-500); /* Sunset Clay */
}

.user-menu-item--danger:hover {
  @apply bg-red-50;
  color: var(--color-signal-warm-500); /* Sunset Clay */
}

.user-menu-item-icon {
  @apply w-5 h-5 flex-shrink-0;
  stroke-width: 2;
}

/* Divider */
.user-menu-divider {
  @apply my-1 h-px bg-gray-200;
}

/* Dropdown Transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.dropdown-enter-to,
.dropdown-leave-from {
  opacity: 1;
  transform: translateY(0);
}

/* Responsive Design */
@media (max-width: 768px) {
  .user-menu-display-name {
    @apply max-w-[100px];
  }

  .user-menu-dropdown {
    @apply w-48;
    min-width: 12rem;
  }
}
</style>
