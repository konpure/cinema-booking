<template>
  <header class="navbar">
    <div class="nav-inner">
      <router-link to="/" class="brand">
        <span class="brand-icon">★</span>
        <div class="brand-text">
          <span class="brand-name">星影 Cinema</span>
          <span class="brand-sub">在线选座 · 卖品自取</span>
        </div>
      </router-link>

      <div v-if="cinemas.length" class="cinema-picker">
        <el-icon><Location /></el-icon>
        <el-select
          v-model="selectedCinemaId"
          placeholder="选择影城"
          size="default"
          style="width: 240px"
          @change="onCinemaChange"
        >
          <el-option
            v-for="c in cinemas"
            :key="c.id"
            :label="c.name"
            :value="String(c.id)"
          />
        </el-select>
      </div>

      <nav class="nav-links">
        <router-link to="/">热映影片</router-link>
        <router-link v-if="token" to="/orders">我的订单</router-link>
        <router-link v-if="role === 'ADMIN'" to="/admin">管理后台</router-link>
      </nav>
      <div class="nav-user">
        <template v-if="token">
          <span class="username">{{ username }}</span>
          <el-button size="small" @click="logout">退出</el-button>
        </template>
        <router-link v-else to="/login">
          <el-button type="primary" size="small">登录</el-button>
        </router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Location } from '@element-plus/icons-vue'
import { cinemaApi } from '../api'
import { useCinema } from '../composables/useCinema'

const router = useRouter()
const token = ref(localStorage.getItem('token'))
const username = ref(localStorage.getItem('username'))
const role = ref(localStorage.getItem('role'))
const { selectedCinemaId, cinemas, setCinema, syncFromStorage } = useCinema()

onMounted(async () => {
  window.addEventListener('storage', sync)
  window.addEventListener('cinema-changed', sync)
  try {
    const res = await cinemaApi.list()
    cinemas.value = res.data || []
    if (!selectedCinemaId.value && cinemas.value.length) {
      setCinema(cinemas.value[0].id)
    }
  } catch (_) { /* ignore */ }
})

function sync() {
  token.value = localStorage.getItem('token')
  username.value = localStorage.getItem('username')
  role.value = localStorage.getItem('role')
  syncFromStorage()
}

function onCinemaChange(id) {
  setCinema(id)
  if (router.currentRoute.value.path !== '/') {
    router.push('/')
  }
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  sync()
  router.push('/login')
}

router.afterEach(sync)
</script>

<style scoped>
.navbar {
  background: rgba(10, 10, 15, 0.96);
  border-bottom: 1px solid var(--border);
  backdrop-filter: blur(12px);
  position: sticky;
  top: 0;
  z-index: 100;
}
.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 160px;
}
.brand-icon { font-size: 1.5rem; color: var(--gold); }
.brand-text { display: flex; flex-direction: column; line-height: 1.2; }
.brand-name { font-size: 1.1rem; font-weight: 700; color: var(--gold-light); }
.brand-sub { font-size: 11px; color: var(--text-muted); }
.cinema-picker {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--gold);
}
.nav-links {
  flex: 1;
  display: flex;
  gap: 20px;
  min-width: 200px;
}
.nav-links a {
  color: var(--text-muted);
  transition: color 0.2s;
  font-size: 14px;
}
.nav-links a.router-link-active { color: var(--gold); }
.nav-user { display: flex; align-items: center; gap: 12px; margin-left: auto; }
.username { color: var(--text-muted); font-size: 14px; }
</style>
