<template>
  <div class="dashboard-page">
    <h1 class="dashboard-title">Dashboard</h1>
    <p class="dashboard-placeholder">This page is under construction.</p>
  </div>
</template>

<script setup lang="ts">
// Placeholder page - will be implemented later
</script>

<style scoped>
@reference "tailwindcss";

.dashboard-page {
  @apply min-h-screen py-12 px-4;
  background: var(--color-surface-50);
}

.dashboard-title {
  @apply text-center mb-4 text-[var(--color-brand-600)];
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 2rem;
}

.dashboard-placeholder {
  @apply text-center text-gray-500;
  font-family: var(--font-sans);
}
</style>
