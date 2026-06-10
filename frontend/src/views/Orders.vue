<template>
  <div class="page">
    <h2 class="section-title">我的订单</h2>
    <div v-if="showSkeleton"><el-skeleton :rows="4" animated /></div>
    <div v-else-if="orders.length" class="order-list">
      <div v-for="o in orders" :key="o.id" class="order-card">
        <img :src="o.poster" class="thumb" />
        <div class="info">
          <h3>{{ o.movieTitle }}</h3>
          <p class="cinema" v-if="o.cinemaName">
            <el-icon><Location /></el-icon> {{ o.cinemaName }}
          </p>
          <p>{{ o.hallName }} · {{ formatTime(o.startTime) }}</p>
          <p class="seats">座位：{{ o.seats?.join('、') || '—' }}</p>
          <p v-if="o.snacks?.length" class="snacks">卖品：{{ o.snacks.join('、') }}</p>
        </div>
        <div class="right">
          <div class="price">¥{{ o.totalPrice }}</div>
          <el-tag type="success" size="small">{{ o.status === 'PAID' ? '已支付' : o.status }}</el-tag>
          <p class="pickup">凭订单号前台取票</p>
          <p class="order-no">{{ o.orderNo }}</p>
        </div>
      </div>
    </div>
    <el-empty v-else-if="loadError" :description="loadError">
      <el-button type="primary" @click="loadOrders">重试</el-button>
    </el-empty>
    <el-empty v-else description="暂无订单">
      <router-link to="/"><el-button type="primary">去购票</el-button></router-link>
    </el-empty>
  </div>
</template>

<script setup>
defineOptions({ name: 'Orders' })
import { ref, computed, onMounted } from 'vue'
import { Location } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '../api'

const orders = ref([])
const loading = ref(true)
const loadError = ref('')
const showSkeleton = computed(() => loading.value && orders.value.length === 0)

onMounted(loadOrders)

async function loadOrders() {
  const silent = orders.value.length > 0
  if (!silent) loading.value = true
  loadError.value = ''
  try {
    const res = await orderApi.list()
    orders.value = res.data || []
  } catch (e) {
    loadError.value = e.message || '加载订单失败'
    ElMessage.error(loadError.value)
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN')
}
</script>

<style scoped>
.order-list { display: flex; flex-direction: column; gap: 16px; }
.order-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 12px;
}
.thumb { width: 80px; height: 120px; object-fit: cover; border-radius: 8px; }
.info { flex: 1; }
.info h3 { margin-bottom: 8px; }
.info p { color: var(--text-muted); font-size: 14px; margin-bottom: 4px; }
.cinema { color: var(--gold-light) !important; display: flex; align-items: center; gap: 4px; }
.seats { color: var(--gold) !important; }
.snacks { color: #c9a96e !important; }
.right { text-align: right; min-width: 120px; }
.price { font-size: 1.4rem; color: var(--gold); font-weight: 700; margin-bottom: 8px; }
.pickup { font-size: 11px; color: var(--text-muted); margin-top: 8px; }
.order-no { font-size: 11px; color: var(--text-muted); margin-top: 4px; font-family: monospace; }
</style>
