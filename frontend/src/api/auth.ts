export async function apiMe(): Promise<Response> {
    return fetch('/api/auth/me', {
        method: 'GET',
        headers: { Accept: 'application/json' },
        credentials: 'include',
    })
}

export async function apiRoutes(): Promise<Response> {
    return fetch('/api/routes', {
        method: 'GET',
        headers: { Accept: 'application/json' },
        credentials: 'include',
    })
}

export async function apiRouteAccess(path: string): Promise<Response> {
    return fetch(`/api/route-access?path=${encodeURIComponent(path)}`, {
        method: 'GET',
        headers: { Accept: 'application/json' },
        credentials: 'include',
    })
}

export async function apiLogin(account: string, password: string): Promise<Response> {
    const body = new URLSearchParams()
    body.set('username', account)
    body.set('password', password)

    return fetch('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        credentials: 'include',
        body,
    })
}

export async function apiLogout(): Promise<Response> {
    return fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
    })
}