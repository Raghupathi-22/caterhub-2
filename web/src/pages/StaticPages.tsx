import { Phone, WhatsApp } from '@mui/icons-material'
import { Alert, Box, Button, Card, CardContent, CircularProgress, Grid, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { catalogApi } from '../api/catalogApi'
import { apiErrorMessage } from '../api/http'
import type { PublicOffer } from '../types/models'

const SUPPORT_PHONE = import.meta.env.VITE_SUPPORT_PHONE ?? '+919999999999'
const CLEAN_SUPPORT_PHONE = SUPPORT_PHONE.replace(/[^\d]/g, '')

function offerLabel(offer: PublicOffer): string {
  if (offer.discountType === 'PERCENTAGE') return `${offer.discountValue}% OFF`
  if (offer.discountType === 'FLAT_AMOUNT') return `₹${Number(offer.discountValue).toLocaleString('en-IN')} OFF`
  if (offer.discountType === 'FREE_DELIVERY') return 'Free Delivery'
  return 'Buy 1 Get 1'
}

export function OffersPublicPage() {
  const [offers, setOffers] = useState<PublicOffer[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadOffers = () => {
    setLoading(true)
    setError('')
    catalogApi
      .getPublicOffers()
      .then(setOffers)
      .catch((e: unknown) => {
        if (import.meta.env.DEV) console.error('Failed to load public offers', e)
        setError(apiErrorMessage(e, 'Offers are temporarily unavailable. Please try again shortly.'))
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadOffers()
  }, [])

  return (
    <Stack spacing={2.5}>
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 700 }}>Offers</Typography>
        <Typography color="text.secondary">Active deals currently available on CaterHub.</Typography>
      </Box>

      {loading ? (
        <Stack alignItems="center" sx={{ py: 5 }}>
          <CircularProgress />
        </Stack>
      ) : null}

      {!loading && error ? (
        <Alert severity="warning" action={<Button color="inherit" size="small" onClick={loadOffers}>Retry</Button>}>
          {error}
        </Alert>
      ) : null}

      {!loading && !error && offers.length === 0 ? (
        <Card sx={{ border: '1px solid', borderColor: 'divider' }}>
          <CardContent>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5 }}>Offers coming soon</Typography>
            <Typography color="text.secondary">New promotional offers will appear here once they are published.</Typography>
          </CardContent>
        </Card>
      ) : null}

      {!loading && !error && offers.length > 0 ? (
        <Grid container spacing={2}>
          {offers.map((offer) => (
            <Grid item xs={12} sm={6} md={4} key={offer.id}>
              <Card sx={{ height: '100%', border: '1px solid', borderColor: 'divider' }}>
                <CardContent>
                  <Typography variant="overline" color="primary.main" sx={{ fontWeight: 700 }}>{offerLabel(offer)}</Typography>
                  <Typography variant="h6" sx={{ fontWeight: 700 }}>{offer.couponCode}</Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ minHeight: 44 }}>{offer.description}</Typography>
                  <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
                    Valid until {new Date(offer.validUntil).toLocaleDateString('en-IN')}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : null}
    </Stack>
  )
}

export function AboutPage() {
  return (
    <Stack spacing={2.5}>
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 700 }}>About CaterHub</Typography>
        <Typography color="text.secondary">
          CaterHub is an event-services marketplace that helps customers discover and book catering and related services from one platform.
        </Typography>
      </Box>
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%', border: '1px solid', borderColor: 'divider' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.8 }}>What we help you book</Typography>
              <Typography color="text.secondary">
                Catering, decoration, entertainment, beauty, photography, rentals, logistics, staffing and other event support services.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%', border: '1px solid', borderColor: 'divider' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.8 }}>How it works</Typography>
              <Typography color="text.secondary">
                Browse service categories, choose what you need, submit event details, and manage your requests in one place.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
      <Card sx={{ border: '1px solid', borderColor: 'divider' }}>
        <CardContent>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.8 }}>Built for practical event planning</Typography>
          <Typography color="text.secondary">
            CaterHub focuses on clear service discovery, straightforward booking flows, and reliable support for both customers and admin operations.
          </Typography>
        </CardContent>
      </Card>
    </Stack>
  )
}

export function ContactPage() {
  return (
    <Stack spacing={2.5}>
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 700 }}>Contact & Support</Typography>
        <Typography color="text.secondary">Facing an issue or have a question? We are here to help with your booking.</Typography>
      </Box>

      <Card sx={{ border: '1px solid', borderColor: 'divider' }}>
        <CardContent>
          <Stack spacing={2}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>Need help with your booking?</Typography>
            <Typography color="text.secondary">Reach our support team through call or WhatsApp.</Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
              <Button startIcon={<Phone />} href={`tel:${SUPPORT_PHONE}`} variant="outlined" fullWidth>
                Call Us
              </Button>
              <Button
                startIcon={<WhatsApp />}
                href={`https://wa.me/${CLEAN_SUPPORT_PHONE}`}
                target="_blank"
                rel="noreferrer"
                variant="contained"
                fullWidth
              >
                WhatsApp Us
              </Button>
            </Stack>
          </Stack>
        </CardContent>
      </Card>

      <Button component={RouterLink} to="/services" variant="text">
        Browse Services
      </Button>
    </Stack>
  )
}
