import { Alert, Button, Card, CardContent, Grid, Stack, TextField, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { apiErrorMessage } from '../api/http'
import { workerApi } from '../api/workerApi'
import { StatusChip } from '../components/StatusChip'
import type { StaffingJob } from '../types/models'

export function WorkerJobsPage() {
  const [area, setArea] = useState('')
  const [search, setSearch] = useState('')
  const [jobs, setJobs] = useState<StaffingJob[]>([])
  const [error, setError] = useState('')

  const load = () => {
    setError('')
    workerApi
      .getAvailableJobs({ area: area.trim() || undefined, search: search.trim() || undefined })
      .then(setJobs)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load jobs.')))
  }

  useEffect(() => {
    workerApi
      .getAvailableJobs({})
      .then(setJobs)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load jobs.')))
  }, [])

  const accept = async (jobId: number) => {
    try {
      await workerApi.acceptJob(jobId)
      load()
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to accept job.'))
    }
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Available Bookings</Typography>
      <Grid container spacing={1}>
        <Grid item xs={12} md={4}>
          <TextField fullWidth label="Skill search" value={search} onChange={(e) => setSearch(e.target.value)} />
        </Grid>
        <Grid item xs={12} md={4}>
          <TextField fullWidth label="Area" value={area} onChange={(e) => setArea(e.target.value)} />
        </Grid>
        <Grid item xs={12} md={4}>
          <Button variant="contained" sx={{ height: '100%' }} onClick={load}>Search Bookings</Button>
        </Grid>
      </Grid>
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Grid container spacing={2}>
        {jobs.map((job) => (
          <Grid item xs={12} md={6} key={job.id}>
            <Card>
              <CardContent>
                <Stack spacing={0.7}>
                  <Typography sx={{ fontWeight: 700 }}>{job.workerType.replace(/_/g, ' ')}</Typography>
                  <Typography>{job.eventType} • {job.eventDate}</Typography>
                  <Typography color="text.secondary">{job.location}, {job.area}</Typography>
                  <Typography>₹{Number(job.payment).toLocaleString('en-IN')}</Typography>
                  <StatusChip status={job.status} />
                  <Button variant="contained" onClick={() => void accept(job.id)} disabled={job.alreadyAccepted || job.remainingPositions <= 0}>
                    {job.alreadyAccepted ? 'Already Accepted' : 'Accept Job'}
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Stack>
  )
}
