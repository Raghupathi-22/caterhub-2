import { Alert, Card, CardContent, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { StatusChip } from '../components/StatusChip'
import type { WorkerProfile } from '../types/models'

export function AdminWorkerDetailPage() {
  const { id } = useParams()
  const [worker, setWorker] = useState<WorkerProfile | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) return
    adminApi
      .getWorkerProfile(Number(id))
      .then(setWorker)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load worker profile.')))
  }, [id])

  if (error) return <Alert severity="error">{error}</Alert>
  if (!worker) return <Typography>Loading worker detail...</Typography>

  return (
    <Card>
      <CardContent>
        <Stack spacing={1}>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>{worker.fullName}</Typography>
          <Typography>{worker.workerType.replace(/_/g, ' ')}</Typography>
          <StatusChip status={worker.status} />
          <Typography color="text.secondary">Experience: {worker.experienceYears} years</Typography>
          <Typography color="text.secondary">Skills: {worker.skills || '-'}</Typography>
          <Typography color="text.secondary">Areas: {worker.preferredAreas || '-'}</Typography>
          <Typography color="text.secondary">Languages: {worker.languages || '-'}</Typography>
          <Typography color="text.secondary">Bio: {worker.bio || '-'}</Typography>
          {worker.rejectionReason ? <Alert severity="warning">{worker.rejectionReason}</Alert> : null}
        </Stack>
      </CardContent>
    </Card>
  )
}
