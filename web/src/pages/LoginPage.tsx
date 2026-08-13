import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Button,
  Card,
  Container,
  TextField,
  Typography,
  Alert,
  CircularProgress,
  Link,
} from '@mui/material'
import { useMutation } from '@tanstack/react-query'
import { authAPI, LoginRequest } from '../api/authAPI'
import { useAuthStore } from '../store/authStore'

export const LoginPage = () => {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)
  const [formData, setFormData] = useState<LoginRequest>({
    email_or_username: '',
    password: '',
  })
  const [error, setError] = useState('')

  const { mutate: login, isPending } = useMutation({
    mutationFn: authAPI.login,
    onSuccess: (response) => {
      const { data } = response
      setAuth(data.user as any, data.access_token, data.refresh_token)
      navigate('/')
    },
    onError: (error: any) => {
      setError(
        error.response?.data?.message || 'Login failed. Please try again.'
      )
    },
  })

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    login(formData)
  }

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
        }}
      >
        <Card sx={{ padding: 4, width: '100%', maxWidth: 400 }}>
          <Typography variant="h4" component="h1" gutterBottom sx={{ mb: 3 }}>
            Login
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth
              label="Email or Username"
              name="email_or_username"
              type="text"
              value={formData.email_or_username}
              onChange={handleChange}
              disabled={isPending}
              required
            />

            <TextField
              fullWidth
              label="Password"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              disabled={isPending}
              required
            />

            <Button
              fullWidth
              variant="contained"
              color="primary"
              type="submit"
              disabled={isPending}
              sx={{ mt: 2 }}
            >
              {isPending ? <CircularProgress size={24} /> : 'Login'}
            </Button>
          </Box>

          <Typography sx={{ mt: 3, textAlign: 'center' }}>
            Don't have an account?{' '}
            <Link href="/register" underline="hover">
              Register
            </Link>
          </Typography>
        </Card>
      </Box>
    </Container>
  )
}
