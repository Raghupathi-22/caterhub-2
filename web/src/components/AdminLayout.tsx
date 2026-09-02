import type { ReactNode } from 'react'
import {
  AppBar,
  Box,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemText,
  Toolbar,
  Typography,
  Button,
  Stack,
} from '@mui/material'
import MenuIcon from '@mui/icons-material/Menu'
import { useMemo, useState } from 'react'
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

const drawerWidth = 260

const navItems = [
  { label: 'Dashboard', to: '/admin/dashboard' },
  { label: 'Bookings', to: '/admin/bookings' },
  { label: 'Customers', to: '/admin/customers' },
  { label: 'Workers', to: '/admin/workers' },
  { label: 'Verification', to: '/admin/verification' },
  { label: 'Services', to: '/admin/services' },
  { label: 'Categories', to: '/admin/categories' },
  { label: 'Offers', to: '/admin/offers' },
  { label: 'Support', to: '/admin/support' },
  { label: 'Reports', to: '/admin/reports' },
  { label: 'Settings', to: '/admin/settings' },
]

function AdminNav({ onNavigate }: { onNavigate?: () => void }) {
  const location = useLocation()
  const isSelected = (to: string) => {
    if (to === '/admin/dashboard') {
      return location.pathname === '/admin' || location.pathname === '/admin/dashboard'
    }
    return location.pathname === to || location.pathname.startsWith(`${to}/`)
  }

  return (
    <List sx={{ px: 1 }}>
      {navItems.map((item) => (
        <ListItemButton
          key={item.to}
          component={RouterLink}
          to={item.to}
          selected={isSelected(item.to)}
          onClick={onNavigate}
          sx={{ borderRadius: 1 }}
        >
          <ListItemText primary={item.label} />
        </ListItemButton>
      ))}
    </List>
  )
}

export function AdminLayout({ children }: { children?: ReactNode }) {
  const [mobileOpen, setMobileOpen] = useState(false)
  const user = useAuthStore((state) => state.user)
  const logout = useAuthStore((state) => state.logout)
  const name = useMemo(() => [user?.first_name, user?.last_name].filter(Boolean).join(' ') || user?.username || 'Admin', [user])

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar
        position="fixed"
        color="inherit"
        elevation={0}
        sx={{ borderBottom: '1px solid', borderColor: 'divider', width: { md: `calc(100% - ${drawerWidth}px)` }, ml: { md: `${drawerWidth}px` } }}
      >
        <Toolbar>
          <IconButton edge="start" onClick={() => setMobileOpen(true)} sx={{ mr: 1, display: { md: 'none' } }}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 700 }}>
            CaterHub Admin
          </Typography>
          <Stack direction="row" spacing={1} alignItems="center">
            <Typography variant="body2">{name}</Typography>
            <Button size="small" variant="contained" onClick={logout}>
              Logout
            </Button>
          </Stack>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ display: { xs: 'block', md: 'none' }, '& .MuiDrawer-paper': { width: drawerWidth } }}
        >
          <Toolbar>
            <Typography sx={{ fontWeight: 700 }}>Admin Console</Typography>
          </Toolbar>
          <AdminNav onNavigate={() => setMobileOpen(false)} />
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{ display: { xs: 'none', md: 'block' }, '& .MuiDrawer-paper': { width: drawerWidth, boxSizing: 'border-box' } }}
          open
        >
          <Toolbar>
            <Typography sx={{ fontWeight: 700 }}>Admin Console</Typography>
          </Toolbar>
          <AdminNav />
        </Drawer>
      </Box>

      <Box component="main" sx={{ flexGrow: 1, p: 3, width: { md: `calc(100% - ${drawerWidth}px)` } }}>
        <Toolbar />
        {children ?? <Outlet />}
      </Box>
    </Box>
  )
}
