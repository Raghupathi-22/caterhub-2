import { Alert, Button, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Grid, Paper, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { StatusChip } from '../components/StatusChip'
import type { AdminServiceRequestDetails, ServiceRequestDTO, StaffingJob } from '../types/models'

export function AdminSupportPage() {
  const [serviceRequests, setServiceRequests] = useState<ServiceRequestDTO[]>([])
  const [staffingRequests, setStaffingRequests] = useState<StaffingJob[]>([])
  const [details, setDetails] = useState<AdminServiceRequestDetails | null>(null)
  const [error, setError] = useState('')

  const load = () => {
    setError('')
    Promise.all([adminApi.getServiceRequests(), adminApi.getStaffingRequests()])
      .then(([services, staffing]) => {
        setServiceRequests(services)
        setStaffingRequests(staffing)
      })
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load support requests.')))
  }

  useEffect(() => { load() }, [])

  const approveStaffing = async (id: number) => {
    try {
      const updated = await adminApi.updateStaffingRequestStatus(id, 'OPEN')
      setStaffingRequests((current) => current.map((item) => (item.id === id ? updated : item)))
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to update staffing request status.'))
    }
  }

  const openDetails = async (id: number) => {
    try {
      setError('')
      setDetails(await adminApi.getServiceRequestDetails(id))
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to load request details.'))
    }
  }

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h4" sx={{ fontWeight: 700 }}>Support & Service Requests</Typography>
        <Button variant="outlined" onClick={load}>Refresh</Button>
      </Stack>
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
                <Typography>Required: {request.requiredWorkers} • Accepted: {request.acceptedWorkers} • Remaining: {request.remainingPositions}</Typography>
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
                <Typography>{request.eventType} • {request.eventDate} • {request.startTime}-{request.endTime}</Typography>
                <Typography color="text.secondary">{request.location}, {request.area}</Typography>
                <Typography>₹{Number(request.totalAmount).toLocaleString('en-IN')}</Typography>
                <StatusChip status={request.status} />
                <Button variant="outlined" onClick={() => void openDetails(request.id)}>View customer & worker details</Button>
              </Stack>
            </CardContent></Card>
          </Grid>
        ))}
      </Grid>

      <Dialog open={Boolean(details)} onClose={() => setDetails(null)} fullWidth maxWidth="md">
        <DialogTitle>Service Request #{details?.id} — Full Details</DialogTitle>
        <DialogContent dividers>
          {details ? (
            <Stack spacing={2}>
              <Stack spacing={0.5}>
                <Typography variant="h6" sx={{ fontWeight: 800 }}>Customer</Typography>
                <Typography><b>{details.customer.name}</b> • {details.customer.mobileNumber}</Typography>
                <Typography color="text.secondary">{details.customer.email || 'No email'} • {details.customer.username}</Typography>
              </Stack>
              <Divider />
              <Stack spacing={0.5}>
                <Typography variant="h6" sx={{ fontWeight: 800 }}>Request</Typography>
                <Typography>{details.serviceType.replace(/_/g, ' ')} • {details.eventType}</Typography>
                <Typography>{details.eventDate} • {details.startTime}-{details.endTime}</Typography>
                <Typography color="text.secondary">{details.location}, {details.area}</Typography>
                <Typography sx={{ fontWeight: 700 }}>₹{Number(details.totalAmount).toLocaleString('en-IN')} • {details.status}</Typography>
                {details.selectedServices.length ? <Typography>Services: {details.selectedServices.join(', ')}</Typography> : null}
                {details.instructions ? <Typography>Instructions: {details.instructions}</Typography> : null}
              </Stack>
              <Divider />
              <Stack spacing={1}>
                <Typography variant="h6" sx={{ fontWeight: 800 }}>Staffing</Typography>
                {details.staffingJobs.length === 0 ? <Typography color="text.secondary">No linked staffing jobs.</Typography> : details.staffingJobs.map((job) => (
                  <Paper key={job.id} variant="outlined" sx={{ p: 1.5 }}>
                    <Typography sx={{ fontWeight: 800 }}>{job.workerType.replace(/_/g, ' ')}</Typography>
                    <Typography>Required: {job.requiredWorkers} • Accepted: {job.acceptedWorkers} • Remaining: {job.remainingPositions}</Typography>
                    <Typography color="text.secondary">₹{Number(job.paymentPerWorker).toLocaleString('en-IN')} per worker • {job.status}</Typography>
                  </Paper>
                ))}
              </Stack>
              <Divider />
              <Stack spacing={1}>
                <Typography variant="h6" sx={{ fontWeight: 800 }}>Accepted Workers ({details.acceptedWorkers.length})</Typography>
                {details.acceptedWorkers.length === 0 ? <Typography color="text.secondary">No worker has accepted this request yet.</Typography> : details.acceptedWorkers.map((worker) => (
                  <Paper key={`${worker.acceptanceId}-${worker.workerProfileId}`} variant="outlined" sx={{ p: 1.5 }}>
                    <Typography sx={{ fontWeight: 800 }}>{worker.name} • {worker.workerType.replace(/_/g, ' ')}</Typography>
                    <Typography>{worker.mobileNumber} • {worker.email || 'No email'}</Typography>
                    <Typography color="text.secondary">Experience: {worker.experienceYears ?? 0} years • Rating: {worker.rating ?? 0}</Typography>
                    {worker.skills ? <Typography color="text.secondary">Skills: {worker.skills}</Typography> : null}
                    {worker.preferredAreas ? <Typography color="text.secondary">Preferred areas: {worker.preferredAreas}</Typography> : null}
                    <Typography color="success.main" sx={{ fontWeight: 700 }}>{worker.acceptanceStatus || 'ACCEPTED'} • {worker.acceptedAt || ''}</Typography>
                  </Paper>
                ))}
              </Stack>
            </Stack>
          ) : null}
        </DialogContent>
        <DialogActions><Button onClick={() => setDetails(null)}>Close</Button></DialogActions>
      </Dialog>
    </Stack>
  )
}
