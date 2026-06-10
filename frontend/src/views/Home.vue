<template>
  <div class="page">
    <section class="hero">
      <div class="hero-content">
        <p class="hero-tag">星影旗下 · 全国连锁</p>
        <h1>选座购票 <em>一站式</em></h1>
        <p class="hero-desc">在线锁座 · 卖品加购 · 凭码取票</p>
      </div>
      <div v-if="currentCinema" class="cinema-banner">
        <img :src="currentCinema.cover" class="cinema-cover" alt="" />
        <div class="cinema-info">
          <h3>{{ currentCinema.name }}</h3>
          <p><el-icon><Location /></el-icon> {{ currentCinema.address }}</p>
          <p><el-icon><Phone /></el-icon> {{ currentCinema.phone }}</p>
        </div>
      </div>
    </section>

    <div class="section-head">
      <h2 class="section-title">正在热映</h2>
      <span class="hint">切换顶部影城可查看该店场次</span>
    </div>

    <div v-if="showSkeleton" class="loading"><el-skeleton :rows="3" animated /></div>
    <el-empty v-else-if="loadError" :description="loadError" />
    <div v-else class="movie-grid">
      <MovieCard v-for="m in movies" :key="m.id" :movie="m" />
    </div>

    <section class="snack-preview" v-if="snacks.length">
      <div class="section-head">
        <div>
          <h2 class="section-title">观影卖品</h2>
          <p class="hint">点击加减数量，选座购票时自动带入订单</p>
        </div>
        <el-tag v-if="cartTotal" type="warning">已选 {{ cartTotal }} 件卖品</el-tag>
      </div>
      <div class="snack-grid">
        <div
          v-for="s in snacks"
          :key="s.id"
          class="snack-card"
          :class="{ active: cart[s.id] > 0 }"
        >
          <img :src="s.image" :alt="s.name" />
          <div class="snack-body">
            <div class="snack-name">
              {{ s.name }}
              <el-tag v-if="s.category === 'COMBO'" size="small" type="warning">套餐</el-tag>
            </div>
            <p class="snack-desc">{{ s.description }}</p>
            <div class="snack-footer">
              <div class="snack-price">¥{{ s.price }}</div>
              <div class="snack-actions" @click.stop>
                <el-button size="small" circle :disabled="!cart[s.id]" @click="changeQty(s.id, -1)">−</el-button>
                <span class="qty">{{ cart[s.id] || 0 }}</span>
                <el-button size="small" circle type="primary" @click="changeQty(s.id, 1)">+</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="cartTotal" class="snack-cta">
        <span>已预选 {{ cartTotal }} 件卖品</span>
        <el-button type="primary" @click="goBook">去选座加购</el-button>
      </div>
    </section>

    <section class="arch-banner">
      <h3>架构技术集成（课程答辩）</h3>
      <div class="tags">
        <span>Spring Security</span><span>Redis</span><span>RabbitMQ</span>
        <span>Nginx LB</span><span>Vue3</span><span>ECharts</span>
      </div>
    </section>
  </div>
</template>

<script setup>
defineOptions({ name: 'Home' })
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Location, Phone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { movieApi, snackApi, cinemaApi } from '../api'
import { useCinema, getCinemaIdParam } from '../composables/useCinema'
import { getSnackCart, addToSnackCart, snackCartCount } from '../composables/useSnackCart'
import MovieCard from '../components/MovieCard.vue'

const router = useRouter()
const movies = ref([])
const snacks = ref([])
const loading = ref(true)
const loadError = ref('')
const showSkeleton = computed(() => loading.value && movies.value.length === 0)
const cart = reactive({ ...getSnackCart() })
const { selectedCinemaId, cinemas } = useCinema()

const cartTotal = computed(() => snackCartCount())

const currentCinema = computed(() =>
  cinemas.value.find(c => String(c.id) === String(selectedCinemaId.value))
)

function syncCart() {
  Object.keys(cart).forEach(k => delete cart[k])
  Object.assign(cart, getSnackCart())
}

function changeQty(id, delta) {
  addToSnackCart(id, delta)
  syncCart()
}

function goBook() {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.info('请先登录后再选座购票')
    router.push('/login')
    return
  }
  if (movies.value.length) {
    router.push(`/movie/${movies.value[0].id}`)
  } else {
    ElMessage.warning('暂无热映影片')
  }
}

onMounted(() => {
  syncCart()
  loadData()
  window.addEventListener('cinema-changed', loadData)
  window.addEventListener('snack-cart-changed', syncCart)
})
onUnmounted(() => {
  window.removeEventListener('cinema-changed', loadData)
  window.removeEventListener('snack-cart-changed', syncCart)
})

async function loadData() {
  const silent = movies.value.length > 0
  if (!silent) loading.value = true
  loadError.value = ''
  try {
    if (!cinemas.value.length) {
      const cRes = await cinemaApi.list()
      cinemas.value = cRes.data || []
    }
    const cinemaId = getCinemaIdParam()
    const [mRes, sRes] = await Promise.all([movieApi.list(cinemaId), snackApi.list()])
    movies.value = mRes.data || []
    snacks.value = sRes.data || []
  } catch (e) {
    loadError.value = e.message || '加载失败'
    ElMessage.error(loadError.value)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 36px;
  align-items: stretch;
}
.hero-content {
  padding: 40px 24px;
  background: radial-gradient(ellipse at left, rgba(212,168,83,0.12) 0%, transparent 70%);
  border-radius: 16px;
  border: 1px solid var(--border);
}
.hero-tag { color: var(--gold); font-size: 13px; letter-spacing: 2px; margin-bottom: 12px; }
.hero-content h1 { font-size: 2.2rem; font-weight: 300; }
.hero-content h1 em { font-style: normal; color: var(--gold); font-weight: 700; }
.hero-desc { margin-top: 12px; color: var(--text-muted); }
.cinema-banner {
  display: flex;
  gap: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
}
.cinema-cover { width: 140px; object-fit: cover; }
.cinema-info { padding: 20px 16px 20px 0; display: flex; flex-direction: column; justify-content: center; }
.cinema-info h3 { color: var(--gold-light); margin-bottom: 10px; font-size: 1rem; }
.cinema-info p { display: flex; align-items: center; gap: 6px; color: var(--text-muted); font-size: 13px; margin-bottom: 6px; }
.section-head { display: flex; align-items: baseline; gap: 12px; margin-bottom: 20px; }
.hint { color: var(--text-muted); font-size: 13px; }
.movie-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}
.snack-preview { margin-top: 48px; }
.snack-preview .section-head { margin-bottom: 16px; }
.snack-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.snack-card {
  display: flex;
  gap: 14px;
  padding: 14px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 12px;
  transition: border-color 0.2s, box-shadow 0.2s;
  cursor: default;
}
.snack-card.active {
  border-color: var(--gold);
  box-shadow: 0 0 0 1px rgba(212,168,83,0.25);
}
.snack-card img { width: 72px; height: 72px; border-radius: 8px; object-fit: cover; flex-shrink: 0; }
.snack-body { flex: 1; min-width: 0; }
.snack-name { font-weight: 600; margin-bottom: 4px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.snack-desc { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; line-height: 1.5; }
.snack-footer { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.snack-price { color: var(--gold); font-weight: 700; font-size: 1.1rem; }
.snack-actions { display: flex; align-items: center; gap: 8px; }
.snack-actions .qty { min-width: 24px; text-align: center; font-weight: 600; color: #fff; }
.snack-cta {
  margin-top: 16px;
  padding: 14px 18px;
  border: 1px dashed var(--gold);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--gold-light);
}
.arch-banner {
  margin-top: 48px;
  padding: 24px;
  border: 1px dashed var(--border);
  border-radius: 12px;
  text-align: center;
}
.arch-banner h3 { color: var(--gold-light); margin-bottom: 12px; font-weight: 400; }
.tags { display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }
.tags span {
  padding: 4px 14px;
  border: 1px solid var(--border);
  border-radius: 20px;
  font-size: 13px;
  color: var(--text-muted);
}
@media (max-width: 768px) {
  .hero { grid-template-columns: 1fr; }
}
</style>
