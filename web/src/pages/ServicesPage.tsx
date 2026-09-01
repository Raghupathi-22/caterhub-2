import { Alert, Card, CardContent, Grid, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { catalogApi } from '../api/catalogApi'
import { apiErrorMessage } from '../api/http'
import type { CatalogCategory } from '../types/models'

export function ServicesPage() {
  const [categories, setCategories] = useState<CatalogCategory[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    catalogApi
      .getCategories()
      .then(setCategories)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load categories.')))
  }, [])

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>
        Service Categories
      </Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Grid container spacing={2}>
        {categories.map((category) => (
          <Grid item xs={12} sm={6} md={4} key={category.id}>
            <Card component={RouterLink} to={`/services/${category.id}`} sx={{ textDecoration: 'none', height: '100%' }}>
              <CardContent>
                <Typography variant="h6">{category.name}</Typography>
                <Typography color="text.secondary">{category.description}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Stack>
  )
}
