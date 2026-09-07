import { Alert, Box, Button, Card, CardContent, CircularProgress, Stack, TextField, Typography } from '@mui/material'
import { useState } from 'react'
import { authApi } from '../api/authApi'
import { apiErrorMessage } from '../api/http'
import { CaterhubLogo } from '../components/CaterhubLogo'

function normalizePhone(value: string): string {
  const digits = value.replace(/[^\d]/g, '')
  if (digits.startsWith('91') && digits.length === 12) return `+${digits}`
  if (digits.length === 10) return `+91${digits}`
  if (digits.startsWith('0') && digits.length === 11) return `+91${digits.slice(1)}`
  return value.trim()
}

export function DeleteAccountPage() {
  const [mobileNumber, setMobileNumber] = useState('')
  const [otp, setOtp] = useState('')
  const [otpSent, setOtpSent] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const mobileCandidate = normalizePhone(mobileNumber)
  const canRequestOtp = mobileCandidate.length >= 10
  const canDelete = otp.trim().length >= 4 && canRequestOtp

  const sendOtp = async () => {
    setError('')
    setSuccess('')
    setLoading(true)
    try {
      await authApi.sendOtp({ mobileNumber: mobileCandidate, purpose: 'LOGIN', channel: 'AUTO' })
      setOtpSent(true)
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to send OTP.'))
    } finally {
      setLoading(false)
    }
  }

  const deleteAccount = async () => {
    setError('')
    setSuccess('')
    setLoading(true)
    try {
      const response = await authApi.deleteAccountWithOtp({ mobileNumber: mobileCandidate, otp: otp.trim() })
      setSuccess(response.message || 'Your CaterHub account has been deleted successfully.')
      setOtp('')
      setMobileNumber('')
      setOtpSent(false)
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to delete account.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Stack spacing={2.5} sx={{ maxWidth: 760, mx: 'auto' }}>
      <CaterhubLogo />
      <Box>
        <Typography component="h1" variant="h3" sx={{ fontWeight: 800, mb: 1 }}>
          Delete Your CaterHub Account
        </Typography>
        <Typography color="text.secondary">
          If you want to delete your CaterHub account and associated personal data, you can submit a deletion request below.
        </Typography>
      </Box>

      <Card>
        <CardContent sx={{ p: { xs: 2.25, md: 3 } }}>
          <Stack spacing={2}>
            {error ? <Alert severity="error">{error}</Alert> : null}
            {success ? <Alert severity="success">{success}</Alert> : null}
            <TextField
              label="Mobile Number"
              placeholder="+91 98765 43210"
              value={mobileNumber}
              onChange={(event) => setMobileNumber(event.target.value)}
              fullWidth
            />
            {otpSent ? (
              <TextField
                label="OTP"
                placeholder="Enter OTP"
                value={otp}
                onChange={(event) => setOtp(event.target.value)}
                fullWidth
              />
            ) : null}

            {!otpSent ? (
              <Button variant="contained" onClick={() => void sendOtp()} disabled={loading || !canRequestOtp}>
                {loading ? <CircularProgress size={20} color="inherit" /> : 'Send OTP'}
              </Button>
            ) : (
              <Button color="error" variant="contained" onClick={() => void deleteAccount()} disabled={loading || !canDelete}>
                {loading ? <CircularProgress size={20} color="inherit" /> : 'Request Account Deletion'}
              </Button>
            )}
          </Stack>
        </CardContent>
      </Card>

      <Card>
        <CardContent sx={{ p: { xs: 2.25, md: 3 } }}>
          <Stack spacing={1.25}>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>
              What happens when you delete your account?
            </Typography>
            <Typography color="text.secondary">
              Your CaterHub customer account is deactivated and personal identifiers such as name, email, phone, and profile details are anonymized.
              Active login/refresh tokens are revoked, so you cannot sign in again with the deleted account.
            </Typography>
            <Typography color="text.secondary">
              Booking and transaction records may be retained for legal, fraud-prevention, accounting, and dispute-resolution obligations.
              Retained records no longer keep unnecessary personal identifiers in plain form.
            </Typography>
            <Typography color="text.secondary">
              Worker/partner and admin accounts are not deleted through this customer flow and require their own support-assisted process.
            </Typography>
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  )
}

