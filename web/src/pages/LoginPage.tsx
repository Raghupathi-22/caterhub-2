import { Alert, Box, Button, Card, CardContent, CircularProgress, Stack, TextField, Typography } from '@mui/material'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { apiErrorMessage } from '../api/http'
import { useAuthStore } from '../store/authStore'

export function LoginPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)
  const [mobileNumber, setMobileNumber] = useState('')
  const [otp, setOtp] = useState('')
  const [otpSent, setOtpSent] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

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
      setAuth(auth.user, auth.access_token, auth.refresh_token)
      navigate('/home', { replace: true })
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Invalid OTP or login failed.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box sx={{ minHeight: '70vh', display: 'grid', placeItems: 'center' }}>
      <Card sx={{ width: '100%', maxWidth: 440 }}>
        <CardContent sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>Customer Login</Typography>
            {error ? <Alert severity="error">{error}</Alert> : null}
            <TextField
              label="Mobile Number"
              value={mobileNumber}
              onChange={(e) => setMobileNumber(e.target.value)}
              placeholder="+91XXXXXXXXXX"
              fullWidth
            />
            {otpSent ? (
              <TextField label="OTP" value={otp} onChange={(e) => setOtp(e.target.value)} fullWidth />
            ) : null}
            {!otpSent ? (
              <Button variant="contained" onClick={() => void sendOtp()} disabled={loading || !mobileNumber.trim()}>
                {loading ? <CircularProgress size={20} color="inherit" /> : 'Send OTP'}
              </Button>
            ) : (
              <Button variant="contained" onClick={() => void verifyOtp()} disabled={loading || otp.length < 4}>
                {loading ? <CircularProgress size={20} color="inherit" /> : 'Verify OTP'}
              </Button>
            )}
            <Button onClick={() => navigate('/register')} size="small">
              New user? Register
            </Button>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  )
}
