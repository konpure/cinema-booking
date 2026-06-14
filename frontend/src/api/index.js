import axios from 'axios'

const api = axios.create({ baseURL: '' })

function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})

api.interceptors.response.use(
  res => {
    const body = res.data
    if (body && body.success === false) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body?.data !== undefined ? { ...res, data: body.data } : res
  },
  err => {
    const status = err.response?.status
    const msg = err.response?.data?.message
    if (status === 401) {
      clearAuth()
      const currentPath = window.location.pathname
      if (!currentPath.startsWith('/login')) {
        window.history.pushState({}, '', '/login')
        window.dispatchEvent(new PopStateEvent('popstate'))
      }
      return Promise.reject(new Error(msg || '登录已过期，请重新登录'))
    }
    if (status === 403) {
      return Promise.reject(new Error(msg || '无权访问'))
    }
    return Promise.reject(new Error(msg || err.message || '网络错误'))
  }
)

export const authApi = {
  login: (username, password) => api.post('/api/auth/login', { username, password }),
  register: (username, password) => api.post('/api/auth/register', { username, password }),
  me: () => api.get('/api/auth/me')
}

export const cinemaApi = {
  list: () => api.get('/api/cinemas'),
  detail: id => api.get('/api/cinemas/' + id)
}

export const snackApi = {
  list: () => api.get('/api/snacks')
}

export const movieApi = {
  list: (cinemaId) => api.get('/api/movies', { params: cinemaId ? { cinemaId } : {} }),
  detail: id => api.get('/api/movies/' + id)
}

export const screeningApi = {
  list: (movieId, cinemaId) => api.get('/api/screenings', { params: { movieId, cinemaId } }),
  detail: id => api.get('/api/screenings/' + id)
}

export const bookingApi = {
  seats: screeningId => api.get('/api/bookings/' + screeningId + '/seats'),
  lock: data => api.post('/api/bookings/lock', data),
  submit: data => api.post('/api/bookings/submit', data),
  release: data => api.post('/api/bookings/release', data)
}

export const orderApi = {
  list: () => api.get('/api/orders')
}

export const adminApi = {
  stats: () => api.get('/api/admin/stats'),
  movies: () => api.get('/api/admin/movies'),
  saveMovie: data => api.post('/api/admin/movies', data),
  deleteMovie: id => api.delete('/api/admin/movies/' + id),
  screenings: () => api.get('/api/admin/screenings'),
  saveScreening: data => api.post('/api/admin/screenings', data),
  deleteScreening: id => api.delete('/api/admin/screenings/' + id),
  orders: () => api.get('/api/admin/orders'),
  snacks: () => api.get('/api/admin/snacks'),
  saveSnack: data => api.post('/api/admin/snacks', data),
  deleteSnack: id => api.delete('/api/admin/snacks/' + id),
  cinemas: () => api.get('/api/admin/cinemas'),
  saveCinema: data => api.post('/api/admin/cinemas', data)
}

export default api
