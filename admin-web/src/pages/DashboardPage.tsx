import { useEffect, useMemo, useState } from 'react'
import { Alert, Card, CardContent, CircularProgress, Grid, Stack, Typography } from '@mui/material'
import { adminApi } from '../api/adminApi'
import { useAuthStore } from '../store/authStore'
import type { DashboardSummary } from '../types/api'

const defaultSummary: DashboardSummary = {
  totalOrders: 0,
  pendingOrders: 0,
  deliveredOrders: 0,
  cancelledOrders: 0,
  totalRevenue: 0,
  averageOrderValue: 0,
}

export function DashboardPage() {
  const businessId = useAuthStore((state) => state.user?.business_id ?? 1)
  const [summary, setSummary] = useState<DashboardSummary>(defaultSummary)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const data = await adminApi.getDashboardSummary(businessId)
        setSummary(data)
      } catch {
        setError('Unable to load dashboard summary.')
      } finally {
        setLoading(false)
      }
    }
    void fetchSummary()
  }, [businessId])

  const cards = useMemo(
    () => [
      { label: 'Total Orders', value: summary.totalOrders },
      { label: 'Pending Orders', value: summary.pendingOrders },
      { label: 'Delivered Orders', value: summary.deliveredOrders },
      { label: 'Cancelled Orders', value: summary.cancelledOrders },
      { label: 'Total Revenue', value: `₹${summary.totalRevenue.toFixed(2)}` },
      { label: 'Avg Order Value', value: `₹${summary.averageOrderValue.toFixed(2)}` },
    ],
    [summary],
  )

  if (loading) {
    return <CircularProgress />
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" fontWeight={700}>
        Business Dashboard
      </Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Grid container spacing={2}>
        {cards.map((card) => (
          <Grid size={{ xs: 12, md: 4 }} key={card.label}>
            <Card>
              <CardContent>
                <Typography variant="body2" color="text.secondary">
                  {card.label}
                </Typography>
                <Typography variant="h5" fontWeight={700}>
                  {card.value}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Stack>
  )
}
