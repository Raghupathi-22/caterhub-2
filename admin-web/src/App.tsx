import { useCallback, useEffect, useMemo, useState } from 'react'
import { AdminLayout } from './components/AdminLayout'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { OrdersPage } from './pages/OrdersPage'
import { OffersPage } from './pages/OffersPage'
import { EventsPage } from './pages/EventsPage'
import { ServiceRequestsPage } from './pages/ServiceRequestsPage'
import { useAuthStore } from './store/authStore'

function normalizePath(pathname: string) {
  if (pathname === '/orders' || pathname === '/offers' || pathname === '/events' || pathname === '/service-requests' || pathname === '/login') {
    return pathname
  }
  return '/'
}

function App() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const [path, setPath] = useState(() => normalizePath(window.location.pathname))

  const navigate = useCallback((to: string, replace = false) => {
    const nextPath = normalizePath(to)
    if (replace) {
      window.history.replaceState(null, '', nextPath)
    } else {
      window.history.pushState(null, '', nextPath)
    }
    setPath(nextPath)
  }, [])

  useEffect(() => {
    const onPopState = () => setPath(normalizePath(window.location.pathname))
    window.addEventListener('popstate', onPopState)
    return () => window.removeEventListener('popstate', onPopState)
  }, [])

  useEffect(() => {
    if (!isAuthenticated && path !== '/login') {
      navigate('/login', true)
      return
    }

    if (isAuthenticated && path === '/login') {
      navigate('/', true)
    }
  }, [isAuthenticated, navigate, path])

  const page = useMemo(() => {
    switch (path) {
      case '/orders':
        return <OrdersPage />
      case '/offers':
        return <OffersPage />
      case '/events':
        return <EventsPage />
      case '/service-requests':
        return <ServiceRequestsPage />
      case '/login':
        return <LoginPage onLoginSuccess={() => navigate('/', true)} />
      default:
        return <DashboardPage />
    }
  }, [navigate, path])

  if (!isAuthenticated || path === '/login') {
    return page
  }

  return (
    <AdminLayout currentPath={path} onNavigate={navigate}>
      {page}
    </AdminLayout>
  )
}

export default App
