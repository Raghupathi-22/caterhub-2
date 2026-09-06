import { ArrowForward } from '@mui/icons-material'
import { Alert, Box, Button, Card, CardContent, Grid, Skeleton, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { FirstBookingOfferCard } from '../components/FirstBookingOfferCard'
import { useCatalogCategories } from '../hooks/useCatalogCategories'
import { getCategoryIcon } from '../utils/catalogVisuals'

export function PublicHomePage() {
  const { data: categories = [], isLoading, isError, refetch } = useCatalogCategories()

  return (
    <Stack spacing={4}>
      <Box
        sx={{
          py: { xs: 5, md: 8 },
          px: { xs: 2, md: 4 },
          borderRadius: 4,
          border: '1px solid',
          borderColor: 'divider',
          bgcolor: '#FFFDF8',
          backgroundImage: 'radial-gradient(circle at 90% 20%, rgba(23,101,58,0.10), transparent 40%)',
        }}
      >
        <Typography component="h1" variant="h2" sx={{ fontWeight: 800, mb: 1.25, maxWidth: 700 }}>
          Everything You Need for Your Event
        </Typography>
        <Typography color="text.secondary" sx={{ maxWidth: 760, mb: 3 }}>
          Book catering, decoration, entertainment, beauty, photography and event services in one place.
        </Typography>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} alignItems={{ xs: 'stretch', sm: 'center' }}>
          <Button component={RouterLink} to="/services" variant="contained" size="large">
            Book a Service
          </Button>
          <Button component={RouterLink} to="/services" variant="outlined" size="large">
            Explore Services
          </Button>
        </Stack>
      </Box>

      <FirstBookingOfferCard />

      <Stack spacing={1}>
        <Typography variant="h4" sx={{ fontWeight: 800 }}>
          Popular Service Categories
        </Typography>
        <Typography color="text.secondary">
          Find trusted event services by category and start booking quickly.
        </Typography>
      </Stack>

      {isError ? (
        <Alert severity="error" action={<Button color="inherit" size="small" onClick={() => void refetch()}>Retry</Button>}>
          Services are temporarily unavailable.
        </Alert>
      ) : null}

      <Grid container spacing={2.25}>
        {isLoading
          ? Array.from({ length: 6 }).map((_, index) => (
              <Grid item xs={12} sm={6} md={4} key={index}>
                <Card sx={{ height: '100%' }}>
                  <CardContent>
                    <Skeleton variant="circular" width={44} height={44} />
                    <Skeleton variant="text" sx={{ mt: 1.2 }} width="65%" />
                    <Skeleton variant="text" width="100%" />
                    <Skeleton variant="text" width="80%" />
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
                    <CardContent sx={{ p: 2.25 }}>
                      <Box
                        sx={{
                          width: 44,
                          height: 44,
                          borderRadius: 2,
                          bgcolor: 'rgba(23,101,58,0.09)',
                          color: 'primary.main',
                          display: 'grid',
                          placeItems: 'center',
                          mb: 1.25,
                        }}
                      >
                        <Icon fontSize="small" />
                      </Box>
                      <Typography sx={{ fontWeight: 700, mb: 0.5 }}>{category.name}</Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ minHeight: 40 }}>
                        {category.description}
                      </Typography>
                      <Button endIcon={<ArrowForward />} sx={{ px: 0, mt: 0.75 }}>
                        View Services
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
              )
            })}
      </Grid>
    </Stack>
  )
}
