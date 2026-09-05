import { Alert, Box, Button, Card, CardContent, Grid, Skeleton, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { useEffect } from 'react'
import { useCatalogCategories } from '../hooks/useCatalogCategories'
import { getCategoryIcon } from '../utils/catalogVisuals'

export function ServicesPage() {
  const { data: categories = [], isLoading, isError, error, refetch } = useCatalogCategories()

  useEffect(() => {
    if (isError) {
      console.error('Failed to load service categories', error)
    }
  }, [error, isError])

  return (
    <Stack spacing={2.5}>
      <Typography component="h1" variant="h3" sx={{ fontWeight: 800 }}>
        Service Categories
      </Typography>

      {isError ? (
        <Alert
          severity="error"
          action={
            <Button color="inherit" size="small" onClick={() => void refetch()}>
              Retry
            </Button>
          }
        >
          Services are temporarily unavailable.
        </Alert>
      ) : null}

      <Grid container spacing={2.25}>
        {isLoading
          ? Array.from({ length: 8 }).map((_, index) => (
              <Grid item xs={12} sm={6} md={4} key={index}>
                <Card sx={{ height: '100%' }}>
                  <CardContent>
                    <Skeleton variant="circular" width={42} height={42} />
                    <Skeleton variant="text" sx={{ mt: 1.5 }} width="65%" />
                    <Skeleton variant="text" width="95%" />
                  </CardContent>
                </Card>
              </Grid>
            ))
          : categories.map((category) => {
              const Icon = getCategoryIcon(category.serviceType, category.icon)
              return (
                <Grid item xs={12} sm={6} md={4} key={category.id}>
                  <Card
                    component={RouterLink}
                    to={`/services/${category.id}`}
                    sx={{
                      textDecoration: 'none',
                      color: 'text.primary',
                      border: '1px solid',
                      borderColor: 'divider',
                      height: '100%',
                      transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                      '&:hover': { transform: 'translateY(-2px)', boxShadow: 6 },
                    }}
                  >
                    <CardContent>
                      <Box
                        sx={{
                          width: 42,
                          height: 42,
                          borderRadius: 2,
                          bgcolor: 'rgba(23,101,58,0.09)',
                          color: 'primary.main',
                          display: 'grid',
                          placeItems: 'center',
                          mb: 1.2,
                        }}
                      >
                        <Icon fontSize="small" />
                      </Box>
                      <Typography variant="h6" sx={{ fontSize: '1.05rem' }}>{category.name}</Typography>
                      <Typography color="text.secondary">{category.description}</Typography>
                    </CardContent>
                  </Card>
                </Grid>
              )
            })}
      </Grid>

      {!isLoading && !isError && categories.length === 0 ? (
        <Card>
          <CardContent>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>No service categories available</Typography>
            <Typography color="text.secondary">Please check back shortly for available services.</Typography>
          </CardContent>
        </Card>
      ) : null}
    </Stack>
  )
}
