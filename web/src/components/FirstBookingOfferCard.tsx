import { CelebrationOutlined } from '@mui/icons-material'
import { Box, Button, Card, CardContent, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'

export function FirstBookingOfferCard() {
  return (
    <Card
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        bgcolor: '#FFFDF8',
        backgroundImage: 'linear-gradient(125deg, rgba(23,101,58,0.10), rgba(255,255,255,0) 55%)',
      }}
    >
      <CardContent sx={{ p: { xs: 2.25, md: 3 } }}>
        <Stack spacing={1.25}>
          <Stack direction="row" spacing={1} alignItems="center">
            <Box
              sx={{
                width: 34,
                height: 34,
                borderRadius: 1.5,
                bgcolor: 'rgba(23,101,58,0.12)',
                color: 'primary.main',
                display: 'grid',
                placeItems: 'center',
              }}
            >
              <CelebrationOutlined fontSize="small" />
            </Box>
            <Typography variant="overline" sx={{ color: 'primary.main', fontWeight: 800, letterSpacing: 1.1 }}>
              FIRST BOOKING SPECIAL
            </Typography>
          </Stack>

          <Typography component="h2" variant="h4" sx={{ fontWeight: 800, lineHeight: 1.1 }}>
            10% OFF
          </Typography>
          <Typography sx={{ fontWeight: 600 }}>On your first CaterHub booking</Typography>
          <Typography color="text.secondary">Maximum discount ₹1,000</Typography>
          <Typography color="text.secondary">
            New to CaterHub? Celebrate your first event with a special launch discount.
          </Typography>

          <Button component={RouterLink} to="/services" variant="contained" sx={{ alignSelf: 'flex-start', mt: 0.5 }}>
            Book Now &amp; Save
          </Button>

          <Typography variant="caption" color="text.secondary">
            Offer valid for first-time customers only. Maximum discount ₹1,000. Terms and conditions apply.
          </Typography>
        </Stack>
      </CardContent>
    </Card>
  )
}
