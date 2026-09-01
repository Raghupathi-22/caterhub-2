import { ArrowForward } from '@mui/icons-material'
import { Alert, Box, Button, Card, CardContent, Grid, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { catalogApi } from '../api/catalogApi'
import { apiErrorMessage } from '../api/http'
import type { CatalogCategory } from '../types/models'

export function PublicHomePage() {
  const [categories, setCategories] = useState<CatalogCategory[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    catalogApi
      .getCategories()
      .then(setCategories)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load services.')))
  }, [])

  return (
    <Stack spacing={3}>
      <Box sx={{ py: { xs: 4, md: 7 } }}>
        <Typography variant="h3" sx={{ fontWeight: 800, mb: 1 }}>
          Everything You Need for Your Event
        </Typography>
        <Typography color="text.secondary" sx={{ maxWidth: 760, mb: 3 }}>
          Book catering, decoration, entertainment, beauty, photography and event services in one place.
        </Typography>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
          <Button component={RouterLink} to="/services" variant="contained" size="large">
            Book a Service
          </Button>
          <Button component={RouterLink} to="/services" variant="outlined" size="large">
            Explore Services
          </Button>
          <Button component={RouterLink} to="/join-caterhub" variant="text" size="large">
            Join CaterHub
          </Button>
        </Stack>
      </Box>

      {error ? <Alert severity="error">{error}</Alert> : null}

      <Grid container spacing={2}>
        {categories.map((category) => (
          <Grid item xs={12} sm={6} md={4} key={category.id}>
            <Card sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', height: '100%' }}>
              <CardContent>
                <Typography sx={{ color: category.accent, fontWeight: 700, mb: 0.5 }}>{category.name}</Typography>
                <Typography variant="body2" color="text.secondary" sx={{ minHeight: 40 }}>
                  {category.description}
                </Typography>
                <Button component={RouterLink} to={`/services/${category.id}`} endIcon={<ArrowForward />} sx={{ mt: 1 }}>
                  View Services
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Stack>
  )
}
