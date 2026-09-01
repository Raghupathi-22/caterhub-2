import { Menu as MenuIcon, Phone, WhatsApp } from '@mui/icons-material'
import {
  AppBar,
  Box,
  Button,
  Container,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemText,
  Stack,
  Toolbar,
  Typography,
} from '@mui/material'
import { useState } from 'react'
import { Link as RouterLink, Outlet } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

const SUPPORT_PHONE = import.meta.env.VITE_SUPPORT_PHONE ?? '+919999999999'
const cleanPhone = SUPPORT_PHONE.replace(/[^\d]/g, '')

const navItems = [
  { label: 'Home', to: '/' },
  { label: 'Services', to: '/services' },
  { label: 'Offers', to: '/offers' },
  { label: 'About', to: '/about' },
  { label: 'Contact', to: '/contact' },
]

export function PublicLayout() {
  const [open, setOpen] = useState(false)
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const logout = useAuthStore((state) => state.logout)

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="sticky" color="inherit" elevation={0} sx={{ borderBottom: '1px solid', borderColor: 'divider' }}>
        <Container maxWidth="xl">
          <Toolbar disableGutters>
            <Typography component={RouterLink} to="/" sx={{ textDecoration: 'none', color: 'text.primary', fontWeight: 800, mr: 4 }}>
              CaterHub
            </Typography>
            <Stack direction="row" spacing={1} sx={{ display: { xs: 'none', md: 'flex' }, flexGrow: 1 }}>
              {navItems.map((item) => (
                <Button key={item.to} component={RouterLink} to={item.to} color="inherit">
                  {item.label}
                </Button>
              ))}
            </Stack>
            <Stack direction="row" spacing={1} sx={{ display: { xs: 'none', md: 'flex' } }}>
              {isAuthenticated ? (
                <>
                  <Button component={RouterLink} to="/home" variant="outlined">Home</Button>
                  <Button component={RouterLink} to="/my-bookings" variant="outlined">My Bookings</Button>
                  <Button variant="contained" onClick={logout}>Logout</Button>
                </>
              ) : (
                <>
                  <Button component={RouterLink} to="/login" variant="outlined">Login</Button>
                  <Button component={RouterLink} to="/register" variant="contained">Get Started</Button>
                </>
              )}
            </Stack>
            <IconButton sx={{ display: { xs: 'inline-flex', md: 'none' } }} onClick={() => setOpen(true)}>
              <MenuIcon />
            </IconButton>
          </Toolbar>
        </Container>
      </AppBar>

      <Drawer anchor="right" open={open} onClose={() => setOpen(false)}>
        <Box sx={{ width: 260 }} role="presentation" onClick={() => setOpen(false)}>
          <List>
            {navItems.map((item) => (
              <ListItemButton key={item.to} component={RouterLink} to={item.to}>
                <ListItemText primary={item.label} />
              </ListItemButton>
            ))}
            <ListItemButton component={RouterLink} to="/join-caterhub">
              <ListItemText primary="Join CaterHub" />
            </ListItemButton>
            <ListItemButton component={RouterLink} to="/admin/login">
              <ListItemText primary="Admin Login" />
            </ListItemButton>
          </List>
        </Box>
      </Drawer>

      <Container maxWidth="xl" sx={{ py: 3 }}>
        <Outlet />
      </Container>

      <Box component="footer" sx={{ py: 3, borderTop: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
        <Container maxWidth="xl">
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }}>
            <Typography variant="body2">Facing issues or have questions?</Typography>
            <Stack direction="row" spacing={1}>
              <Button startIcon={<Phone />} href={`tel:${SUPPORT_PHONE}`} variant="outlined">Call Us</Button>
              <Button
                startIcon={<WhatsApp />}
                href={`https://wa.me/${cleanPhone}`}
                target="_blank"
                rel="noreferrer"
                variant="contained"
              >
                WhatsApp Us
              </Button>
            </Stack>
          </Stack>
        </Container>
      </Box>
    </Box>
  )
}
