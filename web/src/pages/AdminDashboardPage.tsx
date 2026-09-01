import { Alert, Card, CardContent, Grid, Stack, Typography } from '@mui/material'
import { useEffect, useMemo, useState } from 'react'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { useAuthStore } from '../store/authStore'
import type { AdminDashboardSummary } from '../types/models'

const emptySummary: AdminDashboardSummary = {
  totalOrders: 0,
  pendingOrders: 0,
  deliveredOrders: 0,
  cancelledOrders: 0,
  totalRevenue: 0,
  averageOrderValue: 0,
}

export function AdminDashboardPage() {
  const businessId = useAuthStore((state) => state.user?.business_id ?? 1)
  const [summary, setSummary] = useState<AdminDashboardSummary>(emptySummary)
  const [error, setError] = useState('')

  useEffect(() => {
    adminApi
      .getDashboardSummary(businessId)
      .then(setSummary)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load dashboard summary.')))
  }, [businessId])

  const cards = useMemo(
    () => [
      { label: 'Total Bookings', value: summary.totalOrders },
      { label: 'Pending Bookings', value: summary.pendingOrders },
      { label: 'Confirmed/Delivered', value: summary.deliveredOrders },
      { label: 'Cancelled', value: summary.cancelledOrders },
      { label: 'Revenue', value: `₹${Number(summary.totalRevenue).toLocaleString('en-IN')}` },
      { label: 'Avg Booking Value', value: `₹${Number(summary.averageOrderValue).toLocaleString('en-IN')}` },
    ],
    [summary],
  )

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Dashboard</Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Grid container spacing={2}>
        {cards.map((card) => (
          <Grid key={card.label} item xs={12} md={4}>
            <Card><CardContent>
              <Typography variant="body2" color="text.secondary">{card.label}</Typography>
              <Typography variant="h5" sx={{ fontWeight: 700 }}>{card.value}</Typography>
            </CardContent></Card>
          </Grid>
        ))}
      </Grid>
    </Stack>
  )
}
