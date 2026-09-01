import { Alert, Button, Card, CardContent, Grid, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { StatusChip } from '../components/StatusChip'
import type { ServiceRequestDTO, StaffingJob } from '../types/models'

export function AdminSupportPage() {
  const [serviceRequests, setServiceRequests] = useState<ServiceRequestDTO[]>([])
  const [staffingRequests, setStaffingRequests] = useState<StaffingJob[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([adminApi.getServiceRequests(), adminApi.getStaffingRequests()])
      .then(([services, staffing]) => {
        setServiceRequests(services)
        setStaffingRequests(staffing)
      })
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load support requests.')))
  }, [])

  const approveStaffing = async (id: number) => {
    try {
      const updated = await adminApi.updateStaffingRequestStatus(id, 'OPEN')
      setStaffingRequests((current) => current.map((item) => (item.id === id ? updated : item)))
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to update staffing request status.'))
    }
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Support</Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}

      <Typography variant="h6" sx={{ fontWeight: 700 }}>Staffing Requests</Typography>
      <Grid container spacing={2}>
        {staffingRequests.map((request) => (
          <Grid item xs={12} md={6} key={request.id}>
            <Card><CardContent>
              <Stack spacing={0.8}>
                <Typography sx={{ fontWeight: 700 }}>{request.workerType.replace(/_/g, ' ')}</Typography>
                <Typography>{request.eventType} • {request.eventDate} • {request.startTime}-{request.endTime}</Typography>
                <Typography color="text.secondary">{request.location}, {request.area}</Typography>
                <StatusChip status={request.status} />
                {request.status === 'PENDING' ? <Button variant="contained" onClick={() => void approveStaffing(request.id)}>Approve & Publish</Button> : null}
              </Stack>
            </CardContent></Card>
          </Grid>
        ))}
      </Grid>

      <Typography variant="h6" sx={{ fontWeight: 700 }}>Service Requests</Typography>
      <Grid container spacing={2}>
        {serviceRequests.map((request) => (
          <Grid item xs={12} md={6} key={request.id}>
            <Card><CardContent>
              <Stack spacing={0.8}>
                <Typography sx={{ fontWeight: 700 }}>{request.serviceType.replace(/_/g, ' ')}</Typography>
                <Typography>{request.eventType} • {request.eventDate}</Typography>
                <Typography color="text.secondary">{request.location}, {request.area}</Typography>
                <StatusChip status={request.status} />
              </Stack>
            </CardContent></Card>
          </Grid>
        ))}
      </Grid>
    </Stack>
  )
}
