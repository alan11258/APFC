import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import Login from '../pages/Login.vue'
import Forbidden from '../pages/Forbidden.vue'
import NotFound from '../pages/NotFound.vue'
import { apiMe, apiRoutes, apiRouteAccess } from '../api/auth'

type BackendRoute = {
  path: string
  name?: string
  title?: string
  componentKey: string // 例如 "UserLookupPage"
}

const pageModules = import.meta.glob('../pages/*.vue')

function resolvePageComponent(componentKey: string) {
  const path = `../pages/${componentKey}.vue`
  const loader = pageModules[path]
  if (!loader) return null
  return loader as unknown as () => Promise<unknown>
}

let dynamicRoutesLoaded = false

async function fetchBackendRoutes(): Promise<BackendRoute[] | null> {
  const res = await apiRoutes()
  if (res.status === 401) return null
  if (!res.ok) throw new Error(`Failed to fetch routes: HTTP ${res.status}`)
  return (await res.json()) as BackendRoute[]
}

async function ensureDynamicRoutes(router: ReturnType<typeof createRouter>) {
  if (dynamicRoutesLoaded) return

  const backendRoutes = await fetchBackendRoutes()
  if (!backendRoutes) {
    dynamicRoutesLoaded = true
    return
  }

  const DEFAULT_TITLE = ''

  const addBackendRoute = (backendRoute: BackendRoute) => {
    const pageLoader = resolvePageComponent(backendRoute.componentKey)
    if (!pageLoader) {
      // 後端回了前端不存在的元件 key：略過或記錄（建議略過，避免炸整個 router）
      // eslint-disable-next-line no-console
      console.warn(
          `[router] Unknown componentKey "${backendRoute.componentKey}" for path "${backendRoute.path}"`
      )
      return
    }

    router.addRoute({
      path: backendRoute.path,
      name: backendRoute.name ?? backendRoute.path,
      component: pageLoader,
      meta: { title: backendRoute.title ?? DEFAULT_TITLE },
    })
  }

  for (const backendRoute of backendRoutes) {
    addBackendRoute(backendRoute)
  }

  dynamicRoutesLoaded = true
}

const staticRoutes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: Login, meta: { public: true } },
  { path: '/403', name: 'forbidden', component: Forbidden, meta: { public: true } },
  { path: '/404', name: 'notfound', component: NotFound, meta: { public: true } },
]

export const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes,
})

router.beforeEach(async (to) => {
  if (to.meta.public) return true

  // 1) 未登入 → /login
  const meRes = await apiMe()
  if (meRes.status === 401) {
    return { path: '/login', query: { redirect: to.fullPath }, replace: true }
  }

  // 2) 已登入：載入動態 routes
  await ensureDynamicRoutes(router)

  // 3) 若仍無匹配：用後端 route-access 區分 403/404
  if (!to.matched.length) {
    const accessRes = await apiRouteAccess(to.path)
    if (accessRes.status === 401) {
      return { path: '/login', query: { redirect: to.fullPath }, replace: true }
    }
    if (accessRes.status === 403) {
      return { path: '/403', replace: true }
    }
    if (accessRes.status === 404) {
      return { path: '/404', replace: true }
    }
    return { path: '/404', replace: true }
  }

  return true
})

