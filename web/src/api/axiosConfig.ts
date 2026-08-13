import axios, { AxiosInstance } from 'axios'
import { useAuthStore } from '../store/authStore'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

let axiosInstance: AxiosInstance

export const initAxios = () => {
  axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  // Request interceptor
  axiosInstance.interceptors.request.use(
    (config) => {
      const token = useAuthStore.getState().accessToken
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  // Response interceptor
  axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config

      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true
        const refreshToken = useAuthStore.getState().refreshToken

        if (refreshToken) {
          try {
            const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
              refresh_token: refreshToken,
            })

            const { access_token, refresh_token } = response.data
            useAuthStore.setState({
              accessToken: access_token,
              refreshToken: refresh_token,
            })

            originalRequest.headers.Authorization = `Bearer ${access_token}`
            return axiosInstance(originalRequest)
          } catch (refreshError) {
            useAuthStore.getState().logout()
            window.location.href = '/login'
          }
        }
      }

      return Promise.reject(error)
    }
  )

  return axiosInstance
}

export const getAxiosInstance = (): AxiosInstance => {
  if (!axiosInstance) {
    initAxios()
  }
  return axiosInstance
}

export default getAxiosInstance()
