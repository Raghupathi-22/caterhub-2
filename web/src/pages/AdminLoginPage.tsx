import { Alert, Box, Button, Card, CardContent, CircularProgress, Stack, TextField, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { apiErrorMessage } from '../api/http'
import { useAuthStore } from '../store/authStore'

export function AdminLoginPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)
  const logout = useAuthStore((state) => state.logout)
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const hasRole = useAuthStore((state) => state.hasRole)
  const [mobileNumber, setMobileNumber] = useState('')
  const [otp, setOtp] = useState('')
  const [otpSent, setOtpSent] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (isAuthenticated && hasRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')) {
      navigate('/admin/dashboard', { replace: true })
    }
  }, [hasRole, isAuthenticated, navigate])

  const sendOtp = async () => {
    setError('')
    setLoading(true)
    try {
      await authApi.sendOtp({ mobileNumber, purpose: 'LOGIN', channel: 'AUTO' })
      setOtpSent(true)
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to send OTP.'))
    } finally {
      setLoading(false)
    }
  }

  const verifyOtp = async () => {
    setError('')
    setLoading(true)
    try {
      const auth = await authApi.verifyOtp({ mobileNumber, otp, purpose: 'LOGIN' })
      const roles = auth.user.roles ?? []
      if (!roles.includes('ROLE_ADMIN') && !roles.includes('ROLE_SUPER_ADMIN')) {
        logout()
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
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', bgcolor: '#f4f6fb' }}>
      <Card sx={{ width: '100%', maxWidth: 460 }}>
        <CardContent sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>Admin Login</Typography>
            {error ? <Alert severity="error">{error}</Alert> : null}
            <TextField label="Admin Mobile Number" value={mobileNumber} onChange={(e) => setMobileNumber(e.target.value)} fullWidth />
            {otpSent ? <TextField label="OTP" value={otp} onChange={(e) => setOtp(e.target.value)} fullWidth /> : null}
            {!otpSent ? (
              <Button variant="contained" onClick={() => void sendOtp()} disabled={loading || !mobileNumber.trim()}>
                {loading ? <CircularProgress size={20} color="inherit" /> : 'Send OTP'}
              </Button>
            ) : (
              <Button variant="contained" onClick={() => void verifyOtp()} disabled={loading || otp.length < 4}>
                {loading ? <CircularProgress size={20} color="inherit" /> : 'Verify OTP'}
              </Button>
            )}
          </Stack>
        </CardContent>
      </Card>
    </Box>
  )
}
