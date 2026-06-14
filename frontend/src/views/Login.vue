<template>
  <div class="page login-page">
    <div class="login-card">
      <h2>{{ isRegister ? '注册账号' : '欢迎回来' }}</h2>
      <p class="subtitle">登录后即可选座购票</p>
      <el-form ref="formRef" :rules="rules" :model="form" @submit.prevent="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" native-type="submit">
          {{ isRegister ? '注册' : '登录' }}
        </el-button>
      </el-form>
      <p class="switch">
        <a href="#" @click.prevent="isRegister = !isRegister">
          {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
        </a>
      </p>
      <div class="demo-hint">
        <p>演示账号：demo / demo123</p>
        <p>管理员：admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

const router = useRouter()
const isRegister = ref(false)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const formRef = ref(null)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度 2-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 20, message: '密码长度 4-20 位', trigger: 'blur' }
  ]
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const fn = isRegister.value ? authApi.register : authApi.login
    const res = await fn(form.username, form.password)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('role', res.data.role)
    ElMessage.success(isRegister.value ? '注册成功' : '登录成功')
    router.push(res.data.role === 'ADMIN' ? '/admin' : '/')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
}
.login-card {
  width: 400px;
  padding: 40px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.4);
}
.login-card h2 { text-align: center; color: var(--gold-light); margin-bottom: 8px; }
.subtitle { text-align: center; color: var(--text-muted); margin-bottom: 28px; font-size: 14px; }
.switch { text-align: center; margin-top: 16px; font-size: 14px; }
.demo-hint {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
  line-height: 1.8;
}
</style>
