import { useEffect, useState } from 'react'
import { Alert, Button, Card, CardContent, Chip, CircularProgress, Grid, Stack, Typography } from '@mui/material'
import { adminApi } from '../api/adminApi'
import type { ServiceRequest, StaffingRequest } from '../types/api'

export function ServiceRequestsPage() {
  const [services, setServices] = useState<ServiceRequest[]>([])
  const [staffing, setStaffing] = useState<StaffingRequest[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([adminApi.getServiceRequests(), adminApi.getStaffingRequests()])
      .then(([serviceRequests, staffingRequests]) => { setServices(serviceRequests); setStaffing(staffingRequests) })
      .catch((e) => setError(e?.response?.data?.message || 'Unable to load service requests'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Stack alignItems="center" sx={{ py: 8 }}><CircularProgress /></Stack>
  if (error) return <Alert severity="error">{error}</Alert>

  return <Stack spacing={3}>
    <div>
      <Typography variant="h4" fontWeight={800}>Service Requests</Typography>
      <Typography color="text.secondary">Customer requests for catering staff, chairs, tables, decoration and lighting.</Typography>
    </div>

    <Typography variant="h6" fontWeight={800}>Staff Requests</Typography>
    <Grid container spacing={2}>
      {staffing.map((r) => <Grid key={r.id} size={{ xs: 12, md: 6 }}>
        <Card><CardContent><Stack spacing={1}>
          <Stack direction="row" justifyContent="space-between"><Typography fontWeight={800}>{r.workerType.replaceAll('_', ' ')}</Typography><Chip size="small" label={r.status} /></Stack>
          <Typography>{r.eventType} • {r.eventDate} • {r.startTime} - {r.endTime}</Typography>
          <Typography color="text.secondary">{r.location}, {r.area}</Typography>
          <Typography>{r.requiredWorkers} workers × ₹{Number(r.payment).toLocaleString('en-IN')} = ₹{(r.requiredWorkers * Number(r.payment)).toLocaleString('en-IN')}</Typography>
          {r.additionalRequirements && <Typography color="text.secondary">{r.additionalRequirements}</Typography>}
          {r.status === 'PENDING' && <Button variant="contained" onClick={() => adminApi.updateStaffingRequestStatus(r.id, 'OPEN').then(updated => setStaffing(prev => prev.map(x => x.id === updated.id ? updated : x)))}>Approve & Publish to Workers</Button>}
        </Stack></CardContent></Card>
      </Grid>)}
    </Grid>

    <Typography variant="h6" fontWeight={800}>Equipment & Decoration Requests</Typography>
    <Grid container spacing={2}>
      {services.map((r) => <Grid key={r.id} size={{ xs: 12, md: 6 }}>
        <Card><CardContent><Stack spacing={1}>
          <Stack direction="row" justifyContent="space-between"><Typography fontWeight={800}>{r.serviceType}</Typography><Chip size="small" label={r.status} /></Stack>
          <Typography>{r.eventType} • {r.eventDate} • {r.startTime}</Typography>
          <Typography color="text.secondary">{r.location}, {r.area}</Typography>
          <Typography sx={{ whiteSpace: 'pre-wrap' }}>{r.details}</Typography>
          <Typography fontWeight={800}>Fixed total: ₹{Number(r.totalAmount).toLocaleString('en-IN')}</Typography>
        </Stack></CardContent></Card>
      </Grid>)}
    </Grid>
  </Stack>
}
