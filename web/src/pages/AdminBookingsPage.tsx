import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, MenuItem, Paper, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { StatusChip } from '../components/StatusChip'
import { useAuthStore } from '../store/authStore'
import type { AdminBookingDetails, BookingDTO } from '../types/models'

const statuses = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED']

export function AdminBookingsPage() {
  const businessId = useAuthStore((state) => state.user?.business_id ?? 1)
  const [rows, setRows] = useState<BookingDTO[]>([])
  const [error, setError] = useState('')
  const [details, setDetails] = useState<AdminBookingDetails | null>(null)
  const [detailsLoading, setDetailsLoading] = useState(false)

  const load = useCallback(() => {
    setError('')
    adminApi
      .getOrders(businessId)
      .then(setRows)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load bookings.')))
  }, [businessId])

  useEffect(() => {
    load()
  }, [load])


  const openDetails = async (id: number) => {
    try {
      setDetailsLoading(true)
      setError('')
      setDetails(await adminApi.getBookingDetails(id))
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to load booking details.'))
    } finally {
      setDetailsLoading(false)
    }
  }

  const updateStatus = async (id: number, status: string) => {
    try {
      const updated = await adminApi.updateOrderStatus(id, status)
      setRows((current) => current.map((item) => (item.id === id ? updated : item)))
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to update booking status.'))
    }
  }

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between">
        <Typography variant="h4" sx={{ fontWeight: 700 }}>Bookings</Typography>
        <Button variant="outlined" onClick={load}>Refresh</Button>
      </Stack>
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Booking ID</TableCell>
              <TableCell>Event</TableCell>
              <TableCell>Date</TableCell>
              <TableCell>Area</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Action</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>
                <TableCell>{row.id}</TableCell>
                <TableCell>{row.eventType}</TableCell>
                <TableCell>{row.eventDateTime?.slice(0, 10)}</TableCell>
                <TableCell>{row.deliveryAddress}</TableCell>
                <TableCell>₹{Number(row.totalAmount).toLocaleString('en-IN')}</TableCell>
                <TableCell><StatusChip status={row.status} /></TableCell>
                <TableCell sx={{ minWidth: 240 }}>
                  <Stack spacing={1}>
                  <Button size="small" variant="outlined" onClick={() => void openDetails(row.id)} disabled={detailsLoading}>View full details</Button>
                  <TextField select size="small" fullWidth value={row.status} onChange={(e) => void updateStatus(row.id, e.target.value)}>
                    {statuses.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
                  </TextField>
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>

      <Dialog open={Boolean(details)} onClose={() => setDetails(null)} fullWidth maxWidth="md">
        <DialogTitle>Booking #{details?.id} — Customer & Accepted Workers</DialogTitle>
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
                <Typography variant="h6" sx={{ fontWeight: 800 }}>Booking</Typography>
                <Typography>{details.bookingReference || `#${details.id}`} • {details.eventType} • {details.guestCount} guests</Typography>
                <Typography color="text.secondary">{details.eventDate || details.eventDateTime?.slice(0, 10)} • {details.deliveryAddress}</Typography>
                <Typography sx={{ fontWeight: 700 }}>₹{Number(details.totalAmount).toLocaleString('en-IN')} • {details.status}</Typography>
              </Stack>
              <Divider />
              <Stack spacing={1}>
                <Typography variant="h6" sx={{ fontWeight: 800 }}>Accepted Workers ({details.acceptedWorkers.length})</Typography>
                {details.acceptedWorkers.length === 0 ? <Typography color="text.secondary">No worker has accepted this booking yet.</Typography> : details.acceptedWorkers.map((worker) => (
                  <Paper key={`${worker.assignmentId}-${worker.workerProfileId}`} variant="outlined" sx={{ p: 1.5 }}>
                    <Typography sx={{ fontWeight: 800 }}>{worker.name} • {worker.workerType.replace(/_/g, ' ')}</Typography>
                    <Typography>{worker.mobileNumber} • {worker.email || 'No email'}</Typography>
                    <Typography color="text.secondary">Experience: {worker.experienceYears ?? 0} years • Rating: {worker.rating ?? 0}</Typography>
                    {worker.skills ? <Typography color="text.secondary">Skills: {worker.skills}</Typography> : null}
                    <Typography color="success.main" sx={{ fontWeight: 700 }}>{worker.assignmentStatus || 'ACCEPTED'}</Typography>
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
