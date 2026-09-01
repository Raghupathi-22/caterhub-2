import { Stack, Typography } from '@mui/material'

export function OffersPublicPage() {
  return (
    <Stack spacing={1}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Offers</Typography>
      <Typography color="text.secondary">Active offers are available after admin publishes them.</Typography>
    </Stack>
  )
}

export function AboutPage() {
  return (
    <Stack spacing={1}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>About CaterHub</Typography>
      <Typography color="text.secondary">Catering & event services marketplace built for fast and reliable bookings.</Typography>
    </Stack>
  )
}

export function ContactPage() {
  return (
    <Stack spacing={1}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Contact</Typography>
      <Typography color="text.secondary">Use the call or WhatsApp actions in the footer for immediate support.</Typography>
    </Stack>
  )
}
