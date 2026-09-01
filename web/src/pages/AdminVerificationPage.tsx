import { Alert, Button, Card, CardContent, Grid, Stack, TextField, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { useAuthStore } from '../store/authStore'
import type { WorkerProfile } from '../types/models'

export function AdminVerificationPage() {
  const adminUserId = useAuthStore((state) => state.user?.id)
  const [workers, setWorkers] = useState<WorkerProfile[]>([])
  const [reasons, setReasons] = useState<Record<number, string>>({})
  const [error, setError] = useState('')

  const load = () => {
    adminApi
      .getWorkerProfiles('PENDING_VERIFICATION')
      .then(setWorkers)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load pending workers.')))
  }

  useEffect(() => {
    load()
  }, [])

  const updateStatus = async (profileId: number, status: 'ACTIVE' | 'REJECTED') => {
    setError('')
    const rejectionReason = reasons[profileId]?.trim()
    if (status === 'REJECTED' && !rejectionReason) {
      setError('Reject requires a reason.')
      return
    }
    try {
      await adminApi.updateWorkerStatus(profileId, { status, adminUserId, rejectionReason })
      load()
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to update worker verification status.'))
    }
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Worker Verification</Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Grid container spacing={2}>
        {workers.map((worker) => (
          <Grid item xs={12} md={6} key={worker.id}>
            <Card><CardContent>
              <Stack spacing={1}>
                <Typography sx={{ fontWeight: 700 }}>{worker.fullName}</Typography>
                <Typography>{worker.workerType.replace(/_/g, ' ')}</Typography>
                <Typography color="text.secondary">Skills: {worker.skills || '-'}</Typography>
                <Typography color="text.secondary">Areas: {worker.preferredAreas || '-'}</Typography>
                <TextField
                  fullWidth
                  label="Reject Reason"
                  value={reasons[worker.id] ?? ''}
                  onChange={(e) => setReasons((current) => ({ ...current, [worker.id]: e.target.value }))}
                />
                <Stack direction="row" spacing={1}>
                  <Button variant="contained" onClick={() => void updateStatus(worker.id, 'ACTIVE')}>Approve</Button>
                  <Button variant="outlined" color="error" onClick={() => void updateStatus(worker.id, 'REJECTED')}>Reject</Button>
                </Stack>
              </Stack>
            </CardContent></Card>
          </Grid>
        ))}
      </Grid>
    </Stack>
  )
}
