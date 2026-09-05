import { Visibility, VisibilityOff } from '@mui/icons-material'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  IconButton,
  InputAdornment,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { apiErrorMessage } from '../api/http'
import { CaterhubLogo } from '../components/CaterhubLogo'
import { useAuthStore } from '../store/authStore'

export function AdminLoginPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const hasRole = useAuthStore((state) => state.hasRole)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (isAuthenticated && hasRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')) {
      navigate('/admin/dashboard', { replace: true })
    }
  }, [hasRole, isAuthenticated, navigate])

  const canSubmit = username.trim().length > 0 && password.length > 0

  const submit = async () => {
    if (!canSubmit) return
    setError('')
    setLoading(true)
    try {
      const auth = await authApi.loginAdmin({ username: username.trim(), password })
      const roles = auth.user.roles ?? []
      if (!roles.includes('ROLE_ADMIN') && !roles.includes('ROLE_SUPER_ADMIN')) {
        setError('This account is not authorized for admin access.')
        return
      }
      setAuth(auth.user, auth.access_token, auth.refresh_token)
      navigate('/admin/dashboard', { replace: true })
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Admin login failed.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', p: 2.5, bgcolor: '#F7F4ED' }}>
      <Card sx={{ width: '100%', maxWidth: 460, border: '1px solid', borderColor: 'divider' }}>
        <CardContent sx={{ p: 3 }}>
          <Stack spacing={2}>
            <CaterhubLogo />
            <Typography variant="overline" sx={{ color: 'secondary.main', fontWeight: 700, letterSpacing: '0.08em' }}>
              Admin
            </Typography>
            <Typography component="h1" variant="h4" sx={{ fontWeight: 800, mt: -1 }}>
              Admin Login
            </Typography>

            {error ? <Alert severity="error">{error}</Alert> : null}

            <TextField
              label="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              fullWidth
            />

            <TextField
              label="Password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              fullWidth
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      edge="end"
                      onClick={() => setShowPassword((value) => !value)}
                      aria-label={showPassword ? 'Hide password' : 'Show password'}
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Button variant="contained" onClick={() => void submit()} disabled={loading || !canSubmit}>
              {loading ? <CircularProgress size={20} color="inherit" /> : 'Login'}
            </Button>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  )
}

