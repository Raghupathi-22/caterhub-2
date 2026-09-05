import { http } from './http'
import type { AuthResponse, User } from '../types/models'

export type OtpPurpose = 'LOGIN' | 'REGISTER_CUSTOMER' | 'REGISTER_WORKER'

export interface SendOtpRequest {
  mobileNumber: string
  purpose: OtpPurpose
  userType?: string
  channel?: 'AUTO' | 'SMS' | 'VOICE'
}

export interface VerifyOtpRequest {
  mobileNumber: string
  otp: string
  purpose: OtpPurpose
  name?: string
}

export interface AdminLoginRequest {
  username: string
  password: string
}

export const authApi = {
  sendOtp: async (payload: SendOtpRequest): Promise<void> => {
    await http.post('/auth/otp/send', payload)
  },
  verifyOtp: async (payload: VerifyOtpRequest): Promise<AuthResponse> => {
    const response = await http.post<AuthResponse>('/auth/otp/verify', payload)
    return response.data
  },
  loginAdmin: async (payload: AdminLoginRequest): Promise<AuthResponse> => {
    const response = await http.post<AuthResponse>('/auth/admin/login', payload)
    return response.data
  },
  logout: async (refreshToken: string): Promise<void> => {
    await http.post('/auth/logout', { refresh_token: refreshToken })
  },
  getMe: async (): Promise<User> => {
    const response = await http.get<User>('/users/me')
    return response.data
  },
}
