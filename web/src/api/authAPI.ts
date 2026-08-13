import axios from '../api/axiosConfig'

export interface RegisterRequest {
  username: string
  email: string
  phone_number: string
  password: string
  first_name?: string
  last_name?: string
}

export interface LoginRequest {
  email_or_username: string
  password: string
}

export interface AuthResponse {
  access_token: string
  refresh_token: string
  token_type: string
  expires_in: number
  user: {
    id: number
    username: string
    email: string
    phone_number: string
    first_name: string
    last_name: string
    is_active: boolean
    is_verified: boolean
  }
}

export const authAPI = {
  register: (data: RegisterRequest) =>
    axios.post<AuthResponse>('/auth/register', data),

  login: (data: LoginRequest) =>
    axios.post<AuthResponse>('/auth/login', data),

  refreshToken: (refreshToken: string) =>
    axios.post<AuthResponse>('/auth/refresh', {
      refresh_token: refreshToken,
    }),

  logout: (refreshToken: string) =>
    axios.post<void>('/auth/logout', {
      refresh_token: refreshToken,
    }),
}

