import type { ReactElement } from 'react'
import { Alert, Box, Button } from '@mui/material'
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
    return (
      <Box sx={{ p: 3 }}>
        <Alert
          severity="error"
          action={
            <Button color="inherit" size="small" href="/">
              Go Home
            </Button>
          }
        >
          You do not have permission to access admin pages.
        </Alert>
      </Box>
    )
  }

  return children
}
