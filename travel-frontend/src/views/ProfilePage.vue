<template>
  <div class="profile-page">
    <h1 class="profile-title">Profile</h1>
    <p class="profile-placeholder">This page is under construction.</p>
  </div>
</template>

<script setup lang="ts">
// Placeholder page - will be implemented later
</script>

<style scoped>
@reference "tailwindcss";

.profile-page {
  @apply min-h-screen py-12 px-4;
  background: var(--color-surface-50);
}

.profile-title {
  @apply text-center mb-4 text-[var(--color-brand-600)];
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 2rem;
}

.profile-placeholder {
  @apply text-center text-gray-500;
  font-family: var(--font-sans);
}
</style>
