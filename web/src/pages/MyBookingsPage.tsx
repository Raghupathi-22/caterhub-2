import { Alert, Card, CardContent, Grid, Stack, Typography } from '@mui/material'
import { useEffect, useMemo, useState } from 'react'
import { bookingApi } from '../api/bookingApi'
import { apiErrorMessage } from '../api/http'
import { StatusChip } from '../components/StatusChip'
import type { BookingDTO, ServiceRequestDTO } from '../types/models'

interface UnifiedBooking {
  id: string
  category: string
  service: string
  eventType: string
  date: string
  time: string
  area: string
  amount: number
  status: string
}

function normalizeBookings(bookings: BookingDTO[]): UnifiedBooking[] {
  return bookings.map((item) => ({
    id: `booking-${item.id}`,
    category: 'Catering & Food',
    service: item.mealType,
    eventType: item.eventType,
    date: item.eventDateTime?.slice(0, 10) ?? '-',
    time: item.eventDateTime?.slice(11, 16) ?? '-',
    area: item.deliveryAddress,
    amount: Number(item.totalAmount),
    status: item.status,
  }))
}

function normalizeServiceRequests(requests: ServiceRequestDTO[]): UnifiedBooking[] {
  return requests.map((item) => ({
    id: `service-${item.id}`,
    category: item.serviceType.replace(/_/g, ' '),
    service: item.selectedServices?.join(', ') || item.serviceType,
    eventType: item.eventType,
    date: item.eventDate,
    time: `${item.startTime} - ${item.endTime}`,
    area: `${item.location}, ${item.area}`,
    amount: Number(item.totalAmount),
    status: item.status,
  }))
}

export function MyBookingsPage() {
  const [bookings, setBookings] = useState<UnifiedBooking[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.allSettled([bookingApi.getMyBookings(), bookingApi.getMyServiceRequests()])
      .then((results) => {
        const [cateringResult, serviceResult] = results
        const combined: UnifiedBooking[] = []
        if (cateringResult.status === 'fulfilled') {
          combined.push(...normalizeBookings(cateringResult.value))
        }
        if (serviceResult.status === 'fulfilled') {
          combined.push(...normalizeServiceRequests(serviceResult.value))
        }
        setBookings(combined)
        if (cateringResult.status === 'rejected' && serviceResult.status === 'rejected') {
          setError(apiErrorMessage(cateringResult.reason, 'Unable to load bookings.'))
        }
      })
      .finally(() => setLoading(false))
  }, [])

  const sortedBookings = useMemo(() => [...bookings].reverse(), [bookings])

  if (loading) return <Typography>Loading bookings...</Typography>
  if (error) return <Alert severity="error">{error}</Alert>
  if (sortedBookings.length === 0) {
    return (
      <Stack spacing={1}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>No bookings yet</Typography>
        <Typography color="text.secondary">Your event plans will appear here.</Typography>
      </Stack>
    )
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>My Bookings</Typography>
      <Grid container spacing={2}>
        {sortedBookings.map((item) => (
          <Grid item xs={12} md={6} key={item.id}>
            <Card>
              <CardContent>
                <Stack spacing={0.8}>
                  <Typography sx={{ fontWeight: 700 }}>{item.category}</Typography>
                  <Typography>{item.service}</Typography>
                  <Typography variant="body2" color="text.secondary">{item.eventType} • {item.date} • {item.time}</Typography>
                  <Typography variant="body2" color="text.secondary">{item.area}</Typography>
                  <Typography variant="body2">₹{item.amount.toLocaleString('en-IN')}</Typography>
                  <StatusChip status={item.status} />
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Stack>
  )
}
