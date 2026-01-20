<template>
  <nav class="navbar" :class="{ 'navbar--scrolled': isScrolled }">
    <div class="navbar-container">
      <!-- Logo/Title -->
      <div class="navbar-header">
        <router-link to="/" class="navbar-logo">
          <h1 class="navbar-title">เที่ยวไหนดี</h1>
        </router-link>
        
        <!-- Mobile Hamburger Button -->
        <button
          type="button"
          class="navbar-hamburger"
          :class="{ 'navbar-hamburger--active': isMobileMenuOpen }"
          @click="toggleMobileMenu"
          aria-label="เมนู"
          :aria-expanded="isMobileMenuOpen"
          aria-controls="mobile-menu"
        >
          <span class="navbar-hamburger-line"></span>
          <span class="navbar-hamburger-line"></span>
          <span class="navbar-hamburger-line"></span>
        </button>
      </div>

      <!-- Search Section -->
      <div v-if="showSearch" class="navbar-search-section">
        <!-- SearchBar -->
        <div class="navbar-search-wrapper">
          <SearchBar
            v-model="searchQuery"
            :placeholder="searchPlaceholder"
            :loading="searchLoading"
            :label="searchLabel"
            @search="handleSearch"
            @clear="handleSearchClear"
          />
        </div>

        <!-- Navigation Links -->
        <div class="navbar-links">
          <!-- Not Authenticated: Login & Register -->
          <template v-if="!authStore.isAuthenticated">
            <router-link to="/login" class="navbar-link">
              เข้าสู่ระบบ
            </router-link>
            <router-link
              to="/register"
              class="navbar-link navbar-link--primary"
            >
              สมัครสมาชิก
            </router-link>
          </template>

          <!-- Authenticated: UserMenu -->
          <UserMenu v-else />
        </div>
      </div>

      <!-- Desktop Navigation (When no search) -->
      <div v-else class="navbar-desktop">
        <div class="navbar-links">
          <!-- Not Authenticated: Login & Register -->
          <template v-if="!authStore.isAuthenticated">
            <router-link to="/login" class="navbar-link">
              เข้าสู่ระบบ
            </router-link>
            <router-link
              to="/register"
              class="navbar-link navbar-link--primary"
            >
              สมัครสมาชิก
            </router-link>
          </template>

          <!-- Authenticated: UserMenu -->
          <UserMenu v-else />
        </div>
      </div>
    </div>

    <!-- Overlay for Mobile Menu -->
    <transition name="overlay">
      <div
        v-if="isMobileMenuOpen"
        class="navbar-overlay"
        @click="closeMobileMenu"
      ></div>
    </transition>

    <!-- Mobile Menu -->
    <transition name="mobile-menu">
      <div
        v-if="isMobileMenuOpen"
        id="mobile-menu"
        class="navbar-mobile"
        @click.stop
      >
        <!-- Navigation Links -->
        <div class="navbar-mobile-links">
          <!-- Not Authenticated: Login & Register -->
          <template v-if="!authStore.isAuthenticated">
            <router-link
              to="/login"
              class="navbar-mobile-link"
              @click="closeMobileMenu"
            >
              เข้าสู่ระบบ
            </router-link>
            <router-link
              to="/register"
              class="navbar-mobile-link navbar-mobile-link--primary"
              @click="closeMobileMenu"
            >
              สมัครสมาชิก
            </router-link>
          </template>

          <!-- Authenticated: Menu Items -->
          <template v-else>
            <router-link
              to="/dashboard"
              class="navbar-mobile-link"
              @click="closeMobileMenu"
            >
              Dashboard
            </router-link>
            <router-link
              to="/profile"
              class="navbar-mobile-link"
              @click="closeMobileMenu"
            >
              Profile
            </router-link>
            <button
              type="button"
              class="navbar-mobile-link navbar-mobile-link--danger"
              @click="handleMobileLogout"
            >
              Logout
            </button>
          </template>
        </div>
      </div>
    </transition>
  </nav>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import SearchBar from "./SearchBar.vue";
import UserMenu from "./UserMenu.vue";

interface Props {
  showSearch?: boolean;
  searchPlaceholder?: string;
  searchLabel?: string;
  searchLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  showSearch: false,
  searchPlaceholder: "หาที่เที่ยวแล้วไปกัน...",
  searchLabel: "ค้นหาที่เที่ยว",
  searchLoading: false,
});

const emit = defineEmits<{
  search: [query: string];
  "search-clear": [];
}>();

const router = useRouter();
const authStore = useAuthStore();

const isMobileMenuOpen = ref(false);
const isScrolled = ref(false);
const searchQuery = ref("");

/**
 * Toggle mobile menu
 */
function toggleMobileMenu() {
  isMobileMenuOpen.value = !isMobileMenuOpen.value;
  // Prevent body scroll when menu is open
  if (isMobileMenuOpen.value) {
    document.body.style.overflow = "hidden";
  } else {
    document.body.style.overflow = "";
  }
}

/**
 * Close mobile menu
 */
function closeMobileMenu() {
  isMobileMenuOpen.value = false;
  document.body.style.overflow = "";
}

/**
 * Handle search from SearchBar
 */
function handleSearch(query: string) {
  emit("search", query);
  // Close mobile menu after search
  if (isMobileMenuOpen.value) {
    closeMobileMenu();
  }
}

/**
 * Handle search clear
 */
function handleSearchClear() {
  searchQuery.value = "";
  emit("search-clear");
}

/**
 * Handle mobile logout
 */
async function handleMobileLogout() {
  try {
    await authStore.logout();
    closeMobileMenu();
    router.push("/");
  } catch (error) {
    console.error("Logout error:", error);
    closeMobileMenu();
    router.push("/");
  }
}

/**
 * Handle scroll for navbar styling
 */
function handleScroll() {
  isScrolled.value = window.scrollY > 10;
}

// Listen for scroll events
onMounted(() => {
  window.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
  // Ensure body overflow is reset
  document.body.style.overflow = "";
});
</script>

<style scoped>
@reference "tailwindcss";

/* 
 * Navbar Component
 * - Modern navigation bar with logo, search, and user menu
 * - Responsive with hamburger menu for mobile
 */

.navbar {
  @apply sticky top-0 z-40 bg-white border-b border-gray-200 transition-shadow duration-200;
}

.navbar--scrolled {
  @apply shadow-md;
}

.navbar-container {
  @apply max-w-7xl mx-auto px-4 py-4;
}

/* Header Section (Logo) */
.navbar-header {
  @apply text-center mb-6;
  position: relative;
}

/* Logo/Title (Center) */
.navbar-logo {
  @apply no-underline inline-block;
}

.navbar-title {
  @apply text-[var(--color-brand-600)] m-0;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 2.5rem;
  line-height: 1.2;
  white-space: nowrap;
}

.navbar-logo:hover .navbar-title {
  color: var(--color-brand-800); /* Aubergine Ink */
}

/* Search Section */
.navbar-search-section {
  @apply flex items-end gap-4;
  max-width: 1000px;
  margin: 0 auto;
}

.navbar-search-wrapper {
  @apply flex-1;
}

/* Desktop Navigation (When no search) */
.navbar-desktop {
  @apply hidden md:flex items-center justify-end;
  max-width: 1000px;
  margin: 0 auto;
}

.navbar-links {
  @apply hidden md:flex items-center gap-4;
  flex-shrink: 0;
}

/* Navigation Links (Desktop) */
.navbar-link {
  @apply px-4 py-2 rounded-lg text-sm font-medium no-underline transition-colors duration-200;
  color: #1f2937; /* Text Primary */
  font-family: var(--font-sans);
  white-space: nowrap;
}

.navbar-link:hover {
  @apply bg-[var(--color-surface-100)]; /* Lavender Mist */
  color: var(--color-brand-600); /* Royal Violet */
}

.navbar-link--primary {
  @apply bg-[var(--color-brand-600)] text-white;
}

.navbar-link--primary:hover {
  @apply bg-[var(--color-brand-800)]; /* Aubergine Ink */
  color: white;
}

/* Hamburger Button (Mobile) */
.navbar-hamburger {
  @apply md:hidden flex flex-col justify-center items-center w-10 h-10 p-0 bg-transparent border-0 cursor-pointer gap-1.5;
  position: absolute;
  top: 0;
  right: 0;
  z-index: 60;
}

.navbar-hamburger-line {
  @apply w-6 h-0.5 bg-gray-700 rounded transition-all duration-300;
}

.navbar-hamburger--active .navbar-hamburger-line:nth-child(1) {
  transform: rotate(45deg) translate(6px, 6px);
}

.navbar-hamburger--active .navbar-hamburger-line:nth-child(2) {
  opacity: 0;
}

.navbar-hamburger--active .navbar-hamburger-line:nth-child(3) {
  transform: rotate(-45deg) translate(6px, -6px);
}

/* Mobile Menu */
.navbar-mobile {
  @apply md:hidden fixed left-0 right-0 bg-white border-b border-gray-200 shadow-lg;
  top: 70px; /* เริ่มจากใต้ header section (navbar-container py-4 + title + mb-4) */
  padding-top: 0; /* ไม่ต้องมี padding-top เพราะเริ่มจากตำแหน่งที่ถูกต้องแล้ว */
  max-height: calc(100vh - 70px); /* ลดความสูงตาม top */
  overflow-y: auto;
  z-index: 50; /* สูงกว่า overlay (z-30) เพื่อให้อยู่ด้านบน */
}

.navbar-mobile-links {
  @apply flex flex-col;
}

.navbar-mobile-link {
  @apply px-4 py-3 text-base font-medium no-underline transition-colors duration-200 border-b border-gray-100;
  color: #1f2937; /* Text Primary */
  background: transparent;
  border-left: none;
  border-right: none;
  border-top: none;
  text-align: left;
  font-family: var(--font-sans);
}

.navbar-mobile-link:hover {
  @apply bg-[var(--color-surface-100)]; /* Lavender Mist */
  color: var(--color-brand-600); /* Royal Violet */
}

.navbar-mobile-link--primary {
  @apply bg-[var(--color-brand-600)] text-white border-0;
}

.navbar-mobile-link--primary:hover {
  @apply bg-[var(--color-brand-800)]; /* Aubergine Ink */
  color: white;
}

.navbar-mobile-link--danger {
  color: var(--color-signal-warm-500); /* Sunset Clay */
}

.navbar-mobile-link--danger:hover {
  @apply bg-red-50;
  color: var(--color-signal-warm-500); /* Sunset Clay */
}

/* Overlay */
.navbar-overlay {
  @apply md:hidden fixed inset-0 z-30;
  background-color: rgba(0, 0, 0, 0.5); /* bg-black/50 equivalent */
  top: 70px; /* เริ่มจากใต้ header section เหมือน mobile menu */
}

/* Mobile Menu Transition */
.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.mobile-menu-enter-from,
.mobile-menu-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.mobile-menu-enter-to,
.mobile-menu-leave-from {
  opacity: 1;
  transform: translateY(0);
}

/* Overlay Transition */
.overlay-enter-active,
.overlay-leave-active {
  transition: opacity 0.2s ease;
}

.overlay-enter-from,
.overlay-leave-to {
  opacity: 0;
}

.overlay-enter-to,
.overlay-leave-from {
  opacity: 1;
}

/* Responsive Design */
@media (max-width: 768px) {
  .navbar-container {
    @apply py-6 px-4;
  }

  .navbar-header {
    @apply mb-4;
    position: relative;
  }

  .navbar-title {
    font-size: 1.875rem; /* 30px */
  }

  .navbar-search-section {
    @apply flex-col gap-4;
  }

  .navbar-search-wrapper {
    @apply w-full;
  }

  /* Mobile Menu */
  .navbar-mobile {
    top: 70px; /* navbar-container py-6 (24px) + title (~36px) + mb-4 (16px) = ~76px, ปัดเป็น 70px */
    padding-top: 0;
    max-height: calc(100vh - 70px);
  }

  /* Overlay - เริ่มจากใต้ header section */
  .navbar-overlay {
    top: 70px;
  }
}
</style>
