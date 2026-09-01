import { Alert, Button, Card, CardContent, Stack, Typography } from '@mui/material'
import { useState } from 'react'
import { apiErrorMessage } from '../api/http'
import { workerApi } from '../api/workerApi'

export function WorkerAvailabilityPage() {
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const setAvailability = async (available: boolean) => {
    setError('')
    setMessage('')
    try {
      await workerApi.updateAvailability(available)
      setMessage(available ? 'Availability enabled.' : 'Availability disabled.')
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to update availability.'))
    }
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Worker Availability</Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      {message ? <Alert severity="success">{message}</Alert> : null}
      <Card>
        <CardContent>
          <Stack direction="row" spacing={1}>
            <Button variant="contained" onClick={() => void setAvailability(true)}>Set Available</Button>
            <Button variant="outlined" onClick={() => void setAvailability(false)}>Set Unavailable</Button>
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  )
}
