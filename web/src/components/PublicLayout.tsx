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
import { useEffect, useMemo, useState } from 'react'
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom'
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
  const location = useLocation()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const logout = useAuthStore((state) => state.logout)
  const pageMeta = useMemo(() => {
    const pathname = location.pathname
    if (pathname === '/services') return { title: 'Services | CaterHub', description: 'Browse catering and event service categories on CaterHub.' }
    if (pathname.startsWith('/services/')) return { title: 'Service Details | CaterHub', description: 'View available services and book your event with CaterHub.' }
    if (pathname === '/offers') return { title: 'Offers | CaterHub', description: 'Discover active CaterHub offers for your next event booking.' }
    if (pathname === '/about') return { title: 'About | CaterHub', description: 'Learn about CaterHub, your event-services marketplace.' }
    if (pathname === '/contact') return { title: 'Contact | CaterHub', description: 'Contact CaterHub support by call or WhatsApp.' }
    if (pathname === '/login') return { title: 'Customer Login | CaterHub', description: 'Sign in to CaterHub with OTP to manage bookings.' }
    if (pathname === '/register' || pathname === '/get-started') return { title: 'Get Started | CaterHub', description: 'Create your CaterHub account and start booking event services.' }
    return { title: 'CaterHub - Catering & Event Services', description: 'Book catering, decoration, entertainment, beauty, photography and event services in one place.' }
  }, [location.pathname])

  useEffect(() => {
    document.title = pageMeta.title
    const description = document.querySelector('meta[name="description"]')
    if (description) {
      description.setAttribute('content', pageMeta.description)
    }
  }, [pageMeta])

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
                  <Button component={RouterLink} to="/admin/login" variant="text">Admin Login</Button>
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
            {!isAuthenticated ? (
              <ListItemButton component={RouterLink} to="/register">
                <ListItemText primary="Get Started" />
              </ListItemButton>
            ) : null}
            {!isAuthenticated ? (
              <ListItemButton component={RouterLink} to="/login">
                <ListItemText primary="Customer Login" />
              </ListItemButton>
            ) : null}
            <ListItemButton component={RouterLink} to="/admin/login">
              <ListItemText primary="Admin Login" />
            </ListItemButton>
          </List>
        </Box>
      </Drawer>

      <Container maxWidth="xl" sx={{ py: { xs: 2.5, md: 3.5 } }}>
        <Outlet />
      </Container>

      <Box component="footer" sx={{ py: 4, borderTop: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
        <Container maxWidth="xl">
          <Stack spacing={2.5}>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.5} justifyContent="space-between" alignItems={{ xs: 'flex-start', md: 'center' }}>
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 800, mb: 0.5 }}>CaterHub</Typography>
                <Typography variant="body2" color="text.secondary">Event services marketplace for seamless planning and booking.</Typography>
              </Box>
              <Stack direction="row" spacing={2} flexWrap="wrap">
                <Button component={RouterLink} to="/services" color="inherit" size="small">Services</Button>
                <Button component={RouterLink} to="/about" color="inherit" size="small">About</Button>
                <Button component={RouterLink} to="/contact" color="inherit" size="small">Contact</Button>
                <Button component={RouterLink} to="/login" color="inherit" size="small">Customer Login</Button>
                <Button component={RouterLink} to="/admin/login" color="inherit" size="small">Admin Login</Button>
              </Stack>
            </Stack>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }}>
              <Typography variant="body2" color="text.secondary">Need support? Reach out and we will help with your booking.</Typography>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} width={{ xs: '100%', sm: 'auto' }}>
                <Button fullWidth={true} startIcon={<Phone />} href={`tel:${SUPPORT_PHONE}`} variant="outlined">Call Us</Button>
                <Button
                  fullWidth={true}
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
            <Typography variant="caption" color="text.secondary">© {new Date().getFullYear()} CaterHub. All rights reserved.</Typography>
          </Stack>
        </Container>
      </Box>
    </Box>
  )
}
