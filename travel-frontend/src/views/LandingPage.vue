<template>
  <div class="landing-page">
    <h1>Landing Page</h1>
    <p>Router is working!</p>
    <p>Pinia Store is ready!</p>
    <div v-if="authStore.isAuthenticated" class="auth-info">
      <p>Status: Logged in</p>
      <p>Email: {{ authStore.user?.email }}</p>
    </div>
    <div v-else class="auth-info">
      <p>Status: Not logged in</p>
    </div>

    <!-- Test Component -->
    <TestApiClient />
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from "../stores/auth";
import TestApiClient from "../components/TestApiClient.vue";

const authStore = useAuthStore();
</script>

<style scoped>
.landing-page {
  padding: 2rem;
  text-align: center;
}

.landing-page h1 {
  color: #1e40af;
  margin-bottom: 1rem;
}

.landing-page p {
  color: #666;
  margin: 0.5rem 0;
}

.auth-info {
  margin-top: 2rem;
  padding: 1rem;
  background: #f3f4f6;
  border-radius: 8px;
  display: inline-block;
}
</style>
