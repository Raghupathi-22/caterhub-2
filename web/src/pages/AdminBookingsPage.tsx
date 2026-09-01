import { Alert, Button, MenuItem, Paper, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { StatusChip } from '../components/StatusChip'
import { useAuthStore } from '../store/authStore'
import type { BookingDTO } from '../types/models'

const statuses = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED']

export function AdminBookingsPage() {
  const businessId = useAuthStore((state) => state.user?.business_id ?? 1)
  const [rows, setRows] = useState<BookingDTO[]>([])
  const [error, setError] = useState('')

  const load = () => {
    setError('')
    adminApi
      .getOrders(businessId)
      .then(setRows)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load bookings.')))
  }

  useEffect(() => {
    load()
  }, [businessId])

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
                <TableCell sx={{ minWidth: 180 }}>
                  <TextField select size="small" fullWidth value={row.status} onChange={(e) => void updateStatus(row.id, e.target.value)}>
                    {statuses.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
                  </TextField>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Stack>
  )
}
