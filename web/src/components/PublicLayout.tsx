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
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom'
import { siteConfig } from '../config/siteConfig'
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
                  <Button component={RouterLink} to="/login" variant="text">Login</Button>
                  <Button component={RouterLink} to="/get-started" variant="contained">Get Started</Button>
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
            <ListItemButton component={RouterLink} to="/login">
              <ListItemText primary="Login" />
            </ListItemButton>
            <ListItemButton component={RouterLink} to="/get-started">
              <ListItemText primary="Get Started" />
            </ListItemButton>
            <ListItemButton
              component={adminIsExternal ? 'a' : RouterLink}
              href={adminIsExternal ? siteConfig.adminLoginUrl : undefined}
              to={adminIsExternal ? undefined : siteConfig.adminLoginUrl}
            >
              <ListItemText primary="Admin Login" />
            </ListItemButton>
          </List>
        </Box>
      </Drawer>

      <Box component="main" sx={{ flex: 1, py: { xs: 2.5, md: 4 } }}>
        <Container maxWidth="xl">
          <Outlet />
        </Container>
      </Box>

      <Box component="footer" sx={{ borderTop: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
        <Container maxWidth="xl" sx={{ py: { xs: 4, md: 5 } }}>
          <Stack spacing={3}>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={3} justifyContent="space-between">
              <Box sx={{ maxWidth: 340 }}>
                <CaterhubLogo />
                <Typography color="text.secondary" sx={{ mt: 1.5 }}>
                  Event services marketplace for seamless planning and booking.
                </Typography>
              </Box>

              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={{ xs: 2.5, sm: 5 }}>
                <Box>
                  <Typography sx={{ fontWeight: 700, mb: 1 }}>Quick Links</Typography>
                  <Stack spacing={0.75}>
                    {navItems.map((item) => (
                      <Typography
                        key={item.to}
                        component={RouterLink}
                        to={item.to}
                        sx={{ color: 'text.secondary', textDecoration: 'none', '&:hover': { color: 'text.primary' } }}
                      >
                        {item.label}
                      </Typography>
                    ))}
                  </Stack>
                </Box>
                <Box>
                  <Typography sx={{ fontWeight: 700, mb: 1 }}>Account</Typography>
                  <Stack spacing={0.75}>
                    {accountLinks.map((item) => (
                      <Typography
                        key={item.to}
                        component={RouterLink}
                        to={item.to}
                        sx={{ color: 'text.secondary', textDecoration: 'none', '&:hover': { color: 'text.primary' } }}
                      >
                        {item.label}
                      </Typography>
                    ))}
                    <Typography
                      component={adminIsExternal ? 'a' : RouterLink}
                      href={adminIsExternal ? siteConfig.adminLoginUrl : undefined}
                      to={adminIsExternal ? undefined : siteConfig.adminLoginUrl}
                      target={adminIsExternal ? '_blank' : undefined}
                      rel={adminIsExternal ? 'noreferrer' : undefined}
                      sx={{ color: 'text.secondary', textDecoration: 'none', '&:hover': { color: 'text.primary' } }}
                    >
                      Admin Login
                    </Typography>
                  </Stack>
                </Box>
                <Box>
                  <Typography sx={{ fontWeight: 700, mb: 1 }}>Support</Typography>
                  <Stack spacing={1}>
                    <Button startIcon={<Phone />} href={cleanPhoneHref} variant="outlined">Call Us</Button>
                    <Button
                      startIcon={<WhatsApp />}
                      href={whatsappHref}
                      target="_blank"
                      rel="noreferrer"
                      variant="contained"
                      color="secondary"
                    >
                      WhatsApp Us
                    </Button>
                  </Stack>
                </Box>
              </Stack>
            </Stack>
            <Typography variant="body2" color="text.secondary">
              © 2026 CaterHub. All rights reserved.
            </Typography>
          </Stack>
        </Container>
      </Box>
    </Box>
  )
}
