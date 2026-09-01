import { Alert, Card, CardContent, Grid, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { apiErrorMessage } from '../api/http'
import { workerApi } from '../api/workerApi'
import { StatusChip } from '../components/StatusChip'
import type { WorkerDashboard } from '../types/models'

export function WorkerDashboardPage() {
  const [data, setData] = useState<WorkerDashboard | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    workerApi
      .getMyDashboard()
      .then(setData)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load worker dashboard.')))
  }, [])

  if (error) return <Alert severity="error">{error}</Alert>
  if (!data) return <Typography>Loading dashboard...</Typography>

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Worker Dashboard</Typography>
      <Card><CardContent>
        <Typography sx={{ fontWeight: 600 }}>{data.profile?.fullName ?? 'Worker Profile'}</Typography>
        <Typography color="text.secondary">{data.profile?.workerType?.replace(/_/g, ' ')}</Typography>
        <StatusChip status={data.profile?.status ?? 'PENDING_VERIFICATION'} />
      </CardContent></Card>
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}><Card><CardContent><Typography sx={{ fontWeight: 700 }}>Nearby Opportunities</Typography><Typography>{data.nearbyOpportunities.length}</Typography></CardContent></Card></Grid>
        <Grid item xs={12} md={6}><Card><CardContent><Typography sx={{ fontWeight: 700 }}>My Jobs</Typography><Typography>{data.myJobs.length}</Typography></CardContent></Card></Grid>
      </Grid>
    </Stack>
  )
}
