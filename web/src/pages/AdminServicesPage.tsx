import { Alert, Box, Button, Card, CardContent, Chip, Grid, Stack, Typography } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { apiErrorMessage } from '../api/http'
import { catalogApi } from '../api/catalogApi'
import type { CatalogCategory } from '../types/models'
import { catalogSummary } from '../utils/adminContact'

export function AdminServicesPage() {
  const [categories, setCategories] = useState<CatalogCategory[] | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    setLoading(true)
    setError('')
    catalogApi
      .getCatalog()
      .then((response) => setCategories(response.categories))
      .catch((e: unknown) => {
        setCategories(null)
        setError(apiErrorMessage(e, 'Unable to load the service catalogue.'))
      })
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const summary = categories ? catalogSummary(categories) : null

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Services</Typography>
      <Alert severity="info">Service management is catalog-driven through /catalog and currently read-only.</Alert>

      {loading ? <Typography color="text.secondary">Loading service catalogue...</Typography> : null}

      {!loading && error ? (
        <Alert severity="error" action={<Button color="inherit" size="small" onClick={load}>Retry</Button>}>{error}</Alert>
      ) : null}

      {!loading && !error && categories && categories.length === 0 ? (
        <Alert severity="warning">The service catalogue is empty. No services are currently configured.</Alert>
      ) : null}

      {!loading && !error && summary && categories && categories.length > 0 ? (
        <>
          <Typography color="text.secondary">
            {summary.categories} categories · {summary.services} services · {summary.roles} worker roles
          </Typography>
          <Grid container spacing={2}>
            {categories.map((category) => (
              <Grid item xs={12} md={6} key={category.id}>
                <Card sx={{ height: '100%', borderTop: 4, borderColor: category.accent || 'primary.main' }}>
                  <CardContent>
                    <Stack spacing={1.5}>
                      <Box>
                        <Stack direction="row" justifyContent="space-between" alignItems="flex-start" spacing={1}>
                          <Typography variant="h6" sx={{ fontWeight: 700 }}>{category.name}</Typography>
                          <Chip size="small" variant="outlined" label={category.serviceType.replace(/_/g, ' ')} />
                        </Stack>
                        <Typography variant="body2" color="text.secondary">{category.description}</Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.5 }}>Services ({category.services.length})</Typography>
                        {category.services.length ? (
                          <Stack direction="row" flexWrap="wrap" gap={0.75}>
                            {category.services.map((service) => <Chip key={service} size="small" label={service} />)}
                          </Stack>
                        ) : (
                          <Typography variant="body2" color="text.secondary">No services listed.</Typography>
                        )}
                      </Box>
                      {category.roles.length ? (
                        <Box>
                          <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.5 }}>Worker roles ({category.roles.length})</Typography>
                          <Stack direction="row" flexWrap="wrap" gap={0.75}>
                            {category.roles.map((role) => <Chip key={role.id} size="small" variant="outlined" label={role.title} />)}
                          </Stack>
                        </Box>
                      ) : null}
                    </Stack>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </>
      ) : null}
    </Stack>
  )
}
