import { Box, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'

interface CaterhubLogoProps {
  to?: string
  textColor?: string
}

export function CaterhubLogo({ to = '/', textColor = 'text.primary' }: CaterhubLogoProps) {
  return (
    <Box
      component={RouterLink}
      to={to}
      sx={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 1.2,
        minWidth: 0,
        textDecoration: 'none',
      }}
      aria-label="CaterHub Home"
    >
      <Box
        component="img"
        src="/assets/caterhub-logo.png"
        alt="CaterHub"
        sx={{ width: 42, height: 42, objectFit: 'contain', borderRadius: 1 }}
      />
      <Typography
        variant="h6"
        sx={{
          color: textColor,
          fontWeight: 800,
          letterSpacing: '-0.02em',
          lineHeight: 1,
          display: { xs: 'none', sm: 'block' },
        }}
      >
        CaterHub
      </Typography>
    </Box>
  )
}
