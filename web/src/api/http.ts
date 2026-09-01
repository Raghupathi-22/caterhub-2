import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '../store/authStore'
import type { AuthResponse } from '../types/models'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

type RetriableRequest = InternalAxiosRequestConfig & { _retry?: boolean }

export const http = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

const refreshClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

let refreshPromise: Promise<string> | null = null

function loginRouteForCurrentPath(): string {
  return window.location.pathname.startsWith('/admin') ? '/admin/login' : '/login'
}

function clearSessionAndRedirect(): void {
  useAuthStore.getState().logout()
  window.location.href = loginRouteForCurrentPath()
}

async function refreshAccessToken(): Promise<string> {
  const refreshToken = useAuthStore.getState().refreshToken
  if (!refreshToken) {
    throw new Error('Missing refresh token')
  }

  const response = await refreshClient.post<AuthResponse>('/auth/refresh', {
    refresh_token: refreshToken,
  })
  useAuthStore.getState().setAuth(response.data.user, response.data.access_token, response.data.refresh_token)
  return response.data.access_token
}

http.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const request = error.config as RetriableRequest | undefined
    const status = error.response?.status

    if (!request || request._retry || status !== 401 || request.url?.includes('/auth/refresh')) {
      return Promise.reject(error)
    }

    request._retry = true

    try {
      if (!refreshPromise) {
        refreshPromise = refreshAccessToken().finally(() => {
          refreshPromise = null
        })
      }
      const token = await refreshPromise
      request.headers.Authorization = `Bearer ${token}`
      return http(request)
    } catch (refreshError) {
      clearSessionAndRedirect()
      return Promise.reject(refreshError)
    }
  },
)

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    if (status === 403) return "You don't have permission to perform this action."
    if (status === 404) return 'Requested resource was not found.'
    if (status === 409) return 'This action conflicts with current data.'
    if (status === 422) return 'Please check form values and try again.'
    if (status && status >= 500) return 'Server error. Please try again shortly.'
    if (status === 401) return 'Session expired. Please login again.'
    if (!status) return 'Unable to connect to server. Check your network.'
    const data = error.response?.data as { message?: string } | undefined
    if (data?.message) return data.message
  }
  return fallback
}
