import { Android, OpenInNew, Smartphone } from '@mui/icons-material'
import { Box, Button, Card, Chip, Grid, Stack, Typography } from '@mui/material'
import { siteConfig } from '../config/siteConfig'

export function AppDownloadSection() {
  const buttonProps = siteConfig.hasPlayStoreUrl
    ? {
        component: 'a' as const,
        href: siteConfig.playStoreUrl,
        target: '_blank',
        rel: 'noopener noreferrer',
      }
    : undefined

  return (
    <Card
      sx={{
        p: { xs: 2.25, md: 3.5 },
        borderRadius: 4,
        border: '1px solid',
        borderColor: 'divider',
        bgcolor: '#FFFDF8',
        backgroundImage: 'radial-gradient(circle at 75% 22%, rgba(197,138,22,0.14), transparent 48%)',
      }}
    >
      <Grid container spacing={{ xs: 2.5, md: 3.5 }} alignItems="center">
        <Grid item xs={12} md={7} sx={{ order: { xs: 2, md: 1 } }}>
          <Stack spacing={1.5}>
            <Chip
              icon={<Android fontSize="small" />}
              label="CaterHub mobile app"
              sx={{ alignSelf: 'flex-start', bgcolor: 'rgba(30,107,68,0.10)', color: 'primary.dark', fontWeight: 700 }}
            />
            <Typography variant="h4" component="h2">
              Explore More with the CaterHub App
            </Typography>
            <Typography color="text.secondary">
              Plan your event, explore catering services, and manage your bookings easily from the CaterHub mobile app.
              Download the CaterHub app to book catering and event services with confidence.
            </Typography>
            <Stack spacing={0.8} alignItems={{ xs: 'stretch', sm: 'flex-start' }}>
              <Button
                {...buttonProps}
                variant="contained"
                size="large"
                endIcon={siteConfig.hasPlayStoreUrl ? <OpenInNew /> : undefined}
                aria-label="Download CaterHub App from Google Play"
                disabled={!siteConfig.hasPlayStoreUrl}
                sx={{ minWidth: { sm: 250 } }}
              >
                Download CaterHub App
              </Button>
              <Typography variant="body2" color="text.secondary">
                Available on Google Play
              </Typography>
            </Stack>
          </Stack>
        </Grid>

        <Grid item xs={12} md={5} sx={{ order: { xs: 1, md: 2 } }}>
          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <Box
              sx={{
                width: { xs: 180, sm: 200, md: 220 },
                borderRadius: 6,
                p: 1.1,
                border: '1px solid rgba(17, 24, 39, 0.12)',
                bgcolor: '#111827',
                boxShadow: '0 22px 42px rgba(17, 24, 39, 0.18)',
              }}
            >
              <Box
                sx={{
                  borderRadius: 5,
                  bgcolor: '#FDF8EC',
                  p: 2.2,
                  minHeight: { xs: 290, sm: 320, md: 340 },
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                }}
              >
                <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                  <Box
                    component="img"
                    src="/assets/caterhub-logo.png"
                    alt="CaterHub app logo"
                    loading="lazy"
                    sx={{ width: 84, height: 84, borderRadius: 3, objectFit: 'cover', border: '1px solid rgba(17, 24, 39, 0.08)' }}
                  />
                </Box>
                <Stack spacing={1} sx={{ textAlign: 'center' }}>
                  <Smartphone sx={{ alignSelf: 'center', color: 'primary.main' }} />
                  <Typography sx={{ fontWeight: 800, color: 'text.primary' }}>CaterHub Android App</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Event planning and booking in one premium mobile experience.
                  </Typography>
                </Stack>
              </Box>
            </Box>
          </Box>
        </Grid>
      </Grid>
    </Card>
  )
}
