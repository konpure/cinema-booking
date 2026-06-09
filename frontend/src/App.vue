<template>
  <div id="app-root">
    <NavBar />
    <router-view v-slot="{ Component }">
      <keep-alive include="Home,Orders,MovieDetail">
        <component :is="Component" />
      </keep-alive>
    </router-view>
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
</script>
