<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <nav class="app-nav">
        <RouterLink class="nav-link" to="/">User Lookup</RouterLink>
        <RouterLink class="nav-link" to="/hello">Hello</RouterLink>
      </nav>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
}
.app-nav {
  display: flex;
  gap: 12px;
}
.nav-link {
  text-decoration: none;
  color: #111827;
  padding: 6px 10px;
  border-radius: 8px;
}
.nav-link.router-link-active {
  background: #111827;
  color: #ffffff;
}
.app-main {
  padding: 16px;
}
</style>

