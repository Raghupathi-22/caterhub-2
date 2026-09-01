import { Alert, Card, CardContent, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { apiErrorMessage } from '../api/http'
import { workerApi } from '../api/workerApi'
import { StatusChip } from '../components/StatusChip'
import type { WorkerProfile } from '../types/models'

export function WorkerProfilePage() {
  const [profile, setProfile] = useState<WorkerProfile | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    workerApi
      .getMyProfile()
      .then(setProfile)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load worker profile.')))
  }, [])

  if (error) return <Alert severity="error">{error}</Alert>
  if (!profile) return <Typography>Loading profile...</Typography>

  return (
    <Card>
      <CardContent>
        <Stack spacing={1}>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>{profile.fullName}</Typography>
          <Typography>{profile.workerType.replace(/_/g, ' ')}</Typography>
          <StatusChip status={profile.status} />
          <Typography color="text.secondary">Skills: {profile.skills || '-'}</Typography>
          <Typography color="text.secondary">Areas: {profile.preferredAreas || '-'}</Typography>
          {profile.status === 'REJECTED' && profile.rejectionReason ? <Alert severity="warning">{profile.rejectionReason}</Alert> : null}
        </Stack>
      </CardContent>
    </Card>
  )
}
