import type { ReactNode } from 'react'
import {
  AppBar,
  Box,
  Button,
  Container,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material'
import DashboardIcon from '@mui/icons-material/Dashboard'
import AssignmentIcon from '@mui/icons-material/Assignment'
import LocalOfferIcon from '@mui/icons-material/LocalOffer'
import EventIcon from '@mui/icons-material/Event'
import LogoutIcon from '@mui/icons-material/Logout'
import { useAuthStore } from '../store/authStore'

const drawerWidth = 240

const navItems = [
  { label: 'Dashboard', to: '/', icon: <DashboardIcon /> },
  { label: 'Orders', to: '/orders', icon: <AssignmentIcon /> },
  { label: 'Offers', to: '/offers', icon: <LocalOfferIcon /> },
  { label: 'Events', to: '/events', icon: <EventIcon /> },
]

interface AdminLayoutProps {
  children: ReactNode
  currentPath: string
  onNavigate: (to: string) => void
}

export function AdminLayout({ children, currentPath, onNavigate }: AdminLayoutProps) {
  const user = useAuthStore((state) => state.user)
  const logout = useAuthStore((state) => state.logout)

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        sx={{ width: `calc(100% - ${drawerWidth}px)`, ml: `${drawerWidth}px` }}
      >
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            Cetaring Admin
          </Typography>
          <Typography variant="body2" sx={{ mr: 2 }}>
            {user?.first_name || user?.username}
          </Typography>
          <Button
            color="inherit"
            startIcon={<LogoutIcon />}
            onClick={logout}
            sx={{ textTransform: 'none' }}
          >
            Logout
          </Button>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: 'border-box' },
        }}
      >
        <Toolbar>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            Admin Console
          </Typography>
        </Toolbar>
        <List>
          {navItems.map((item) => (
            <ListItemButton
              key={item.to}
              selected={currentPath === item.to}
              onClick={() => onNavigate(item.to)}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          ))}
        </List>
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
        <Toolbar />
        <Container maxWidth="xl">{children}</Container>
      </Box>
    </Box>
  )
}
