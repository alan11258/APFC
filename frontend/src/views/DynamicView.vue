<script setup lang="ts">
import { computed, defineAsyncComponent, ref, watchEffect } from 'vue'
import { useRoute } from 'vue-router'

type ViewInitResponse = {
  component: string
  props?: Record<string, unknown>
}

const route = useRoute()

// 只允許載入這些檔案（白名單）：避免後端回傳任意字串造成任意檔案載入
const pages = import.meta.glob('../pages/*.vue')
const rootViews = import.meta.glob('../App_*.vue')

const init = ref<ViewInitResponse | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

async function fetchInit(path: string) {
  loading.value = true
  error.value = null
  init.value = null

  const url = `/api/view-init?path=${encodeURIComponent(path)}`
  const res = await fetch(url, { headers: { Accept: 'application/json' } })
  if (!res.ok) throw new Error(`view-init failed: HTTP ${res.status}`)

  init.value = (await res.json()) as ViewInitResponse
  loading.value = false
}

watchEffect(async () => {
  try {
    await fetchInit(route.path)
  } catch (e) {
    loading.value = false
    error.value = e instanceof Error ? e.message : String(e)
  }
})

const componentKey = computed(() => {
  // 後端回傳 component 例如：
  // - "UserLookupPage" -> 對應 ../pages/UserLookupPage.vue
  // - "App_HelloWorld" -> 對應 ../App_HelloWorld.vue
  return init.value?.component ?? null
})

const ResolvedComponent = computed(() => {
  const key = componentKey.value
  if (!key) return null

  const pagePath = `../pages/${key}.vue`
  if (pagePath in pages) {
    return defineAsyncComponent(pages[pagePath] as any)
  }

  const rootViewPath = `../${key}.vue`
  if (rootViewPath in rootViews) {
    return defineAsyncComponent(rootViews[rootViewPath] as any)
  }

  return null
})

const resolvedProps = computed(() => init.value?.props ?? {})
</script>

<template>
  <div>
    <div v-if="loading">Loading view…</div>
    <div v-else-if="error">Error: {{ error }}</div>
    <div v-else-if="!ResolvedComponent">
      No matched component for: {{ componentKey }}
    </div>
    <component v-else :is="ResolvedComponent" v-bind="resolvedProps" />
  </div>
</template>
