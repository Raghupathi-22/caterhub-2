import { ArrowForward, SupportAgent } from '@mui/icons-material'
import { Alert, Button, Card, CardContent, CircularProgress, Grid, Stack, Typography } from '@mui/material'
import { useEffect, useMemo, useState } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { catalogApi } from '../api/catalogApi'
import { apiErrorMessage } from '../api/http'
import { CategoryIcon } from '../components/CategoryIcon'
import type { CatalogCategory, PublicOffer } from '../types/models'

const SUPPORT_PHONE = import.meta.env.VITE_SUPPORT_PHONE ?? '+919999999999'
const CLEAN_SUPPORT_PHONE = SUPPORT_PHONE.replace(/[^\d]/g, '')

function offerBadge(offer: PublicOffer): string {
  if (offer.discountType === 'PERCENTAGE') return `${offer.discountValue}% OFF`
  if (offer.discountType === 'FLAT_AMOUNT') return `₹${Number(offer.discountValue).toLocaleString('en-IN')} OFF`
  if (offer.discountType === 'FREE_DELIVERY') return 'Free Delivery'
  return 'Buy 1 Get 1'
}

export function PublicHomePage() {
  const [categories, setCategories] = useState<CatalogCategory[]>([])
  const [offers, setOffers] = useState<PublicOffer[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadData = () => {
    setLoading(true)
    setError('')
    Promise.all([catalogApi.getCategories(), catalogApi.getPublicOffers().catch(() => [])])
      .then(([categoriesData, offersData]) => {
        setCategories(categoriesData)
        setOffers(offersData)
      })
      .catch((e: unknown) => {
        if (import.meta.env.DEV) console.error('Failed to load home data', e)
        setError(apiErrorMessage(e, 'Services are temporarily unavailable. Please try again in a moment.'))
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadData()
  }, [])

  const featuredOffers = useMemo(() => offers.slice(0, 3), [offers])

  return (
    <Stack spacing={{ xs: 3, md: 4 }}>
      <Card sx={{ border: '1px solid', borderColor: 'divider', bgcolor: '#FFFDF7' }}>
        <CardContent sx={{ p: { xs: 3, md: 5 } }}>
          <Stack spacing={2}>
            <Typography variant="h3" sx={{ fontWeight: 800, lineHeight: 1.15 }}>
              Everything You Need for Your Event
            </Typography>
            <Typography color="text.secondary" sx={{ maxWidth: 780 }}>
              Book catering, decoration, entertainment, beauty, photography and event services in one place.
            </Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
              <Button component={RouterLink} to="/services" variant="contained" size="large">
                Book a Service
              </Button>
              <Button component={RouterLink} to="/services" variant="outlined" size="large">
                Explore Services
              </Button>
            </Stack>
          </Stack>
        </CardContent>
      </Card>

      <Stack spacing={1}>
        <Typography variant="h4" sx={{ fontWeight: 700 }}>Service Categories</Typography>
        <Typography color="text.secondary">Choose from curated categories designed for event planning.</Typography>
      </Stack>

      {loading ? (
        <Stack alignItems="center" sx={{ py: 4 }}>
          <CircularProgress />
        </Stack>
      ) : null}

      {!loading && error ? (
        <Alert severity="warning" action={<Button color="inherit" size="small" onClick={loadData}>Retry</Button>}>
          {error}
        </Alert>
      ) : null}

      {!loading && !error && categories.length > 0 ? (
        <Grid container spacing={2}>
          {categories.map((category) => (
            <Grid item xs={12} sm={6} md={4} key={category.id}>
              <Card
                sx={{
                  height: '100%',
                  border: '1px solid',
                  borderColor: 'divider',
                  transition: 'transform 180ms ease, box-shadow 180ms ease',
                  '&:hover': { transform: 'translateY(-3px)', boxShadow: '0 10px 28px rgba(15,23,42,0.10)' },
                }}
              >
                <CardContent>
                  <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 1 }}>
                    <CategoryIcon icon={category.icon} sx={{ color: category.accent }} />
                    <Typography sx={{ color: category.accent, fontWeight: 700 }}>{category.name}</Typography>
                  </Stack>
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
      ) : null}

      {!loading && !error && categories.length === 0 ? (
        <Card sx={{ border: '1px solid', borderColor: 'divider' }}>
          <CardContent>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5 }}>Services are being updated</Typography>
            <Typography color="text.secondary">Please check back shortly for available categories.</Typography>
          </CardContent>
        </Card>
      ) : null}

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%', border: '1px solid', borderColor: 'divider' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.8 }}>Why CaterHub</Typography>
              <Stack spacing={0.8}>
                <Typography color="text.secondary">• Multiple event services in one place</Typography>
                <Typography color="text.secondary">• Simple booking flow with event details</Typography>
                <Typography color="text.secondary">• Trusted marketplace with admin-managed operations</Typography>
                <Typography color="text.secondary">• Support-ready planning for customers and workers</Typography>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%', border: '1px solid', borderColor: 'divider' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.8 }}>How it works</Typography>
              <Stack spacing={0.8}>
                <Typography color="text.secondary">1. Choose a service</Typography>
                <Typography color="text.secondary">2. Select your event details</Typography>
                <Typography color="text.secondary">3. Submit your booking</Typography>
                <Typography color="text.secondary">4. Get connected with the right service or worker</Typography>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Stack spacing={1}>
        <Typography variant="h4" sx={{ fontWeight: 700 }}>Offers</Typography>
        <Typography color="text.secondary">Latest deals available for your upcoming events.</Typography>
      </Stack>

      {featuredOffers.length > 0 ? (
        <Grid container spacing={2}>
          {featuredOffers.map((offer) => (
            <Grid item xs={12} sm={6} md={4} key={offer.id}>
              <Card sx={{ border: '1px solid', borderColor: 'divider', height: '100%' }}>
                <CardContent>
                  <Typography variant="overline" color="primary.main" sx={{ fontWeight: 700 }}>{offerBadge(offer)}</Typography>
                  <Typography variant="h6" sx={{ fontWeight: 700 }}>{offer.couponCode}</Typography>
                  <Typography variant="body2" color="text.secondary">{offer.description}</Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Card sx={{ border: '1px solid', borderColor: 'divider' }}>
          <CardContent>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5 }}>Offers coming soon</Typography>
            <Typography color="text.secondary">Check back for upcoming promotional deals.</Typography>
          </CardContent>
        </Card>
      )}

      <Card sx={{ border: '1px solid', borderColor: 'divider', bgcolor: '#FFFFFF' }}>
        <CardContent sx={{ p: { xs: 2.5, md: 3 } }}>
          <Stack spacing={1.5}>
            <Stack direction="row" spacing={1} alignItems="center">
              <SupportAgent color="primary" />
              <Typography variant="h6" sx={{ fontWeight: 700 }}>Need help with your booking?</Typography>
            </Stack>
            <Typography color="text.secondary">Facing an issue or have a question? We&apos;re here to help.</Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.2}>
              <Button href={`tel:${SUPPORT_PHONE}`} variant="outlined" fullWidth>Call Us</Button>
              <Button href={`https://wa.me/${CLEAN_SUPPORT_PHONE}`} target="_blank" rel="noreferrer" variant="contained" fullWidth>WhatsApp Us</Button>
            </Stack>
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  )
}
