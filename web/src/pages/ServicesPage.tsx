import { ArrowForward } from '@mui/icons-material'
import { Alert, Button, Card, CardContent, CircularProgress, Grid, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { catalogApi } from '../api/catalogApi'
import { apiErrorMessage } from '../api/http'
import { CategoryIcon } from '../components/CategoryIcon'
import type { CatalogCategory } from '../types/models'

export function ServicesPage() {
  const [categories, setCategories] = useState<CatalogCategory[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = () => {
    setLoading(true)
    setError('')
    catalogApi
      .getCategories()
      .then(setCategories)
      .catch((e: unknown) => {
        if (import.meta.env.DEV) console.error('Failed to load categories', e)
        setError(apiErrorMessage(e, 'Services are temporarily unavailable. Please try again in a moment.'))
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  return (
    <Stack spacing={2.5}>
      <BoxHeader />

      {loading ? (
        <Stack alignItems="center" sx={{ py: 4 }}>
          <CircularProgress />
        </Stack>
      ) : null}

      {!loading && error ? (
        <Alert severity="warning" action={<Button color="inherit" size="small" onClick={load}>Retry</Button>}>
          {error}
        </Alert>
      ) : null}

      {!loading && !error && categories.length === 0 ? (
        <Card sx={{ border: '1px solid', borderColor: 'divider' }}>
          <CardContent>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5 }}>No categories available right now</Typography>
            <Typography color="text.secondary">Please check back shortly for available event services.</Typography>
          </CardContent>
        </Card>
      ) : null}

      {!loading && !error && categories.length > 0 ? (
        <Grid container spacing={2}>
          {categories.map((category) => (
            <Grid item xs={12} sm={6} md={4} key={category.id}>
              <Card
                component={RouterLink}
                to={`/services/${category.id}`}
                sx={{
                  textDecoration: 'none',
                  height: '100%',
                  border: '1px solid',
                  borderColor: 'divider',
                  transition: 'transform 180ms ease, box-shadow 180ms ease',
                  '&:hover': { transform: 'translateY(-3px)', boxShadow: '0 10px 28px rgba(15,23,42,0.10)' },
                }}
              >
                <CardContent>
                  <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                    <CategoryIcon icon={category.icon} sx={{ color: category.accent }} />
                    <Typography variant="h6" sx={{ color: category.accent }}>{category.name}</Typography>
                  </Stack>
                  <Typography color="text.secondary" sx={{ minHeight: 44 }}>{category.description}</Typography>
                  <Typography variant="body2" sx={{ mt: 1, fontWeight: 600, color: 'text.primary' }}>
                    {category.services.length} services available
                  </Typography>
                  <Button endIcon={<ArrowForward />} sx={{ mt: 1 }}>
                    View Services
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : null}
    </Stack>
  )
}

function BoxHeader() {
  return (
    <Stack spacing={0.8}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>
        Service Categories
      </Typography>
      <Typography color="text.secondary">
        Explore event-ready service categories and select the right fit for your booking.
      </Typography>
    </Stack>
  )
}
