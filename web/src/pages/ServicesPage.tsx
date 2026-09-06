import { ArrowForward } from '@mui/icons-material'
import { Alert, Box, Button, Card, CardContent, Grid, Skeleton, Stack, Typography } from '@mui/material'
import { useEffect } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { foodShowcaseItems, getCategoryVisual, occasionCards } from '../data/marketingContent'
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
    <Stack spacing={{ xs: 3.5, md: 5 }}>
      <Box
        sx={{
          p: { xs: 2.5, md: 4 },
          borderRadius: 4,
          border: '1px solid',
          borderColor: 'divider',
          overflow: 'hidden',
          position: 'relative',
          minHeight: { xs: 240, md: 290 },
          display: 'flex',
          alignItems: 'center',
        }}
      >
        <Box
          component="img"
          src="https://images.unsplash.com/photo-1555244162-803834f70033?auto=format&fit=crop&w=1800&q=80"
          alt="Premium catering and event service setup"
          loading="eager"
          sx={{
            position: 'absolute',
            inset: 0,
            width: '100%',
            height: '100%',
            objectFit: 'cover',
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            background: 'linear-gradient(95deg, rgba(16,41,29,0.88), rgba(16,41,29,0.45))',
          }}
        />
        <Box sx={{ position: 'relative', zIndex: 1, maxWidth: 760 }}>
          <Typography component="h1" variant="h2" sx={{ color: '#fff', mb: 1.2 }}>
            Everything You Need for Your Perfect Event
          </Typography>
          <Typography sx={{ color: 'rgba(255,255,255,0.92)', mb: 2.25 }}>
            From delicious catering to beautiful decor, entertainment and event services - CaterHub brings everything together in one place.
          </Typography>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.25}>
            <Button component="a" href="#categories" variant="contained" color="secondary">
              Explore Services
            </Button>
            <Button component={RouterLink} to="/get-started" variant="outlined" sx={{ color: '#fff', borderColor: 'rgba(255,255,255,0.65)' }}>
              Plan Your Event
            </Button>
          </Stack>
        </Box>
      </Box>

      <Box id="categories">
        <Stack spacing={1}>
          <Typography variant="h4">Service Categories</Typography>
          <Typography color="text.secondary">Choose from curated categories designed for events of every size.</Typography>
        </Stack>
      </Box>

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
                  <Skeleton variant="rectangular" height={180} />
                  <CardContent>
                    <Skeleton variant="text" width="65%" />
                    <Skeleton variant="text" width="95%" />
                    <Skeleton variant="text" width="45%" />
                  </CardContent>
                </Card>
              </Grid>
            ))
          : categories.map((category) => {
              const Icon = getCategoryIcon(category.serviceType, category.icon)
              const visual = getCategoryVisual(category)
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
                      overflow: 'hidden',
                      '&:hover .card-image': { transform: 'scale(1.05)' },
                    }}
                  >
                    <Box sx={{ height: 190, overflow: 'hidden' }}>
                      <Box
                        component="img"
                        src={visual.imageUrl}
                        alt={visual.alt}
                        loading="lazy"
                        className="card-image"
                        sx={{
                          width: '100%',
                          height: '100%',
                          objectFit: 'cover',
                          transition: 'transform 240ms ease',
                        }}
                      />
                    </Box>
                    <CardContent sx={{ p: 2.25 }}>
                      <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                        <Box sx={{ width: 40, height: 40, borderRadius: 2, bgcolor: 'rgba(30,107,68,0.10)', display: 'grid', placeItems: 'center' }}>
                          <Icon color="primary" />
                        </Box>
                        <Typography variant="h6" sx={{ fontSize: '1.1rem' }}>{category.name}</Typography>
                      </Stack>
                      <Typography color="text.secondary" sx={{ mb: 1.2, minHeight: 46 }}>
                        {category.description || visual.shortDescription}
                      </Typography>
                      <Button endIcon={<ArrowForward />} sx={{ px: 0 }}>
                        Explore Services
                      </Button>
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

      <Stack spacing={1}>
        <Typography variant="h4">Made for Every Occasion</Typography>
        <Typography color="text.secondary">From biryani feasts to beverage counters, choose food service that fits your event style.</Typography>
      </Stack>
      <Grid container spacing={2}>
        {foodShowcaseItems.map((item) => (
          <Grid item xs={12} sm={6} md={4} key={item.title}>
            <Card sx={{ border: '1px solid', borderColor: 'divider', overflow: 'hidden', height: '100%' }}>
              <Box component="img" src={item.imageUrl} alt={item.alt} loading="lazy" sx={{ width: '100%', height: 180, objectFit: 'cover' }} />
              <CardContent>
                <Typography sx={{ fontWeight: 700 }}>{item.title}</Typography>
                <Typography variant="body2" color="text.secondary">{item.subtitle}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Stack spacing={1}>
        <Typography variant="h4">Perfect Catering for Every Occasion</Typography>
        <Typography color="text.secondary">Thoughtfully planned services for family milestones, celebrations, and professional events.</Typography>
      </Stack>
      <Grid container spacing={2}>
        {occasionCards.map((item) => (
          <Grid item xs={12} sm={6} md={3} key={item.title}>
            <Card sx={{ border: '1px solid', borderColor: 'divider', overflow: 'hidden', height: '100%' }}>
              <Box component="img" src={item.imageUrl} alt={item.alt} loading="lazy" sx={{ width: '100%', height: 158, objectFit: 'cover' }} />
              <CardContent>
                <Typography sx={{ fontWeight: 700 }}>{item.title}</Typography>
                <Typography variant="body2" color="text.secondary">{item.subtitle}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Stack>
  )
}
