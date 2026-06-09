<template>
  <div class="page" v-if="movie">
    <div class="detail-header">
      <img :src="movie.poster" class="poster" />
      <div class="meta">
        <h1>{{ movie.title }}</h1>
        <div class="tags">
          <el-tag>{{ movie.genre }}</el-tag>
          <el-tag type="warning">{{ movie.duration }} 分钟</el-tag>
          <el-tag type="success">{{ movie.rating }} 分</el-tag>
        </div>
        <p class="desc">{{ movie.description }}</p>
        <p v-if="cinemaName" class="cinema-line">
          <el-icon><Location /></el-icon> 当前影城：{{ cinemaName }}
        </p>
      </div>
    </div>
    <h2 class="section-title">选择场次</h2>
    <div class="screenings">
      <div v-for="s in screenings" :key="s.id" class="screening-item">
        <div class="time-block">
          <div class="time">{{ formatTime(s.startTime) }}</div>
          <div class="date">{{ formatDate(s.startTime) }}</div>
        </div>
        <div class="hall">{{ s.hallName }}</div>
        <div class="format-tag">2D · 国语</div>
        <div class="price">¥{{ s.price }}<span>起</span></div>
        <router-link :to="`/booking/${s.id}`">
          <el-button type="primary">选座购票</el-button>
        </router-link>
      </div>
      <el-empty v-if="!screenings.length" description="该影城暂无场次，请切换顶部影城">
        <router-link to="/"><el-button>返回首页</el-button></router-link>
      </el-empty>
    </div>
  </div>
  <div v-else-if="loadError" class="page">
    <el-empty :description="loadError">
      <router-link to="/"><el-button type="primary">返回首页</el-button></router-link>
    </el-empty>
  </div>
</template>

<script setup>
defineOptions({ name: 'MovieDetail' })
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Location } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { movieApi, screeningApi, cinemaApi } from '../api'
import { getCinemaIdParam } from '../composables/useCinema'

const route = useRoute()
const movie = ref(null)
const screenings = ref([])
const cinemaName = ref('')
const loadError = ref('')

async function loadDetail() {
  loadError.value = ''
  try {
    const id = route.params.id
    const cinemaId = getCinemaIdParam()
    const [mRes, sRes] = await Promise.all([
      movieApi.detail(id),
      screeningApi.list(id, cinemaId)
    ])
    movie.value = mRes.data
    screenings.value = sRes.data || []
    if (cinemaId) {
      const cRes = await cinemaApi.detail(cinemaId)
      cinemaName.value = cRes.data?.name || ''
    } else {
      cinemaName.value = ''
    }
  } catch (e) {
    loadError.value = e.message || '加载失败'
    ElMessage.error(loadError.value)
  }
}

onMounted(() => {
  loadDetail()
  window.addEventListener('cinema-changed', loadDetail)
})
watch(() => route.params.id, loadDetail)

function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
function formatDate(t) {
  return new Date(t).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', weekday: 'short' })
}
</script>

<style scoped>
.detail-header {
  display: flex;
  gap: 32px;
  margin-bottom: 40px;
}
.poster {
  width: 220px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.5);
}
.meta h1 { font-size: 2rem; margin-bottom: 16px; }
.tags { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.desc { color: var(--text-muted); line-height: 1.8; max-width: 600px; }
.cinema-line {
  display: flex; align-items: center; gap: 6px;
  margin-top: 16px; color: var(--gold); font-size: 14px;
}
.screenings { display: flex; flex-direction: column; gap: 12px; }
.screening-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 18px 20px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 12px;
  transition: border-color 0.2s;
}
.screening-item:hover { border-color: rgba(212,168,83,0.4); }
.time-block { min-width: 100px; }
.time { font-size: 1.3rem; font-weight: 600; color: var(--gold-light); }
.date { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
.hall { flex: 1; color: #fff; font-weight: 500; }
.format-tag { color: var(--text-muted); font-size: 13px; min-width: 80px; }
.price { font-size: 1.4rem; color: var(--gold); font-weight: 700; min-width: 90px; }
.price span { font-size: 12px; font-weight: 400; color: var(--text-muted); }
</style>
