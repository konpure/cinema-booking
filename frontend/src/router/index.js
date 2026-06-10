import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import MovieDetail from '../views/MovieDetail.vue'
import Booking from '../views/Booking.vue'
import Orders from '../views/Orders.vue'
import Admin from '../views/Admin.vue'

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
