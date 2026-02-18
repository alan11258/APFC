<script setup lang="ts">
import { ref } from 'vue'

type User = Record<string, unknown>

const userId = ref<string>('')
const loading = ref(false)
const errorMessage = ref<string>('')
const result = ref<User | null>(null)

async function queryUser() {
  errorMessage.value = ''
  result.value = null

  const id = userId.value.trim()
  if (!id) {
    errorMessage.value = '請輸入 userId'
    return
  }
  if (!/^\d+$/.test(id)) {
    errorMessage.value = 'userId 必須是數字'
    return
  }

  loading.value = true
  try {
    const resp = await fetch(`/api/users/${id}`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    })

    if (resp.status === 404) {
      errorMessage.value = '找不到使用者'
      return
    }
    if (!resp.ok) {
      const text = await resp.text()
      errorMessage.value = `API 錯誤：${resp.status} ${text}`
      return
    }

    result.value = await resp.json()
  } catch (e: unknown) {
    errorMessage.value = '無法連線到後端，請確認後端服務已啟動'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="max-width: 720px; margin: 24px auto; padding: 16px;">
    <h2>查詢使用者</h2>

    <div style="display: flex; gap: 8px; align-items: center;">
      <label for="userId">userId</label>
      <input
        id="userId"
        v-model="userId"
        inputmode="numeric"
        placeholder="例如 123"
        style="flex: 1; padding: 8px;"
        @keyup.enter="queryUser"
      />
      <button :disabled="loading" @click="queryUser" style="padding: 8px 12px;">
        {{ loading ? '查詢中...' : '查詢' }}
      </button>
    </div>

    <p v-if="errorMessage" style="color: #b00020; margin-top: 12px;">
      {{ errorMessage }}
    </p>

    <div v-if="result" style="margin-top: 12px;">
      <h3>結果</h3>
      <pre style="background: #f6f8fa; padding: 12px; overflow: auto;">{{ result }}</pre>
    </div>

    <hr style="margin: 20px 0;" />

    <p style="color: #555;">
      提醒：如果你直接呼叫 <code>/api/users</code>（沒帶 userId），後端會回 400 + 提示訊息。
    </p>
  </div>
</template>
