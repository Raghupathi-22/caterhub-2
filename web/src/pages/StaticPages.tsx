import { Phone, WhatsApp } from '@mui/icons-material'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Grid,
  Skeleton,
  Stack,
  Typography,
} from '@mui/material'
import { useEffect } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { publicOffersApi } from '../api/publicOffersApi'
import { FirstBookingOfferCard } from '../components/FirstBookingOfferCard'
import { siteConfig } from '../config/siteConfig'

export function OffersPublicPage() {
  const { data: offers = [], isLoading, isError, error, refetch } = useQuery({
    queryKey: ['offers', 'active'],
    queryFn: publicOffersApi.getActiveOffers,
    staleTime: 1000 * 60 * 3,
  })

  useEffect(() => {
    if (isError) {
      console.error('Failed to load offers', error)
    }
  }, [error, isError])

  return (
    <Stack spacing={2.5}>
      <Typography component="h1" variant="h3" sx={{ fontWeight: 800 }}>Offers</Typography>
      <Typography color="text.secondary">
        Explore the latest promotions available for your event services.
      </Typography>
      <FirstBookingOfferCard />

      {isError ? (
        <Alert action={<Button color="inherit" size="small" onClick={() => void refetch()}>Retry</Button>} severity="error">
          Offers are temporarily unavailable.
        </Alert>
      ) : null}

      <Grid container spacing={2}>
        {isLoading
          ? Array.from({ length: 4 }).map((_, index) => (
              <Grid item xs={12} md={6} key={index}>
                <Card><CardContent>
                  <Skeleton width="55%" />
                  <Skeleton width="100%" />
                  <Skeleton width="75%" />
                </CardContent></Card>
              </Grid>
            ))
          : offers.map((offer) => (
              <Grid item xs={12} md={6} key={offer.id}>
                <Card sx={{ border: '1px solid', borderColor: 'divider', height: '100%' }}>
                  <CardContent>
                    <Stack spacing={1}>
                      <Typography variant="h6" sx={{ fontWeight: 700 }}>{offer.title}</Typography>
                      <Typography color="text.secondary">{offer.description}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Applicable: {offer.applicableCategory ?? 'All services'}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Valid till: {new Date(offer.validUntil).toLocaleDateString('en-IN')}
                      </Typography>
                      <Button component={RouterLink} to="/services" variant="contained" sx={{ alignSelf: 'flex-start' }}>
                        {offer.ctaLabel}
                      </Button>
                    </Stack>
                  </CardContent>
                </Card>
              </Grid>
            ))}
      </Grid>

      {!isLoading && !isError && offers.length === 0 ? (
        <Card>
          <CardContent>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5 }}>Offers coming soon</Typography>
            <Typography color="text.secondary">Check back for upcoming promotional deals.</Typography>
          </CardContent>
        </Card>
      ) : null}
    </Stack>
  )
}

export function AboutPage() {
  const bookingItems = [
    'Catering',
    'Decoration',
    'Entertainment',
    'Beauty',
    'Photography & Video',
    'Rentals',
    'Transport & Logistics',
    'Staffing',
    'Religious & Ceremony Services',
    'Event Support',
  ]

  return (
    <Stack spacing={3}>
      <Box>
        <Typography component="h1" variant="h3" sx={{ fontWeight: 800, mb: 1 }}>About CaterHub</Typography>
        <Typography color="text.secondary">
          CaterHub is an event-services marketplace that helps customers discover and book catering and related services from one platform.
        </Typography>
      </Box>

      <Card>
        <CardContent>
          <Typography variant="h5" sx={{ fontWeight: 700, mb: 1.5 }}>What We Help You Book</Typography>
          <Grid container spacing={1}>
            {bookingItems.map((item) => (
              <Grid item xs={12} sm={6} md={4} key={item}>
                <Typography color="text.secondary">- {item}</Typography>
              </Grid>
            ))}
          </Grid>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h5" sx={{ fontWeight: 700, mb: 1.5 }}>How It Works</Typography>
          <Stack spacing={0.75}>
            <Typography color="text.secondary">1. Browse services</Typography>
            <Typography color="text.secondary">2. Choose what you need</Typography>
            <Typography color="text.secondary">3. Select your event details</Typography>
            <Typography color="text.secondary">4. Submit your request</Typography>
            <Typography color="text.secondary">5. Manage your booking</Typography>
          </Stack>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h5" sx={{ fontWeight: 700, mb: 1.5 }}>Why CaterHub</Typography>
          <Stack spacing={0.75}>
            <Typography color="text.secondary">- Multiple event services in one place</Typography>
            <Typography color="text.secondary">- Simple booking experience</Typography>
            <Typography color="text.secondary">- Convenient event planning</Typography>
            <Typography color="text.secondary">- Customer support</Typography>
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  )
}

export function ContactPage() {
  return (
    <Stack spacing={3}>
      <Box>
        <Typography component="h1" variant="h3" sx={{ fontWeight: 800, mb: 1 }}>Contact & Support</Typography>
        <Typography color="text.secondary">
          Facing an issue or have a question? We&apos;re here to help with your booking.
        </Typography>
      </Box>

      <Card sx={{ border: '1px solid', borderColor: 'divider' }}>
        <CardContent sx={{ p: { xs: 2.25, md: 3 } }}>
          <Stack spacing={2}>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>Need help with your booking?</Typography>
            <Typography color="text.secondary">
              Reach our support team through call or WhatsApp.
            </Typography>
            <Typography sx={{ fontWeight: 700 }}>
              Call/WhatsApp: {siteConfig.supportPhoneDisplay}
            </Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.25}>
              <Button startIcon={<Phone />} href={siteConfig.callHref} variant="outlined" size="large">
                Call Us
              </Button>
              <Button
                startIcon={<WhatsApp />}
                href={siteConfig.whatsappHref}
                target="_blank"
                rel="noreferrer"
                variant="contained"
                color="secondary"
                size="large"
              >
                WhatsApp Us
              </Button>
            </Stack>
          </Stack>
        </CardContent>
      </Card>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.25}>
        <Button component={RouterLink} to="/services" variant="outlined">Browse Services</Button>
        <Button component={RouterLink} to="/login" variant="outlined">Customer Login</Button>
        <Button component={RouterLink} to="/get-started" variant="contained">Get Started</Button>
      </Stack>
    </Stack>
  )
}
