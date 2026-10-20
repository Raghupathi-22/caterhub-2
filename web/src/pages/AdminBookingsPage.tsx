import {
  Alert,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  MenuItem,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableSortLabel,
  TextField,
  Typography,
} from '@mui/material'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { StatusChip } from '../components/StatusChip'
import type { AdminBookingDetails, AdminOrderSummary, AdminServiceRequestDetails } from '../types/models'

const statuses = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED']
type SortKey = 'eventDate' | 'bookedAt'
type DetailState =
  | { kind: 'CATERING_ORDER'; data: AdminBookingDetails }
  | { kind: 'SERVICE_REQUEST'; data: AdminServiceRequestDetails }
  | null

const formatDate = (value?: string | null) => {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
}

const parseServerDateTime = (value: string) => {
  // Spring LocalDateTime is serialized without an offset. Production Railway runs in UTC,
  // so interpret these server timestamps as UTC before displaying them in the admin's local zone.
  return new Date(value.endsWith('Z') ? value : `${value}Z`)
}

const formatDateTime = (value?: string | null) => {
  if (!value) return '—'
  const date = parseServerDateTime(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const workerLabel = (workerType?: string | null) => (workerType || 'WORKER').replace(/_/g, ' ')

export function AdminBookingsPage() {
  const [rows, setRows] = useState<AdminOrderSummary[]>([])
  const [error, setError] = useState('')
  const [details, setDetails] = useState<DetailState>(null)
  const [detailsLoading, setDetailsLoading] = useState(false)
  const [sortKey, setSortKey] = useState<SortKey>('bookedAt')
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc')

  const load = useCallback(() => {
    setError('')
    adminApi.getAllOrders()
      .then(setRows)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load all bookings and service orders.')))
  }, [])

  useEffect(() => { load() }, [load])

  const sortedRows = useMemo(() => {
    return [...rows].sort((a, b) => {
      const av = parseServerDateTime(a[sortKey]).getTime()
      const bv = parseServerDateTime(b[sortKey]).getTime()
      return (av - bv) * (sortDirection === 'asc' ? 1 : -1)
    })
  }, [rows, sortKey, sortDirection])

  const changeSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortDirection((current) => current === 'asc' ? 'desc' : 'asc')
    } else {
      setSortKey(key)
      setSortDirection('desc')
    }
  }

  const openDetails = async (row: AdminOrderSummary) => {
    try {
      setDetailsLoading(true)
      setError('')
      if (row.orderType === 'CATERING_ORDER') {
        setDetails({ kind: 'CATERING_ORDER', data: await adminApi.getBookingDetails(row.id) })
      } else {
        setDetails({ kind: 'SERVICE_REQUEST', data: await adminApi.getServiceRequestDetails(row.id) })
      }
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to load order details.'))
    } finally {
      setDetailsLoading(false)
    }
  }

  const updateStatus = async (row: AdminOrderSummary, status: string) => {
    if (row.orderType !== 'CATERING_ORDER') return
    try {
      const updated = await adminApi.updateOrderStatus(row.id, status)
      setRows((current) => current.map((item) => item.id === row.id && item.orderType === row.orderType
        ? { ...item, status: updated.status }
        : item))
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to update booking status.'))
    }
  }

  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" alignItems={{ md: 'center' }} spacing={1}>
        <div>
          <Typography variant="h4" sx={{ fontWeight: 800 }}>All Bookings & Orders</Typography>
          <Typography color="text.secondary">Catering orders and every event service request in one place.</Typography>
        </div>
        <Stack direction="row" spacing={1}>
          <Chip label={`${rows.length} total`} variant="outlined" />
          <Button variant="outlined" onClick={load}>Refresh</Button>
        </Stack>
      </Stack>
      {error ? <Alert severity="error">{error}</Alert> : null}

      <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell>Booking ID</TableCell>
              <TableCell>Customer</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Event</TableCell>
              <TableCell>
                <TableSortLabel active={sortKey === 'eventDate'} direction={sortKey === 'eventDate' ? sortDirection : 'desc'} onClick={() => changeSort('eventDate')}>
                  Event Date
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel active={sortKey === 'bookedAt'} direction={sortKey === 'bookedAt' ? sortDirection : 'desc'} onClick={() => changeSort('bookedAt')}>
                  Booked On
                </TableSortLabel>
              </TableCell>
              <TableCell>Area / Location</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Accepted</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Action</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {sortedRows.map((row) => (
              <TableRow key={`${row.orderType}-${row.id}`} hover>
                <TableCell>
                  <Typography sx={{ fontWeight: 800 }}>#{row.id}</Typography>
                  <Typography variant="caption" color="text.secondary">{row.reference}</Typography>
                </TableCell>
                <TableCell sx={{ minWidth: 190 }}>
                  <Typography sx={{ fontWeight: 700 }}>{row.customer?.name || 'Unknown customer'}</Typography>
                  <Typography variant="body2" color="text.secondary">{row.customer?.mobileNumber || 'No mobile'}</Typography>
                </TableCell>
                <TableCell>
                  <Chip size="small" label={row.orderType === 'CATERING_ORDER' ? 'Catering' : (row.serviceType || 'Service')} />
                </TableCell>
                <TableCell>{row.eventType}</TableCell>
                <TableCell>{formatDate(row.eventDate)}</TableCell>
                <TableCell>{formatDateTime(row.bookedAt)}</TableCell>
                <TableCell sx={{ minWidth: 180 }}>{row.area || row.location}</TableCell>
                <TableCell>₹{Number(row.totalAmount || 0).toLocaleString('en-IN')}</TableCell>
                <TableCell>
                  <Chip size="small" color={row.acceptedWorkerCount > 0 ? 'success' : 'default'} label={`${row.acceptedWorkerCount} worker${row.acceptedWorkerCount === 1 ? '' : 's'}`} />
                </TableCell>
                <TableCell><StatusChip status={row.status} /></TableCell>
                <TableCell sx={{ minWidth: 210 }}>
                  <Stack spacing={1}>
                    <Button size="small" variant="outlined" onClick={() => void openDetails(row)} disabled={detailsLoading}>View full details</Button>
                    {row.orderType === 'CATERING_ORDER' ? (
                      <TextField select size="small" fullWidth value={row.status} onChange={(e) => void updateStatus(row, e.target.value)}>
                        {statuses.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
                      </TextField>
                    ) : null}
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
            {sortedRows.length === 0 ? (
              <TableRow><TableCell colSpan={12}><Typography color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>No bookings or service orders found.</Typography></TableCell></TableRow>
            ) : null}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={Boolean(details)} onClose={() => setDetails(null)} fullWidth maxWidth="lg">
        <DialogTitle>
          {details?.kind === 'CATERING_ORDER' ? `Catering Booking #${details.data.id}` : `Service Order #${details?.data.id}`} — Customer & Accepted Workers
        </DialogTitle>
        <DialogContent dividers>
          {details?.kind === 'CATERING_ORDER' ? <CateringDetails details={details.data} /> : null}
          {details?.kind === 'SERVICE_REQUEST' ? <ServiceDetails details={details.data} /> : null}
        </DialogContent>
        <DialogActions><Button onClick={() => setDetails(null)}>Close</Button></DialogActions>
      </Dialog>
    </Stack>
  )
}

function CustomerBlock({ customer }: { customer: { name: string; mobileNumber: string; email?: string | null; username: string; verified?: boolean | null } }) {
  return (
    <Stack spacing={0.4}>
      <Typography variant="h6" sx={{ fontWeight: 800 }}>Customer</Typography>
      <Typography sx={{ fontWeight: 700 }}>{customer.name} • {customer.mobileNumber}</Typography>
      <Typography color="text.secondary">{customer.email || 'No email'} • {customer.username} • {customer.verified ? 'Verified' : 'Not verified'}</Typography>
    </Stack>
  )
}

function AcceptedWorkers({ workers }: { workers: Array<{ name: string; workerType: string; mobileNumber: string; email?: string | null; experienceYears?: number | null; rating?: number | null; skills?: string | null; preferredAreas?: string | null; languages?: string | null; bio?: string | null; acceptedAt?: string | null; assignmentStatus?: string; acceptanceStatus?: string }> }) {
  return (
    <Stack spacing={1.2}>
      <Typography variant="h6" sx={{ fontWeight: 800 }}>Accepted Workers ({workers.length})</Typography>
      {workers.length === 0 ? <Alert severity="info">No worker has accepted this order yet.</Alert> : workers.map((worker, index) => (
        <Paper key={`${worker.mobileNumber}-${index}`} variant="outlined" sx={{ p: 1.8, borderRadius: 2 }}>
          <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" spacing={1}>
            <div>
              <Typography sx={{ fontWeight: 800, textTransform: 'capitalize' }}>{worker.name}</Typography>
              <Typography color="success.main" sx={{ fontWeight: 700 }}>{workerLabel(worker.workerType)}</Typography>
            </div>
            <Chip color="success" size="small" label={worker.assignmentStatus || worker.acceptanceStatus || 'ACCEPTED'} />
          </Stack>
          <Typography sx={{ mt: 0.6 }}>📱 {worker.mobileNumber} • {worker.email || 'No email'}</Typography>
          <Typography color="text.secondary">Experience: {worker.experienceYears ?? 0} years • Rating: {worker.rating ?? 0}</Typography>
          {worker.skills ? <Typography color="text.secondary">Skills: {worker.skills}</Typography> : null}
          {worker.preferredAreas ? <Typography color="text.secondary">Preferred areas: {worker.preferredAreas}</Typography> : null}
          {worker.languages ? <Typography color="text.secondary">Languages: {worker.languages}</Typography> : null}
          {worker.bio ? <Typography color="text.secondary">Bio: {worker.bio}</Typography> : null}
          {worker.acceptedAt ? <Typography variant="caption" color="text.secondary">Accepted: {formatDateTime(worker.acceptedAt)}</Typography> : null}
        </Paper>
      ))}
    </Stack>
  )
}

function CateringDetails({ details }: { details: AdminBookingDetails }) {
  return (
    <Stack spacing={2}>
      <CustomerBlock customer={details.customer} />
      <Divider />
      <Stack spacing={0.5}>
        <Typography variant="h6" sx={{ fontWeight: 800 }}>Booking</Typography>
        <Typography>{details.bookingReference || `#${details.id}`} • {details.eventType} • {details.guestCount} guests</Typography>
        <Typography color="text.secondary">Event: {formatDate(details.eventDate || details.eventDateTime)}</Typography>
        <Typography color="text.secondary">Booked on: {formatDateTime(details.createdAt)}</Typography>
        <Typography color="text.secondary">Location: {details.deliveryAddress}</Typography>
        <Typography sx={{ fontWeight: 800 }}>₹{Number(details.totalAmount).toLocaleString('en-IN')} • {details.status}</Typography>
      </Stack>
      <Divider />
      <AcceptedWorkers workers={details.acceptedWorkers} />
    </Stack>
  )
}

function ServiceDetails({ details }: { details: AdminServiceRequestDetails }) {
  return (
    <Stack spacing={2}>
      <CustomerBlock customer={details.customer} />
      <Divider />
      <Stack spacing={0.5}>
        <Typography variant="h6" sx={{ fontWeight: 800 }}>Service Order</Typography>
        <Typography>SR-{details.id} • {details.serviceType} • {details.eventType}</Typography>
        <Typography color="text.secondary">Event: {formatDate(details.eventDate)} • {details.startTime} - {details.endTime}</Typography>
        <Typography color="text.secondary">Booked on: {formatDateTime(details.createdAt)}</Typography>
        <Typography color="text.secondary">Location: {details.location} • {details.area}</Typography>
        <Typography color="text.secondary">Services: {details.selectedServices?.join(', ') || '—'}</Typography>
        <Typography sx={{ fontWeight: 800 }}>₹{Number(details.totalAmount).toLocaleString('en-IN')} • {details.status}</Typography>
      </Stack>
      <Divider />
      <AcceptedWorkers workers={details.acceptedWorkers} />
    </Stack>
  )
}
