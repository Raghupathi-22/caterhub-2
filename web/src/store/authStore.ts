import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { User } from '../types/models'

interface AuthState {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  setAuth: (user: User, accessToken: string, refreshToken: string) => void
  setAccessToken: (token: string) => void
  logout: () => void
  hasRole: (...roles: string[]) => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      setAuth: (user, accessToken, refreshToken) =>
        set({ user, accessToken, refreshToken, isAuthenticated: true }),
      setAccessToken: (token) =>
        set((state) => ({ ...state, accessToken: token, isAuthenticated: true })),
      logout: () => set({ user: null, accessToken: null, refreshToken: null, isAuthenticated: false }),
      hasRole: (...roles: string[]) => {
        const userRoles = get().user?.roles ?? []
        return roles.some((role) => userRoles.includes(role))
      },
    }),
    { name: 'caterhub-auth' },
  ),
)
