import type { ReactElement } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

interface RequireAdminProps {
  children: ReactElement
}

export function RequireAdmin({ children }: RequireAdminProps) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const hasRole = useAuthStore((state) => state.hasRole)

  if (!isAuthenticated) {
    return <Navigate to="/admin/login" replace />
  }

  if (!hasRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')) {
    return <Navigate to="/admin/login" replace />
  }

  return children
}
