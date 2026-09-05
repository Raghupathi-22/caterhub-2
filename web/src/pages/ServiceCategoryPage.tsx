import { Alert, Box, Button, Card, CardContent, Chip, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { catalogApi } from '../api/catalogApi'
import { apiErrorMessage } from '../api/http'
import type { CatalogCategory } from '../types/models'

export function ServiceCategoryPage() {
  const { categoryId } = useParams()
  const [category, setCategory] = useState<CatalogCategory | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!categoryId) return
    catalogApi
      .getCategory(categoryId)
      .then(setCategory)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load selected category.')))
  }, [categoryId])

  if (!categoryId) return <Alert severity="error">Category is missing in URL.</Alert>
  if (error) return <Alert severity="error">Services are temporarily unavailable.</Alert>
  if (!category) return <Typography color="text.secondary">Loading category details...</Typography>

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 700 }}>{category.name}</Typography>
        <Typography color="text.secondary">{category.description}</Typography>
      </Box>
      <Card>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 1 }}>Available Services</Typography>
          <Stack direction="row" gap={1} flexWrap="wrap">
            {category.services.map((service) => <Chip key={service} label={service} />)}
          </Stack>
        </CardContent>
      </Card>
      <Button component={RouterLink} to={`/booking/${category.id}`} variant="contained">
        Book {category.name}
      </Button>
    </Stack>
  )
}
