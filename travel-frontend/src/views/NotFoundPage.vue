<template>
  <div class="not-found-page">
    <h1>404</h1>
    <p>Page not found</p>
    <router-link to="/" class="home-link">Go to Home</router-link>
  </div>
</template>

<script setup lang="ts">
// NotFound page
</script>

<style scoped>
.not-found-page {
  padding: 2rem;
  text-align: center;
}

.not-found-page h1 {
  font-size: 4rem;
  color: #1e40af;
  margin-bottom: 1rem;
}

.not-found-page p {
  color: #666;
  margin-bottom: 2rem;
}

.home-link {
  color: #1e40af;
  text-decoration: none;
  font-weight: 500;
}

.home-link:hover {
  text-decoration: underline;
}
</style>
