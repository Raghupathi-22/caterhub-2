import { http } from './http'
import type { AuthResponse } from '../types/api'

interface LoginRequest {
  email_or_username: string
  password: string
}

export const authApi = {
  login: async (payload: LoginRequest): Promise<AuthResponse> => {
    const response = await http.post<AuthResponse>('/auth/login', payload)
    return response.data
  },
}
