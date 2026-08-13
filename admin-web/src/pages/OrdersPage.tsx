import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  MenuItem,
  Paper,
  Snackbar,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import dayjs from 'dayjs'
import { adminApi } from '../api/adminApi'
import { useAuthStore } from '../store/authStore'
import type { Booking } from '../types/api'
import { StatusChip } from '../components/StatusChip'

const statuses = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED'] as const

export function OrdersPage() {
  const businessId = useAuthStore((state) => state.user?.business_id ?? 1)
  const [orders, setOrders] = useState<Booking[]>([])
  const [error, setError] = useState('')
  const [banner, setBanner] = useState('')
  const seenKeysRef = useRef<Set<string>>(new Set())

  const notify = (message: string) => {
    setBanner(message)
    if ('Notification' in window) {
      if (Notification.permission === 'granted') {
        new Notification('Cetaring Admin', { body: message })
      } else if (Notification.permission !== 'denied') {
        void Notification.requestPermission()
      }
    }
  }

  const loadOrders = useCallback(async () => {
    try {
      const data = await adminApi.getOrders(businessId)
      setOrders(data)
      data.forEach((order) => {
        const key = `${order.id}-${order.status}`
        if (!seenKeysRef.current.has(key)) {
          seenKeysRef.current.add(key)
          if (order.status === 'PENDING') {
            notify(`New pending order #${order.id} (${order.eventType})`)
          }
        }
      })
    } catch {
      setError('Unable to fetch orders.')
    }
  }, [businessId])

  useEffect(() => {
    void loadOrders()
    const timer = window.setInterval(() => void loadOrders(), 20000)
    return () => window.clearInterval(timer)
  }, [loadOrders])

  const updateStatus = async (bookingId: number, status: string) => {
    try {
      const updated = await adminApi.updateOrderStatus(bookingId, status)
      setOrders((current) => current.map((item) => (item.id === bookingId ? updated : item)))
      notify(`Order #${bookingId} status updated to ${status}`)
    } catch {
      setError('Unable to update status.')
    }
  }

  const rows = useMemo(
    () =>
      orders.map((order) => (
        <TableRow key={order.id} hover>
          <TableCell>{order.id}</TableCell>
          <TableCell>{order.eventType}</TableCell>
          <TableCell>{order.guestCount}</TableCell>
          <TableCell>₹{Number(order.totalAmount).toFixed(2)}</TableCell>
          <TableCell>{order.deliveryAddress}</TableCell>
          <TableCell>{dayjs(order.eventDateTime).format('DD MMM YYYY, hh:mm A')}</TableCell>
          <TableCell>
            <StatusChip status={order.status} />
          </TableCell>
          <TableCell sx={{ minWidth: 180 }}>
            <TextField
              select
              size="small"
              value={order.status}
              onChange={(e) => void updateStatus(order.id, e.target.value)}
              fullWidth
            >
              {statuses.map((status) => (
                <MenuItem key={status} value={status}>
                  {status}
                </MenuItem>
              ))}
            </TextField>
          </TableCell>
        </TableRow>
      )),
    [orders],
  )

  return (
    <Stack spacing={2}>
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Typography variant="h4" fontWeight={700}>
          Live Orders
        </Typography>
        <Button variant="outlined" onClick={() => void loadOrders()}>
          Refresh
        </Button>
      </Box>

      {error ? <Alert severity="error">{error}</Alert> : null}

      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Order ID</TableCell>
              <TableCell>Event</TableCell>
              <TableCell>Guests</TableCell>
              <TableCell>Total</TableCell>
              <TableCell>Location</TableCell>
              <TableCell>Event Time</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Update Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>{rows}</TableBody>
        </Table>
      </Paper>

      <Snackbar
        open={Boolean(banner)}
        autoHideDuration={4000}
        onClose={() => setBanner('')}
        message={banner}
      />
    </Stack>
  )
}
