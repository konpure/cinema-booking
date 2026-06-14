<template>
  <div class="page admin">
    <div class="admin-head">
      <h2 class="section-title">管理后台</h2>
      <el-button @click="loadAll" :loading="loading">刷新数据</el-button>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6"><el-card shadow="never"><div class="stat-num">{{ stats.totalOrders || 0 }}</div><div class="stat-label">总订单</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="stat-num">{{ stats.totalUsers || 0 }}</div><div class="stat-label">用户数</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="stat-num">{{ stats.totalMovies || 0 }}</div><div class="stat-label">在映影片</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="stat-num gold">27</div><div class="stat-label">架构技术已集成</div></el-card></el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12"><div ref="revenueChartRef" class="chart"></div></el-col>
      <el-col :span="12"><div ref="movieChartRef" class="chart"></div></el-col>
    </el-row>

    <el-tabs v-model="tab" class="admin-tabs">
      <el-tab-pane label="影片管理" name="movies">
        <div class="tab-toolbar">
          <el-button type="primary" @click="openMovieDialog()">新增影片</el-button>
        </div>
        <el-table :data="movies" v-loading="loading" empty-text="暂无影片">
          <el-table-column prop="title" label="片名" min-width="120" />
          <el-table-column prop="genre" label="类型" width="80" />
          <el-table-column prop="rating" label="评分" width="70" />
          <el-table-column prop="status" label="状态" width="90" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openMovieDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="delMovie(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="场次管理" name="screenings">
        <div class="tab-toolbar">
          <el-button type="primary" @click="openScreeningDialog()">新增场次</el-button>
        </div>
        <el-table :data="screenings" v-loading="loading" empty-text="暂无场次">
          <el-table-column prop="hallName" label="影厅" width="120" />
          <el-table-column label="影片" min-width="100">
            <template #default="{ row }">{{ movieName(row.movieId) }}</template>
          </el-table-column>
          <el-table-column label="影城" min-width="140">
            <template #default="{ row }">{{ cinemaName(row.cinemaId) }}</template>
          </el-table-column>
          <el-table-column label="开场时间" min-width="160">
            <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
          </el-table-column>
          <el-table-column prop="price" label="票价" width="80" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openScreeningDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="delScreening(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="卖品管理" name="snacks">
        <div class="tab-toolbar">
          <el-button type="primary" @click="openSnackDialog()">新增卖品</el-button>
        </div>
        <el-table :data="snacks" v-loading="loading" empty-text="暂无卖品">
          <el-table-column prop="name" label="名称" min-width="120" />
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.category === 'COMBO'" type="warning" size="small">套餐</el-tag>
              <el-tag v-else size="small">单品</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="80" />
          <el-table-column prop="status" label="状态" width="90" />
          <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openSnackDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="delSnack(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="影城管理" name="cinemas">
        <div class="tab-toolbar">
          <el-button type="primary" @click="openCinemaDialog()">新增影城</el-button>
        </div>
        <el-table :data="cinemas" v-loading="loading" empty-text="暂无影城">
          <el-table-column prop="name" label="名称" min-width="160" />
          <el-table-column prop="city" label="城市" width="80" />
          <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
          <el-table-column prop="phone" label="电话" width="120" />
          <el-table-column prop="status" label="状态" width="80" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openCinemaDialog(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="订单查看" name="orders">
        <el-table :data="orders" v-loading="loading" empty-text="暂无订单">
          <el-table-column prop="orderNo" label="订单号" min-width="160" />
          <el-table-column prop="username" label="用户" width="90" />
          <el-table-column prop="movieTitle" label="影片" min-width="100" />
          <el-table-column prop="cinemaName" label="影城" min-width="140" show-overflow-tooltip />
          <el-table-column prop="totalPrice" label="金额" width="80" />
          <el-table-column prop="status" label="状态" width="80" />
          <el-table-column label="时间" min-width="160">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
          <el-tab-pane label="架构技术" name="tech">
        <div class="tech-container">
          <p class="tech-summary">本项目共集成 <strong>27</strong> 种技术栈，覆盖 9 大板块，各板块技术及说明如下：</p>
          <h4 class="tech-section-title">板块1 — 安全认证</h4>
          <el-table :data="techSec1" style="margin-bottom:20px">
            <el-table-column prop="name" label="技术" width="200" />
            <el-table-column prop="desc" label="说明" />
          </el-table>
          <h4 class="tech-section-title">板块2 — 数据层</h4>
          <el-table :data="techSec2" style="margin-bottom:20px">
            <el-table-column prop="name" label="技术" width="200" />
            <el-table-column prop="desc" label="说明" />
          </el-table>
          <h4 class="tech-section-title">板块3 — 消息队列</h4>
          <el-table :data="techSec3" style="margin-bottom:20px">
            <el-table-column prop="name" label="技术" width="200" />
            <el-table-column prop="desc" label="说明" />
          </el-table>
          <h4 class="tech-section-title">板块4 — 网关高可用</h4>
          <el-table :data="techSec4" style="margin-bottom:20px">
            <el-table-column prop="name" label="技术" width="200" />
            <el-table-column prop="desc" label="说明" />
          </el-table>
          <h4 class="tech-section-title">板块5 — 容器化</h4>
          <el-table :data="techSec5" style="margin-bottom:20px">
            <el-table-column prop="name" label="技术" width="200" />
            <el-table-column prop="desc" label="说明" />
          </el-table>
          <h4 class="tech-section-title">板块6 — 前端技术</h4>
          <el-table :data="techSec6" style="margin-bottom:20px">
            <el-table-column prop="name" label="技术" width="200" />
            <el-table-column prop="desc" label="说明" />
          </el-table>
          <h4 class="tech-section-title">板块7 — 可观测性</h4>
          <el-table :data="techSec7" style="margin-bottom:20px">
            <el-table-column prop="name" label="技术" width="200" />
            <el-table-column prop="desc" label="说明" />
          </el-table>
          <h4 class="tech-section-title">板块8 — 服务治理</h4>
          <el-table :data="techSec8" style="margin-bottom:20px">
            <el-table-column prop="name" label="技术" width="200" />
            <el-table-column prop="desc" label="说明" />
          </el-table>
          <h4 class="tech-section-title">板块9 — 工程化</h4>
          <el-table :data="techSec9" style="margin-bottom:20px">
            <el-table-column prop="name" label="技术" width="200" />
            <el-table-column prop="desc" label="说明" />
          </el-table>
        </div>

      </el-tab-pane>
    </el-tabs>

    <!-- 影片 -->
    <el-dialog v-model="movieDialog" :title="movieForm.id ? '编辑影片' : '新增影片'" width="520px" destroy-on-close>
      <el-form :model="movieForm" label-width="70px">
        <el-form-item label="片名" required><el-input v-model="movieForm.title" placeholder="请输入片名" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="movieForm.genre" /></el-form-item>
        <el-form-item label="时长"><el-input-number v-model="movieForm.duration" :min="60" style="width:100%" /></el-form-item>
        <el-form-item label="评分"><el-input-number v-model="movieForm.rating" :min="0" :max="10" :step="0.1" style="width:100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="movieForm.status" style="width:100%">
            <el-option label="热映 SHOWING" value="SHOWING" />
            <el-option label="下架 OFF" value="OFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="海报"><el-input v-model="movieForm.poster" placeholder="图片 URL" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="movieForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="movieDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveMovie">保存</el-button>
      </template>
    </el-dialog>

    <!-- 场次 -->
    <el-dialog v-model="screeningDialog" :title="screeningForm.id ? '编辑场次' : '新增场次'" width="520px" destroy-on-close>
      <el-form :model="screeningForm" label-width="70px">
        <el-form-item label="影片" required>
          <el-select v-model="screeningForm.movieId" style="width:100%" placeholder="选择影片">
            <el-option v-for="m in movies" :key="m.id" :label="m.title" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="影城" required>
          <el-select v-model="screeningForm.cinemaId" style="width:100%" placeholder="选择影城">
            <el-option v-for="c in cinemas" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="影厅"><el-input v-model="screeningForm.hallName" /></el-form-item>
        <el-form-item label="开场"><el-date-picker v-model="screeningForm.startTime" type="datetime" style="width:100%" /></el-form-item>
        <el-form-item label="票价"><el-input-number v-model="screeningForm.price" :min="1" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="screeningDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveScreening">保存</el-button>
      </template>
    </el-dialog>

    <!-- 卖品 -->
    <el-dialog v-model="snackDialog" :title="snackForm.id ? '编辑卖品' : '新增卖品'" width="520px" destroy-on-close>
      <el-form :model="snackForm" label-width="70px">
        <el-form-item label="名称" required><el-input v-model="snackForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="snackForm.category" style="width:100%">
            <el-option label="单品 SINGLE" value="SINGLE" />
            <el-option label="套餐 COMBO" value="COMBO" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格"><el-input-number v-model="snackForm.price" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="snackForm.status" style="width:100%">
            <el-option label="在售 ON_SALE" value="ON_SALE" />
            <el-option label="下架 OFF" value="OFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片"><el-input v-model="snackForm.image" placeholder="图片 URL" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="snackForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="snackDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveSnack">保存</el-button>
      </template>
    </el-dialog>

    <!-- 影城 -->
    <el-dialog v-model="cinemaDialog" :title="cinemaForm.id ? '编辑影城' : '新增影城'" width="520px" destroy-on-close>
      <el-form :model="cinemaForm" label-width="70px">
        <el-form-item label="名称" required><el-input v-model="cinemaForm.name" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="cinemaForm.city" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="cinemaForm.address" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="cinemaForm.phone" /></el-form-item>
        <el-form-item label="封面"><el-input v-model="cinemaForm.cover" placeholder="图片 URL" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="cinemaForm.status" style="width:100%">
            <el-option label="营业 OPEN" value="OPEN" />
            <el-option label="关闭 CLOSED" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cinemaDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCinema">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../api'

const stats = ref({})
const movies = ref([])
const screenings = ref([])
const snacks = ref([])
const cinemas = ref([])
const orders = ref([])
const tab = ref('movies')
const loading = ref(false)
const saving = ref(false)

const revenueChartRef = ref(null)
const movieChartRef = ref(null)
let revenueChartInst = null
let movieChartInst = null

const movieDialog = ref(false)
const screeningDialog = ref(false)
const snackDialog = ref(false)
const cinemaDialog = ref(false)

const movieForm = reactive({ id: null, title: '', genre: '剧情', duration: 120, rating: 8, poster: '', description: '', status: 'SHOWING' })
const screeningForm = reactive({ id: null, movieId: null, cinemaId: null, hallName: '激光厅', startTime: new Date(), price: 48, seatRows: 8, seatCols: 12, status: 'OPEN' })
const snackForm = reactive({ id: null, name: '', category: 'SINGLE', price: 28, description: '', image: '', status: 'ON_SALE' })
const cinemaForm = reactive({ id: null, name: '', city: '北京', address: '', phone: '', cover: '', status: 'OPEN' })


// === 架构技术 27种 ===
const techSec1 = [
  { name: 'Spring Security', desc: '登录认证、角色授权与URL级权限控制' },
  { name: '拦截器（Interceptor）', desc: '请求日志记录、跨域处理与接口权限校验' },
  { name: 'JWT', desc: 'JJWT签发与验证Token，无状态认证与自定义载荷' },
]
const techSec2 = [
  { name: 'Redis', desc: '缓存座位锁定、用户Session，支持持久化与过期策略' },
  { name: '读写分离（MySQL）', desc: '一主一从架构，写主库读从库，提升并发读性能' },
  { name: 'Kafka', desc: '订单消息异步推送，实现创建与通知解耦' },
]
const techSec3 = [
  { name: 'ZooKeeper', desc: '分布式协调服务，管理RabbitMQ集群元数据' },
  { name: 'RabbitMQ', desc: '订单超时取消、异步通知，支持延迟队列' },
  { name: 'HighCharts', desc: '管理后台展示消息吞吐量、队列积压监控图' },
]
const techSec4 = [
  { name: 'Nginx', desc: '反向代理后端，静态缓存、HTTPS终止与请求限流' },
  { name: '负载均衡', desc: 'Nginx upstream多节点轮询，提升系统可用性' },
  { name: 'Sentinel', desc: 'Dashboard监控接口流量，熔断降级与限流规则' },
]
const techSec5 = [
  { name: 'Docker', desc: '容器化部署各服务，统一运行环境简化运维' },
  { name: 'K8s（Kubernetes）', desc: '编排容器集群，自动扩缩容与服务发现' },
  { name: 'HybridFlask', desc: '混合Python/Flask辅助监控，对接Prometheus' },
]
const techSec6 = [
  { name: 'Vue 3', desc: 'Composition API构建SPA，路由守卫控制页面访问' },
  { name: 'ECharts', desc: '集票房柱状图与热门影片饼图，响应式更新' },
  { name: 'Memcached', desc: '仅K-V缓存、无持久化，适合高吞吐只读场景（对比分析）' },
]
const techSec7 = [
  { name: 'SLF4J / Logback', desc: '统一日志门面，按天滚动归档，MDC追踪请求链路' },
  { name: 'Elasticsearch', desc: '集中存储日志，全文检索与聚合分析快速定位问题' },
  { name: 'Prometheus', desc: '采集JVM指标、QPS响应时间，Grafana可视化面板' },
]
const techSec8 = [
  { name: 'Nacos', desc: '注册中心与配置管理，服务动态发现与配置热更新' },
  { name: 'Actuator', desc: '健康检查与指标端点，对接Prometheus监控系统' },
  { name: 'Docker Compose', desc: '编排多容器服务依赖，一键启动开发环境' },
]
const techSec9 = [
  { name: 'MyBatis-Plus', desc: '代码生成器自动生成Mapper，Lambda条件构造器简化SQL' },
  { name: 'JUnit', desc: 'Service层单元测试集成测试，覆盖核心业务逻辑' },
  { name: 'Spring Boot', desc: '项目基础框架，自动配置简化开发，提供RESTful API' },
]
onMounted(loadAll)
onUnmounted(() => {
  revenueChartInst?.dispose()
  movieChartInst?.dispose()
  window.removeEventListener('resize', resizeCharts)
})

async function loadAll() {
  loading.value = true
  try {
    const [sRes, mRes, scRes, snRes, cRes, oRes] = await Promise.all([
      adminApi.stats(), adminApi.movies(), adminApi.screenings(),
      adminApi.snacks(), adminApi.cinemas(), adminApi.orders()
    ])
    stats.value = sRes.data || {}
    movies.value = mRes.data || []
    screenings.value = scRes.data || []
    snacks.value = snRes.data || []
    cinemas.value = cRes.data || []
    orders.value = oRes.data || []
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error(e.message || '加载管理数据失败')
  } finally {
    loading.value = false
  }
}

function pickField(obj, ...keys) {
  for (const k of keys) {
    if (obj && obj[k] != null) return obj[k]
  }
  return null
}

function renderCharts() {
  renderRevenueChart()
  renderMovieChart()
  window.removeEventListener('resize', resizeCharts)
  window.addEventListener('resize', resizeCharts)
}

function resizeCharts() {
  revenueChartInst?.resize()
  movieChartInst?.resize()
}

function renderRevenueChart() {
  if (!revenueChartRef.value) return
  revenueChartInst?.dispose()
  revenueChartInst = echarts.init(revenueChartRef.value)
  // loading animation
  revenueChartInst.showLoading({
    text: '加载中...',
    color: '#d4a853',
    textColor: '#8a8a9a',
    maskColor: 'rgba(20,20,28,0.8)'
  })
  const raw = stats.value.revenueByDay || []
  if (!raw.length) {
    revenueChartInst.hideLoading()
    revenueChartInst.setOption({
      backgroundColor: 'transparent',
      title: { text: '近7日票房', textStyle: { color: '#d4a853', fontSize: 14 } },
      graphic: [{
        type: 'text',
        left: 'center',
        top: 'middle',
        style: { text: '暂无票房数据\n完成购票后即可显示', fill: '#8a8a9a', fontSize: 14, textAlign: 'center' }
      }]
    })
    return
  }
  const data = [...raw].reverse()
  const dates = data.map(d => String(pickField(d, 'date', 'DATE') || ''))
  const values = data.map(d => Number(pickField(d, 'revenue', 'REVENUE') || 0))
  revenueChartInst.hideLoading()
  revenueChartInst.setOption({
    backgroundColor: 'transparent',
    title: { text: '近7日票房', textStyle: { color: '#d4a853', fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 20, top: 48, bottom: 32 },
    xAxis: { type: 'category', data: dates, axisLabel: { color: '#8a8a9a' } },
    yAxis: { type: 'value', axisLabel: { color: '#8a8a9a' }, splitLine: { lineStyle: { color: '#2a2a3a' } } },
    series: [{ name: '票房', type: 'bar', data: values, itemStyle: { color: '#d4a853', borderRadius: [4, 4, 0, 0] } }]
  })
  revenueChartInst.resize()
}

function renderMovieChart() {
  if (!movieChartRef.value) return
  movieChartInst?.dispose()
  movieChartInst = echarts.init(movieChartRef.value)
  // loading animation
  movieChartInst.showLoading({
    text: '加载中...',
    color: '#d4a853',
    textColor: '#8a8a9a',
    maskColor: 'rgba(20,20,28,0.8)'
  })
  const raw = stats.value.topMovies || []
  if (!raw.length) {
    movieChartInst.hideLoading()
    movieChartInst.setOption({
      backgroundColor: 'transparent',
      title: { text: '热门影片 TOP5', textStyle: { color: '#d4a853', fontSize: 14 } },
      graphic: [{
        type: 'text',
        left: 'center',
        top: 'middle',
        style: { text: '暂无订单数据\n完成购票后即可查看', fill: '#8a8a9a', fontSize: 14, textAlign: 'center' }
      }]
    })
    return
  }
  const pieData = raw.map(d => ({
    name: pickField(d, 'title', 'TITLE') || '未知',
    value: Number(pickField(d, 'revenue', 'REVENUE') || 0)
  }))
  movieChartInst.hideLoading()
  movieChartInst.setOption({
    backgroundColor: 'transparent',
    title: { text: '热门影片 TOP5', textStyle: { color: '#d4a853', fontSize: 14 } },
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c}' },
    series: [{
      type: 'pie', radius: ['42%', '68%'],
      data: pieData,
      label: { color: '#ccc' },
      itemStyle: { borderRadius: 6, borderColor: '#14141c', borderWidth: 2 }
    }]
  })
  movieChartInst.resize()
}

function movieName(id) { return movies.value.find(m => m.id === id)?.title || '—' }
function cinemaName(id) { return cinemas.value.find(c => c.id === id)?.name || '—' }
function formatTime(t) {
  if (!t) return '—'
  return new Date(t).toLocaleString('zh-CN')
}

function openMovieDialog(row) {
  Object.assign(movieForm, row || {
    id: null, title: '', genre: '剧情', duration: 120, rating: 8,
    poster: 'https://picsum.photos/seed/new/400/600', description: '', status: 'SHOWING'
  })
  movieDialog.value = true
}

function openScreeningDialog(row) {
  if (row) {
    Object.assign(screeningForm, { ...row, startTime: row.startTime ? new Date(row.startTime) : new Date() })
  } else {
    Object.assign(screeningForm, {
      id: null, movieId: movies.value[0]?.id, cinemaId: cinemas.value[0]?.id,
      hallName: '激光厅', startTime: new Date(Date.now() + 3600000), price: 48,
      seatRows: 8, seatCols: 12, status: 'OPEN'
    })
  }
  screeningDialog.value = true
}

function openSnackDialog(row) {
  Object.assign(snackForm, row || {
    id: null, name: '', category: 'SINGLE', price: 28,
    description: '', image: 'https://picsum.photos/seed/snack/200/200', status: 'ON_SALE'
  })
  snackDialog.value = true
}

function openCinemaDialog(row) {
  Object.assign(cinemaForm, row || {
    id: null, name: '', city: '北京', address: '', phone: '',
    cover: 'https://picsum.photos/seed/cinema/800/400', status: 'OPEN'
  })
  cinemaDialog.value = true
}

async function saveMovie() {
  if (!movieForm.title?.trim()) return ElMessage.warning('请输入片名')
  saving.value = true
  try {
    await adminApi.saveMovie(movieForm)
    movieDialog.value = false
    ElMessage.success('影片保存成功')
    await loadAll()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function saveScreening() {
  if (!screeningForm.movieId) return ElMessage.warning('请选择影片')
  if (!screeningForm.cinemaId) return ElMessage.warning('请选择影城')
  saving.value = true
  try {
    await adminApi.saveScreening(screeningForm)
    screeningDialog.value = false
    ElMessage.success('场次保存成功')
    await loadAll()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function saveSnack() {
  if (!snackForm.name?.trim()) return ElMessage.warning('请输入卖品名称')
  saving.value = true
  try {
    await adminApi.saveSnack(snackForm)
    snackDialog.value = false
    ElMessage.success('卖品保存成功')
    await loadAll()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function saveCinema() {
  if (!cinemaForm.name?.trim()) return ElMessage.warning('请输入影城名称')
  saving.value = true
  try {
    await adminApi.saveCinema(cinemaForm)
    cinemaDialog.value = false
    ElMessage.success('影城保存成功')
    await loadAll()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function delMovie(row) {
  await ElMessageBox.confirm('确定删除影片：' + row.title + '？', '确认删除', { type: 'warning' })
  try {
    await adminApi.deleteMovie(row.id)
    ElMessage.success('已删除')
    await loadAll()
  } catch (e) {
    ElMessage.error(e.message || '删除失败，可能有关联场次')
  }
}

async function delScreening(row) {
  await ElMessageBox.confirm('确定删除场次：' + row.hallName + '？', '确认删除', { type: 'warning' })
  try {
    await adminApi.deleteScreening(row.id)
    ElMessage.success('已删除')
    await loadAll()
  } catch (e) {
    ElMessage.error(e.message || '删除失败，可能有关联订单')
  }
}

async function delSnack(row) {
  await ElMessageBox.confirm('确定删除卖品：' + row.name + '？', '确认删除', { type: 'warning' })
  try {
    await adminApi.deleteSnack(row.id)
    ElMessage.success('已删除')
    await loadAll()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}
</script>

<style scoped>
.admin-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.stat-num { font-size: 2rem; font-weight: 700; color: #fff; }
.stat-num.gold { color: var(--gold); }
.stat-label { color: var(--text-muted); font-size: 13px; margin-top: 4px; }
.chart-row { margin-top: 24px; }
.chart { height: 320px; width: 100%; background: var(--bg-card); border: 1px solid var(--border); border-radius: 12px; }
.admin-tabs { margin-top: 32px; }
.tab-toolbar { margin-bottom: 12px; }

/* 架构技术 tab */
.tech-container { padding: 8px 0; }
.tech-summary { color: var(--gold-light); margin-bottom: 24px; font-size: 15px; }
.tech-summary strong { color: var(--gold); font-size: 1.2rem; }
.tech-section-title { color: var(--gold); margin: 16px 0 10px; font-size: 1rem; font-weight: 500; border-left: 3px solid var(--gold); padding-left: 10px; }
</style>