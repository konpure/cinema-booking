import { createRouter, createWebHistory } from 'vue-router'

const Home = () => import('../views/Home.vue')
const Login = () => import('../views/Login.vue')
const MovieDetail = () => import('../views/MovieDetail.vue')
const Booking = () => import('../views/Booking.vue')
const Orders = () => import('../views/Orders.vue')
const Admin = () => import('../views/Admin.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: Home },
    { path: '/login', component: Login },
    { path: '/movie/:id', component: MovieDetail },
    { path: '/booking/:screeningId', component: Booking, meta: { auth: true } },
    { path: '/orders', component: Orders, meta: { auth: true } },
    { path: '/admin', component: Admin, meta: { auth: true, admin: true } }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (to.meta.auth && !token) return next('/login')
  if (to.meta.admin && role !== 'ADMIN') return next('/')
  next()
})

export default router