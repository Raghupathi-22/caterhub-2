import { Box, Container, Typography } from '@mui/material'

export const HomePage = () => {
  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 8 }}>
        <Typography variant="h3" component="h1" gutterBottom>
          Welcome to Cetaring
        </Typography>
        <Typography variant="h6" color="text.secondary" paragraph>
          Your premier catering booking platform in Hyderabad
        </Typography>
        <Typography variant="body1" paragraph>
          Book delicious catering for your events - birthdays, weddings, corporate events, and more.
        </Typography>
      </Box>
    </Container>
  )
}

