import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { PublicLayout } from './components/PublicLayout'
import { RequireAdmin } from './components/RequireAdmin'
import { RequireAuth } from './components/RequireAuth'
import { AdminLayout } from './components/AdminLayout'
import { PublicHomePage } from './pages/PublicHomePage'
import { ServicesPage } from './pages/ServicesPage'
import { ServiceCategoryPage } from './pages/ServiceCategoryPage'
import { OffersPublicPage, AboutPage, ContactPage } from './pages/StaticPages'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { CustomerHomePage } from './pages/CustomerHomePage'
import { BookingPage } from './pages/BookingPage'
import { MyBookingsPage } from './pages/MyBookingsPage'
import { JoinCaterhubPage } from './pages/JoinCaterhubPage'
import { WorkerDashboardPage } from './pages/WorkerDashboardPage'
import { WorkerJobsPage } from './pages/WorkerJobsPage'
import { WorkerAvailabilityPage } from './pages/WorkerAvailabilityPage'
import { WorkerProfilePage } from './pages/WorkerProfilePage'
import { AdminLoginPage } from './pages/AdminLoginPage'
import { AdminDashboardPage } from './pages/AdminDashboardPage'
import { AdminBookingsPage } from './pages/AdminBookingsPage'
import { AdminOffersPage } from './pages/AdminOffersPage'
import { AdminEventsPage } from './pages/AdminEventsPage'
import { AdminSupportPage } from './pages/AdminSupportPage'
import { AdminWorkersPage } from './pages/AdminWorkersPage'
import { AdminWorkerDetailPage } from './pages/AdminWorkerDetailPage'
import { AdminVerificationPage } from './pages/AdminVerificationPage'
import { AdminCategoriesPage, AdminCustomersPage, AdminReportsPage, AdminServicesPage, AdminSettingsPage, ProfilePage } from './pages/AdminSimplePages'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 1000 * 60 * 3, gcTime: 1000 * 60 * 10 },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<PublicLayout />}>
            <Route path="/" element={<PublicHomePage />} />
            <Route path="/services" element={<ServicesPage />} />
            <Route path="/services/:categoryId" element={<ServiceCategoryPage />} />
            <Route path="/offers" element={<OffersPublicPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/contact" element={<ContactPage />} />
            <Route path="/join-caterhub" element={<JoinCaterhubPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/get-started" element={<RegisterPage />} />
            <Route path="/home" element={<RequireAuth><CustomerHomePage /></RequireAuth>} />
            <Route path="/booking/:categoryId" element={<RequireAuth><BookingPage /></RequireAuth>} />
            <Route path="/my-bookings" element={<RequireAuth><MyBookingsPage /></RequireAuth>} />
            <Route path="/my-bookings/:id" element={<RequireAuth><MyBookingsPage /></RequireAuth>} />
            <Route path="/profile" element={<RequireAuth><ProfilePage /></RequireAuth>} />
            <Route path="/worker" element={<RequireAuth><WorkerDashboardPage /></RequireAuth>} />
            <Route path="/worker/profile" element={<RequireAuth><WorkerProfilePage /></RequireAuth>} />
            <Route path="/worker/jobs" element={<RequireAuth><WorkerJobsPage /></RequireAuth>} />
            <Route path="/worker/availability" element={<RequireAuth><WorkerAvailabilityPage /></RequireAuth>} />
          </Route>

          <Route path="/admin/login" element={<AdminLoginPage />} />
          <Route path="/admin" element={<RequireAdmin><AdminLayout /></RequireAdmin>}>
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route path="dashboard" element={<AdminDashboardPage />} />
            <Route path="bookings" element={<AdminBookingsPage />} />
            <Route path="orders" element={<Navigate to="/admin/bookings" replace />} />
            <Route path="bookings/:id" element={<AdminBookingsPage />} />
            <Route path="customers" element={<AdminCustomersPage />} />
            <Route path="workers" element={<AdminWorkersPage />} />
            <Route path="workers/:id" element={<AdminWorkerDetailPage />} />
            <Route path="verification" element={<AdminVerificationPage />} />
            <Route path="services" element={<AdminServicesPage />} />
            <Route path="categories" element={<AdminCategoriesPage />} />
            <Route path="offers" element={<AdminOffersPage />} />
            <Route path="support" element={<AdminSupportPage />} />
            <Route path="service-requests" element={<AdminSupportPage />} />
            <Route path="reports" element={<AdminReportsPage />} />
            <Route path="settings" element={<AdminSettingsPage />} />
            <Route path="events" element={<AdminEventsPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
