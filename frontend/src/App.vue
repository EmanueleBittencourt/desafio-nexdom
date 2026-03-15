<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import logo from '@/assets/media/Logo2.webp'
</script>

<template>
  <header class="top-nav">
    <div class="top-nav-inner">
      <RouterLink to="/" class="logo-link" aria-label="Home">
        <img :src="logo" alt="Logo" class="logo" />
      </RouterLink>
      <nav>
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/produtos">Produtos</RouterLink>
        <RouterLink to="/movimentacao">Movimentação de Estoque</RouterLink>
      </nav>
    </div>
  </header>

  <main class="main-content">
    <RouterView />
  </main>
</template>

<style scoped>
.top-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  background: var(--palette-verde-escuro);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.top-nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0.25rem 1rem; /* padding menor para caber logo maior */
  min-height: 64px; /* altura fixa do header */
  box-sizing: border-box;
}

.logo-link {
  display: flex;
  align-items: center;
  padding: 0;
  text-decoration: none;
  border-bottom: 2px solid transparent !important;
}

.logo-link:hover {
  border-bottom-color: transparent !important;
}

.logo {
  height: 56px;
  width: auto;
  display: block;
  object-fit: contain;
}

.top-nav nav {
  display: flex;
  align-items: center;
  gap: 0;
}

.main-content {
  padding-top: 64px; 
  min-height: 100vh;
}

.top-nav nav a {
  padding: 20px 30px;
  text-decoration: none;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  border-bottom: 2px solid transparent;
  transition: border-color 0.15s, color 0.15s;
}

.top-nav nav a:hover,
.top-nav nav a.router-link-exact-active {
  border-bottom-color: var(--palette-verde-claro);
  color: var(--palette-verde-claro);
}
</style>
