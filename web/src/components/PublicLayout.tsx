import { Menu as MenuIcon, Phone, WhatsApp } from '@mui/icons-material'
import {
  AppBar,
  Box,
  Button,
  Chip,
  Container,
  Divider,
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
import { CaterhubLogo } from './CaterhubLogo'

const navItems = [
  { label: 'Home', to: '/' },
  { label: 'Services', to: '/services' },
  { label: 'Offers', to: '/offers' },
  { label: 'About', to: '/about' },
  { label: 'Contact', to: '/contact' },
]

const footerServiceLinks = [
  { label: 'Catering', to: '/services' },
  { label: 'Decoration', to: '/services' },
  { label: 'Entertainment', to: '/services' },
  { label: 'Photography', to: '/services' },
]

function isExternal(url: string): boolean {
  return /^https?:\/\//i.test(url)
}

function isNavActive(pathname: string, navTo: string): boolean {
  if (navTo === '/') return pathname === '/'
  return pathname.startsWith(navTo)
}

export function PublicLayout() {
  const [open, setOpen] = useState(false)
  const location = useLocation()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const logout = useAuthStore((state) => state.logout)

  const adminIsExternal = isExternal(siteConfig.adminLoginUrl)
  const cleanPhoneHref = siteConfig.callHref
  const whatsappHref = siteConfig.whatsappHref

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', bgcolor: 'background.default' }}>
      <Box sx={{ bgcolor: '#1B5A3A', color: '#F4E8CD', py: 0.75, borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
        <Container maxWidth="xl">
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={0.5} justifyContent="space-between" alignItems={{ sm: 'center' }}>
            <Typography variant="caption" sx={{ fontWeight: 700 }}>
              Premium catering and event services for every occasion.
            </Typography>
            <Chip
              label={siteConfig.supportPhoneDisplay}
              size="small"
              sx={{ bgcolor: 'rgba(255,255,255,0.14)', color: '#FFF8E8', fontWeight: 700 }}
            />
          </Stack>
        </Container>
      </Box>

      <AppBar
        position="sticky"
        color="inherit"
        elevation={0}
        sx={{
          top: 0,
          bgcolor: 'rgba(255, 253, 252, 0.92)',
          borderBottom: '1px solid',
          borderColor: 'divider',
          boxShadow: '0 8px 24px rgba(17, 24, 39, 0.06)',
          backdropFilter: 'blur(12px)',
        }}
      >
        <Container maxWidth="xl">
          <Toolbar disableGutters sx={{ minHeight: { xs: 70, md: 82 } }}>
            <CaterhubLogo />

            <Stack direction="row" spacing={0.5} sx={{ ml: 4, display: { xs: 'none', md: 'flex' }, flexGrow: 1 }}>
              {navItems.map((item) => {
                const active = isNavActive(location.pathname, item.to)
                return (
                  <Button
                    key={item.to}
                    component={RouterLink}
                    to={item.to}
                    color="inherit"
                    sx={{
                      px: 1.7,
                      color: active ? 'primary.dark' : 'text.primary',
                      fontWeight: active ? 800 : 600,
                      bgcolor: active ? 'rgba(30,107,68,0.10)' : 'transparent',
                      '&:hover': { bgcolor: 'rgba(30,107,68,0.08)' },
                    }}
                  >
                    {item.label}
                  </Button>
                )
              })}
            </Stack>

            <Stack direction="row" spacing={1} sx={{ display: { xs: 'none', md: 'flex' }, ml: 'auto' }}>
              {isAuthenticated ? (
                <>
                  <Button component={RouterLink} to="/my-bookings" variant="outlined">My Bookings</Button>
                  <Button variant="contained" onClick={logout}>Logout</Button>
                </>
              ) : (
                <>
                  <Button component={RouterLink} to="/login" variant="text">Login</Button>
                  <Button component={RouterLink} to="/get-started" variant="contained">Get Started</Button>
                </>
              )}
              <Button
                component={adminIsExternal ? 'a' : RouterLink}
                href={adminIsExternal ? siteConfig.adminLoginUrl : undefined}
                to={adminIsExternal ? undefined : siteConfig.adminLoginUrl}
                target={adminIsExternal ? '_blank' : undefined}
                rel={adminIsExternal ? 'noreferrer' : undefined}
                variant="outlined"
                color="inherit"
              >
                Admin Login
              </Button>
            </Stack>

            <IconButton
              sx={{ display: { xs: 'inline-flex', md: 'none' }, ml: 'auto' }}
              onClick={() => setOpen(true)}
              aria-label="Open navigation menu"
            >
              <MenuIcon />
            </IconButton>
          </Toolbar>
        </Container>
      </AppBar>

      <Drawer anchor="right" open={open} onClose={() => setOpen(false)}>
        <Box sx={{ width: 300 }} role="presentation" onClick={() => setOpen(false)}>
          <Box sx={{ px: 2, py: 2 }}>
            <CaterhubLogo />
          </Box>
          <Divider />
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

      <Box component="footer" sx={{ borderTop: '1px solid', borderColor: 'divider', bgcolor: '#10291D' }}>
        <Container maxWidth="xl" sx={{ py: { xs: 4, md: 5 } }}>
          <Stack spacing={3}>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={3} justifyContent="space-between">
              <Box sx={{ maxWidth: 360 }}>
                <CaterhubLogo textColor="#fff" />
                <Typography sx={{ mt: 1.5, color: 'rgba(250,250,250,0.78)' }}>
                  Premium catering and event services for every occasion.
                </Typography>
              </Box>

              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={{ xs: 2.5, sm: 5 }}>
                <Box>
                  <Typography sx={{ fontWeight: 700, mb: 1, color: '#fff' }}>Company</Typography>
                  <Stack spacing={0.75}>
                    <Typography component={RouterLink} to="/about" sx={{ color: 'rgba(250,250,250,0.78)', textDecoration: 'none' }}>
                      About
                    </Typography>
                    <Typography component={RouterLink} to="/contact" sx={{ color: 'rgba(250,250,250,0.78)', textDecoration: 'none' }}>
                      Contact
                    </Typography>
                    <Typography sx={{ color: 'rgba(250,250,250,0.78)' }}>
                      Get the CaterHub App
                    </Typography>
                    <Typography
                      component={siteConfig.hasPlayStoreUrl ? 'a' : 'span'}
                      href={siteConfig.hasPlayStoreUrl ? siteConfig.playStoreUrl : undefined}
                      target={siteConfig.hasPlayStoreUrl ? '_blank' : undefined}
                      rel={siteConfig.hasPlayStoreUrl ? 'noopener noreferrer' : undefined}
                      sx={{ color: 'rgba(250,250,250,0.78)', textDecoration: 'none' }}
                    >
                      Download on Google Play
                    </Typography>
                  </Stack>
                </Box>
                <Box>
                  <Typography sx={{ fontWeight: 700, mb: 1, color: '#fff' }}>Services</Typography>
                  <Stack spacing={0.75}>
                    {footerServiceLinks.map((item) => (
                      <Typography key={item.label} component={RouterLink} to={item.to} sx={{ color: 'rgba(250,250,250,0.78)', textDecoration: 'none' }}>
                        {item.label}
                      </Typography>
                    ))}
                  </Stack>
                </Box>
                <Box>
                  <Typography sx={{ fontWeight: 700, mb: 1, color: '#fff' }}>Support</Typography>
                  <Typography variant="body2" sx={{ mb: 1.25, color: 'rgba(250,250,250,0.78)' }}>
                    {siteConfig.supportPhoneDisplay}
                  </Typography>
                  <Stack spacing={1}>
                    <Button startIcon={<Phone />} href={cleanPhoneHref} variant="outlined" sx={{ color: '#fff', borderColor: 'rgba(255,255,255,0.5)' }}>
                      Call
                    </Button>
                    <Button
                      startIcon={<WhatsApp />}
                      href={whatsappHref}
                      target="_blank"
                      rel="noreferrer"
                      variant="contained"
                      color="secondary"
                    >
                      WhatsApp
                    </Button>
                  </Stack>
                </Box>
              </Stack>
            </Stack>
            <Typography variant="body2" sx={{ color: 'rgba(250,250,250,0.65)' }}>
              © 2026 CaterHub. All rights reserved.
            </Typography>
          </Stack>
        </Container>
      </Box>

      <Stack
        spacing={1}
        sx={{
          position: 'fixed',
          right: { xs: 12, md: 18 },
          bottom: { xs: 14, md: 18 },
          zIndex: 1200,
        }}
      >
        <IconButton
          aria-label={`Call support at ${siteConfig.supportPhoneDisplay}`}
          component="a"
          href={cleanPhoneHref}
          sx={{
            bgcolor: '#1E6B44',
            color: '#fff',
            width: 48,
            height: 48,
            boxShadow: '0 10px 18px rgba(30,107,68,0.32)',
            '&:hover': { bgcolor: '#154D31' },
          }}
        >
          <Phone fontSize="small" />
        </IconButton>
        <IconButton
          aria-label={`Chat on WhatsApp at ${siteConfig.supportPhoneDisplay}`}
          component="a"
          href={whatsappHref}
          target="_blank"
          rel="noreferrer"
          sx={{
            bgcolor: '#25D366',
            color: '#fff',
            width: 48,
            height: 48,
            boxShadow: '0 10px 18px rgba(17,24,39,0.20)',
            '&:hover': { bgcolor: '#1DA851' },
          }}
        >
          <WhatsApp fontSize="small" />
        </IconButton>
      </Stack>
    </Box>
  )
}
