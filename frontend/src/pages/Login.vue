<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiLogin } from '../api/auth'

const router = useRouter()
const route = useRoute()

const account = ref('')
const password = ref('')
const error = ref<string | null>(null)
const loading = ref(false)

async function onSubmit() {
  error.value = null
  loading.value = true
  try {
    const res = await apiLogin(account.value, password.value)
    if (!res.ok) {
      error.value = '登入失敗：帳號或密碼錯誤，或帳號已鎖定/刪除'
      return
    }

    const redirect = (route.query.redirect as string) || '/'
    await router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="max-width: 420px;">
    <h2>Login</h2>

    <form @submit.prevent="onSubmit">
      <div style="margin: 12px 0;">
        <label>Account</label>
        <input v-model="account" type="text" autocomplete="username" style="width: 100%;" />
      </div>

      <div style="margin: 12px 0;">
        <label>Password</label>
        <input v-model="password" type="password" autocomplete="current-password" style="width: 100%;" />
      </div>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Signing in…' : 'Sign in' }}
      </button>

      <p v-if="error" style="color: #b91c1c; margin-top: 12px;">
        {{ error }}
      </p>
    </form>
  </div>
</template>