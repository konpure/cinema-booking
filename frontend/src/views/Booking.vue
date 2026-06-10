<template>
  <div class="page booking-page" v-if="screening">
    <div class="booking-header">
      <div>
        <h1>选座购票</h1>
        <p>{{ movie?.title }} · {{ screening.hallName }}</p>
        <p class="sub">{{ cinemaName }} · {{ formatTime(screening.startTime) }}</p>
      </div>
      <div class="step-tags">
        <el-tag type="warning">1 选座</el-tag>
        <el-tag>2 卖品</el-tag>
        <el-tag>3 支付</el-tag>
      </div>
    </div>

    <div class="booking-layout">
      <el-card class="seat-panel">
        <SeatMap
          :rows="seatMap.rows"
          :cols="seatMap.cols"
          :sold="seatMap.sold"
          :locked="seatMap.locked"
          :my-locked="seatMap.myLocked"
          :selected="selected"
          @toggle="toggleSeat"
        />
      </el-card>

      <aside class="side-panel">
        <el-card class="order-summary">
          <h3>订单明细</h3>
          <div class="line"><span>电影票</span><span>{{ selected.length }} 张 · ¥{{ ticketPrice }}</span></div>
          <div class="line"><span>卖品</span><span>¥{{ snackPrice }}</span></div>
          <div class="line total"><span>合计</span><span>¥{{ grandTotal }}</span></div>
        </el-card>

        <el-card class="snack-panel">
          <h3>加购卖品 <span class="optional">（可选）</span></h3>
          <div v-for="s in snacks" :key="s.id" class="snack-row">
            <img :src="s.image" :alt="s.name" />
            <div class="snack-info">
              <div class="name">
                {{ s.name }}
                <el-tag v-if="s.category === 'COMBO'" size="small" type="warning">套餐</el-tag>
              </div>
              <div class="price">¥{{ s.price }}</div>
            </div>
            <el-input-number
              v-model="snackQty[s.id]"
              :min="0"
              :max="10"
              size="small"
              @change="onSnackChange"
            />
          </div>
        </el-card>

        <div class="actions">
          <el-button size="large" @click="refreshSeats">刷新座位</el-button>
          <el-button
            type="primary"
            size="large"
            :disabled="!selected.length"
            :loading="submitting"
            @click="confirmBooking"
          >
            确认支付 ¥{{ grandTotal }}
          </el-button>
        </div>
        <p class="tip">支付成功后凭订单号至影城前台取票及领取卖品</p>
      </aside>
    </div>
  </div>
  <div v-else-if="loadError" class="page">
    <el-empty :description="loadError">
      <router-link to="/"><el-button type="primary">返回首页</el-button></router-link>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { screeningApi, movieApi, bookingApi, snackApi, cinemaApi } from '../api'
import { getSnackCart, clearSnackCart, setSnackCart } from '../composables/useSnackCart'
import SeatMap from '../components/SeatMap.vue'

const route = useRoute()
const router = useRouter()
const screening = ref(null)
const movie = ref(null)
const cinemaName = ref('')
const seatMap = ref({ rows: 8, cols: 12, sold: [], locked: [], myLocked: [] })
const selected = ref([])
const snacks = ref([])
const snackQty = reactive({})
const submitting = ref(false)
const loadError = ref('')
let pollTimer = null

const ticketPrice = computed(() =>
  screening.value ? (selected.value.length * Number(screening.value.price)).toFixed(2) : '0.00'
)
const snackPrice = computed(() => {
  let total = 0
  for (const s of snacks.value) {
    total += Number(s.price) * (snackQty[s.id] || 0)
  }
  return total.toFixed(2)
})
const grandTotal = computed(() => (Number(ticketPrice.value) + Number(snackPrice.value)).toFixed(2))

onMounted(async () => {
  try {
    const id = route.params.screeningId
    const [sRes, snRes] = await Promise.all([screeningApi.detail(id), snackApi.list()])
    screening.value = sRes.data
    snacks.value = snRes.data || []
    const pending = getSnackCart()
    snacks.value.forEach(s => {
      snackQty[s.id] = pending[s.id] || 0
    })
    const mRes = await movieApi.detail(screening.value.movieId)
    movie.value = mRes.data
    if (screening.value.cinemaId) {
      const cRes = await cinemaApi.detail(screening.value.cinemaId)
      cinemaName.value = cRes.data?.name || ''
    }
    await refreshSeats()
    pollTimer = setInterval(refreshSeats, 5000)
  } catch (e) {
    loadError.value = e.message || '加载场次失败'
  }
})

onUnmounted(() => clearInterval(pollTimer))

async function refreshSeats() {
  try {
    const res = await bookingApi.seats(route.params.screeningId)
    seatMap.value = {
      rows: res.data.rows,
      cols: res.data.cols,
      sold: res.data.sold || [],
      locked: res.data.locked || [],
      myLocked: res.data.myLocked || []
    }
  } catch (e) {
    if (!submitting.value) ElMessage.error(e.message || '刷新座位失败')
  }
}

function toggleSeat(row, col) {
  const idx = selected.value.findIndex(s => s.row === row && s.col === col)
  if (idx >= 0) selected.value.splice(idx, 1)
  else selected.value.push({ row, col })
}

function buildSnackItems() {
  return snacks.value
    .filter(s => (snackQty[s.id] || 0) > 0)
    .map(s => ({ snackId: s.id, quantity: snackQty[s.id] }))
}

function onSnackChange() {
  const cart = {}
  for (const s of snacks.value) {
    if (snackQty[s.id] > 0) cart[s.id] = snackQty[s.id]
  }
  setSnackCart(cart)
}

async function confirmBooking() {
  if (!selected.value.length) return
  submitting.value = true
  const payload = {
    screeningId: Number(route.params.screeningId),
    seats: [...selected.value],
    snacks: buildSnackItems()
  }
  let lockToken = ''
  try {
    const lockRes = await bookingApi.lock(payload)
    lockToken = lockRes.data.lockToken
    const submitRes = await bookingApi.submit({ ...payload, lockToken })
    ElMessage.success(`购票成功！订单号 ${submitRes.data.orderNo || ''}`)
    clearSnackCart()
    setTimeout(() => router.push('/orders'), 800)
  } catch (e) {
    ElMessage.error(e.message || '购票失败')
    if (lockToken) {
      try { await bookingApi.release({ ...payload, lockToken }) } catch (_) {}
    }
    selected.value = []
    await refreshSeats()
  } finally {
    submitting.value = false
  }
}

function formatTime(t) {
  return new Date(t).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 16px;
  flex-wrap: wrap;
}
.booking-header h1 { color: var(--gold-light); margin-bottom: 8px; }
.booking-header p { color: var(--text-muted); }
.sub { color: var(--gold) !important; margin-top: 4px; font-size: 14px; }
.step-tags { display: flex; gap: 8px; }
.booking-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
  align-items: start;
}
.side-panel { display: flex; flex-direction: column; gap: 16px; }
.order-summary h3, .snack-panel h3 {
  color: var(--gold-light);
  margin-bottom: 14px;
  font-size: 1rem;
}
.optional { font-size: 12px; color: var(--text-muted); font-weight: 400; }
.line {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
  color: var(--text-muted);
}
.line.total {
  border-bottom: none;
  padding-top: 12px;
  font-size: 1.1rem;
  color: #fff;
  font-weight: 700;
}
.line.total span:last-child { color: var(--gold); }
.snack-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}
.snack-row:last-child { border-bottom: none; }
.snack-row img { width: 48px; height: 48px; border-radius: 8px; object-fit: cover; }
.snack-info { flex: 1; }
.snack-info .name { font-size: 13px; display: flex; align-items: center; gap: 6px; }
.snack-info .price { color: var(--gold); font-size: 13px; margin-top: 2px; }
.actions { display: flex; flex-direction: column; gap: 10px; }
.tip { font-size: 12px; color: var(--text-muted); text-align: center; line-height: 1.6; }
@media (max-width: 900px) {
  .booking-layout { grid-template-columns: 1fr; }
}
</style>
