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
  Grid,
} from '@mui/material'
import { useMutation } from '@tanstack/react-query'
import { authAPI, RegisterRequest } from '../api/authAPI'
import { useAuthStore } from '../store/authStore'

export const RegisterPage = () => {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)
  const [formData, setFormData] = useState<RegisterRequest>({
    username: '',
    email: '',
    phone_number: '',
    password: '',
    first_name: '',
    last_name: '',
  })
  const [error, setError] = useState('')

  const { mutate: register, isPending } = useMutation({
    mutationFn: authAPI.register,
    onSuccess: (response) => {
      const { data } = response
      setAuth(data.user as any, data.access_token, data.refresh_token)
      navigate('/')
    },
    onError: (error: any) => {
      setError(
        error.response?.data?.message || 'Registration failed. Please try again.'
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
    register(formData)
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
          py: 4,
        }}
      >
        <Card sx={{ padding: 4, width: '100%' }}>
          <Typography variant="h4" component="h1" gutterBottom sx={{ mb: 3 }}>
            Create Account
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth
              label="Username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              disabled={isPending}
              required
            />

            <TextField
              fullWidth
              label="Email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              disabled={isPending}
              required
            />

            <TextField
              fullWidth
              label="Phone Number"
              name="phone_number"
              value={formData.phone_number}
              onChange={handleChange}
              disabled={isPending}
              required
            />

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="First Name"
                  name="first_name"
                  value={formData.first_name}
                  onChange={handleChange}
                  disabled={isPending}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Last Name"
                  name="last_name"
                  value={formData.last_name}
                  onChange={handleChange}
                  disabled={isPending}
                />
              </Grid>
            </Grid>

            <TextField
              fullWidth
              label="Password"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              disabled={isPending}
              required
              helperText="Password must be at least 8 characters"
            />

            <Button
              fullWidth
              variant="contained"
              color="primary"
              type="submit"
              disabled={isPending}
              sx={{ mt: 2 }}
            >
              {isPending ? <CircularProgress size={24} /> : 'Register'}
            </Button>
          </Box>

          <Typography sx={{ mt: 3, textAlign: 'center' }}>
            Already have an account?{' '}
            <Link href="/login" underline="hover">
              Login
            </Link>
          </Typography>
        </Card>
      </Box>
    </Container>
  )
}
