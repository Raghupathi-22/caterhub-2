import { ArrowForward } from '@mui/icons-material'
import { Alert, Box, Button, Card, CardContent, Chip, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { catalogApi } from '../api/catalogApi'
import { apiErrorMessage } from '../api/http'
import { getCategoryVisual } from '../data/marketingContent'
import { getCategoryIcon } from '../utils/catalogVisuals'
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

  const Icon = getCategoryIcon(category.serviceType, category.icon)
  const visual = getCategoryVisual(category)

  return (
    <Stack spacing={2.5}>
      <Card sx={{ overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}>
        <Box sx={{ height: { xs: 220, md: 290 }, position: 'relative' }}>
          <Box component="img" src={visual.imageUrl} alt={visual.alt} loading="eager" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          <Box sx={{ position: 'absolute', inset: 0, background: 'linear-gradient(95deg, rgba(16,41,29,0.88), rgba(16,41,29,0.45))' }} />
          <Stack spacing={1} sx={{ position: 'absolute', left: { xs: 18, md: 24 }, right: { xs: 18, md: 24 }, bottom: { xs: 18, md: 24 } }}>
            <Stack direction="row" spacing={1} alignItems="center">
              <Box sx={{ width: 38, height: 38, borderRadius: 2, bgcolor: 'rgba(255,255,255,0.2)', display: 'grid', placeItems: 'center' }}>
                <Icon sx={{ color: '#fff' }} />
              </Box>
              <Typography variant="h4" sx={{ color: '#fff' }}>{category.name}</Typography>
            </Stack>
            <Typography sx={{ color: 'rgba(255,255,255,0.92)', maxWidth: 760 }}>{category.description || visual.shortDescription}</Typography>
          </Stack>
        </Box>
      </Card>

      <Card sx={{ border: '1px solid', borderColor: 'divider' }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 1, fontWeight: 700 }}>Available Services</Typography>
          <Stack direction="row" gap={1} flexWrap="wrap">
            {category.services.map((service) => <Chip key={service} label={service} />)}
          </Stack>
        </CardContent>
      </Card>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.25}>
        <Button component={RouterLink} to={`/booking/${category.id}`} variant="contained" endIcon={<ArrowForward />}>
          Book {category.name}
        </Button>
        <Button component={RouterLink} to="/services" variant="outlined">
          Explore More Services
        </Button>
      </Stack>
    </Stack>
  )
}
