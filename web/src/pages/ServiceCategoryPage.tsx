import { Alert, Box, Button, Card, CardContent, Chip, CircularProgress, Stack, Typography } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { catalogApi } from '../api/catalogApi'
import { apiErrorMessage } from '../api/http'
import type { CatalogCategory } from '../types/models'

export function ServiceCategoryPage() {
  const { categoryId } = useParams()
  const [category, setCategory] = useState<CatalogCategory | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    if (!categoryId) {
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    catalogApi
      .getCategory(categoryId)
      .then(setCategory)
      .catch((e: unknown) => {
        if (import.meta.env.DEV) console.error('Failed to load category', e)
        setError(apiErrorMessage(e, 'Category details are temporarily unavailable. Please try again shortly.'))
      })
      .finally(() => setLoading(false))
  }, [categoryId])

  useEffect(() => {
    load()
  }, [load])

  if (!categoryId) return <Alert severity="error">Category is missing in URL.</Alert>
  if (loading) {
    return (
      <Stack alignItems="center" sx={{ py: 5 }}>
        <CircularProgress />
      </Stack>
    )
  }
  if (error) return <Alert severity="warning" action={<Button color="inherit" size="small" onClick={load}>Retry</Button>}>{error}</Alert>
  if (!category) return <Alert severity="info">Category information is not available right now.</Alert>

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
