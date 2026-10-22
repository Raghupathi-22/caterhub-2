import {
  Alert,
  Button,
  Chip,
  CircularProgress,
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
import type {
  AdminAcceptedWorker,
  AdminBookingDetails,
  AdminOrderSummary,
  AdminServiceRequestDetails,
  AdminStaffingJobDetails,
} from '../types/models'
import { formatPhoneNumber, telHref } from '../utils/adminContact'
import {
  acceptanceLabel,
  businessNowKey,
  filterOrders,
  orderCategory,
  orderTypeLabel,
  type OrderFilters,
  type OrderTiming,
} from '../utils/adminOrders'

const statuses = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED']
type SortKey = 'eventDate' | 'bookedAt'
type DetailState =
  | { kind: 'CATERING_ORDER'; data: AdminBookingDetails }
  | { kind: 'SERVICE_REQUEST'; data: AdminServiceRequestDetails }
  | { kind: 'STAFFING_JOB'; data: AdminStaffingJobDetails }

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

// Event times are local (IST) wall-clock values; show them as stored.
const formatTime = (value?: string | null) => (value ? value.slice(0, 5) : '')

const workerLabel = (workerType?: string | null) => (workerType || 'WORKER').replace(/_/g, ' ')

export function AdminBookingsPage() {
  const [rows, setRows] = useState<AdminOrderSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  const [selected, setSelected] = useState<AdminOrderSummary | null>(null)
  const [details, setDetails] = useState<DetailState | null>(null)
  const [detailsLoading, setDetailsLoading] = useState(false)
  const [detailsError, setDetailsError] = useState('')
  const [sortKey, setSortKey] = useState<SortKey>('bookedAt')
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc')
  const [filters, setFilters] = useState<OrderFilters>({ timing: 'ALL' })

  const load = useCallback(() => {
    setLoading(true)
    setError('')
    adminApi.getAllOrders()
      .then(setRows)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load all bookings and service orders.')))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])

  const categories = useMemo(() => Array.from(new Set(rows.map(orderCategory))).sort(), [rows])
  const statusOptions = useMemo(() => Array.from(new Set(rows.map((row) => row.status).filter(Boolean))).sort(), [rows])

  const visibleRows = useMemo(() => {
    const filtered = filterOrders(rows, filters, businessNowKey())
    return filtered.sort((a, b) => {
      const av = parseServerDateTime(a[sortKey]).getTime()
      const bv = parseServerDateTime(b[sortKey]).getTime()
      return (av - bv) * (sortDirection === 'asc' ? 1 : -1)
    })
  }, [rows, filters, sortKey, sortDirection])

  const updateFilter = <K extends keyof OrderFilters>(key: K, value: OrderFilters[K]) =>
    setFilters((current) => ({ ...current, [key]: value || undefined }))

  const changeSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortDirection((current) => current === 'asc' ? 'desc' : 'asc')
    } else {
      setSortKey(key)
      setSortDirection('desc')
    }
  }

  const loadDetails = useCallback(async (row: AdminOrderSummary) => {
    setDetailsLoading(true)
    setDetailsError('')
    setDetails(null)
    try {
      if (row.orderType === 'CATERING_ORDER') {
        setDetails({ kind: 'CATERING_ORDER', data: await adminApi.getBookingDetails(row.id) })
      } else if (row.orderType === 'STAFFING_JOB') {
        setDetails({ kind: 'STAFFING_JOB', data: await adminApi.getStaffingJobDetails(row.id) })
      } else {
        setDetails({ kind: 'SERVICE_REQUEST', data: await adminApi.getServiceRequestDetails(row.id) })
      }
    } catch (e: unknown) {
      setDetailsError(apiErrorMessage(e, 'Unable to load order details.'))
    } finally {
      setDetailsLoading(false)
    }
  }, [])

  const openDetails = (row: AdminOrderSummary) => {
    setSelected(row)
    void loadDetails(row)
  }

  const closeDetails = () => {
    setSelected(null)
    setDetails(null)
    setDetailsError('')
  }

  const updateStatus = async (row: AdminOrderSummary, status: string) => {
    if (row.orderType !== 'CATERING_ORDER') return
    try {
      setActionError('')
      const updated = await adminApi.updateOrderStatus(row.id, status)
      setRows((current) => current.map((item) => item.id === row.id && item.orderType === row.orderType
        ? { ...item, status: updated.status }
        : item))
    } catch (e: unknown) {
      setActionError(apiErrorMessage(e, 'Unable to update booking status.'))
    }
  }

  const hasFilters = Boolean(filters.search || filters.status || filters.category || filters.fromDate || filters.toDate || (filters.timing && filters.timing !== 'ALL'))

  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" alignItems={{ md: 'center' }} spacing={1}>
        <div>
          <Typography variant="h4" sx={{ fontWeight: 800 }}>All Bookings & Orders</Typography>
          <Typography color="text.secondary">Catering orders, service requests and worker jobs — past and upcoming — with every accepted worker.</Typography>
        </div>
        <Stack direction="row" spacing={1}>
          <Chip label={hasFilters ? `${visibleRows.length} of ${rows.length}` : `${rows.length} total`} variant="outlined" />
          <Button variant="outlined" onClick={load} disabled={loading}>Refresh</Button>
        </Stack>
      </Stack>

      <Paper variant="outlined" sx={{ p: 1.5 }}>
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.2} useFlexGap flexWrap="wrap">
          <TextField size="small" label="Search ID, reference, customer or phone" value={filters.search ?? ''}
            onChange={(e) => updateFilter('search', e.target.value)} sx={{ minWidth: 260, flex: 1 }} />
          <TextField select size="small" label="Status" value={filters.status ?? ''} onChange={(e) => updateFilter('status', e.target.value)} sx={{ minWidth: 150 }}>
            <MenuItem value="">All statuses</MenuItem>
            {statusOptions.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
          </TextField>
          <TextField select size="small" label="Service category" value={filters.category ?? ''} onChange={(e) => updateFilter('category', e.target.value)} sx={{ minWidth: 180 }}>
            <MenuItem value="">All categories</MenuItem>
            {categories.map((category) => <MenuItem key={category} value={category}>{category}</MenuItem>)}
          </TextField>
          <TextField size="small" type="date" label="Event from" value={filters.fromDate ?? ''} onChange={(e) => updateFilter('fromDate', e.target.value)} InputLabelProps={{ shrink: true }} />
          <TextField size="small" type="date" label="Event to" value={filters.toDate ?? ''} onChange={(e) => updateFilter('toDate', e.target.value)} InputLabelProps={{ shrink: true }} />
          <TextField select size="small" label="Event timing" value={filters.timing ?? 'ALL'} onChange={(e) => updateFilter('timing', e.target.value as OrderTiming)} sx={{ minWidth: 150 }}>
            <MenuItem value="ALL">Past & upcoming</MenuItem>
            <MenuItem value="UPCOMING">Upcoming only</MenuItem>
            <MenuItem value="PAST">Past / started</MenuItem>
          </TextField>
          {hasFilters ? <Button onClick={() => setFilters({ timing: 'ALL' })}>Clear</Button> : null}
        </Stack>
      </Paper>

      {actionError ? <Alert severity="error" onClose={() => setActionError('')}>{actionError}</Alert> : null}
      {error ? (
        <Alert severity="error" action={<Button color="inherit" size="small" onClick={load}>Retry</Button>}>{error}</Alert>
      ) : null}

      <TableContainer component={Paper} sx={{ overflowX: 'auto' }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell>Booking ID</TableCell>
              <TableCell>Customer</TableCell>
              <TableCell>Category / Role</TableCell>
              <TableCell>Event</TableCell>
              <TableCell>
                <TableSortLabel active={sortKey === 'eventDate'} direction={sortKey === 'eventDate' ? sortDirection : 'desc'} onClick={() => changeSort('eventDate')}>
                  Event Date & Start
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
            {loading && rows.length === 0 ? (
              <TableRow><TableCell colSpan={11}><Stack alignItems="center" sx={{ py: 4 }}><CircularProgress size={28} /></Stack></TableCell></TableRow>
            ) : null}
            {visibleRows.map((row) => (
              <TableRow key={`${row.orderType}-${row.id}`} hover>
                <TableCell>
                  <Typography sx={{ fontWeight: 800 }}>#{row.id}</Typography>
                  <Typography variant="caption" color="text.secondary" display="block">{row.reference}</Typography>
                  <Typography variant="caption" color="text.secondary">{orderTypeLabel(row.orderType)}</Typography>
                </TableCell>
                <TableCell sx={{ minWidth: 190 }}>
                  <Typography sx={{ fontWeight: 700 }}>{row.customer?.name || 'Unknown customer'}</Typography>
                  <Typography variant="body2" color="text.secondary">{row.customer?.mobileNumber ? formatPhoneNumber(row.customer.mobileNumber) : 'No mobile'}</Typography>
                </TableCell>
                <TableCell sx={{ minWidth: 170 }}>
                  <Chip size="small" label={orderCategory(row)} />
                  {row.requestedRoles?.length ? (
                    <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 0.5 }}>
                      {row.requestedRoles.map(workerLabel).join(', ')}
                    </Typography>
                  ) : null}
                </TableCell>
                <TableCell>{row.eventType}</TableCell>
                <TableCell sx={{ minWidth: 130 }}>
                  <Typography variant="body2" sx={{ fontWeight: 700 }}>{formatDate(row.eventDate)}</Typography>
                  {row.startTime ? <Typography variant="caption" color="text.secondary">Starts {formatTime(row.startTime)}{row.endTime ? ` – ${formatTime(row.endTime)}` : ''}</Typography> : null}
                </TableCell>
                <TableCell>{formatDateTime(row.bookedAt)}</TableCell>
                <TableCell sx={{ minWidth: 180 }}>{[row.area, row.location].filter(Boolean).join(' • ') || '—'}</TableCell>
                <TableCell>₹{Number(row.totalAmount || 0).toLocaleString('en-IN')}</TableCell>
                <TableCell sx={{ minWidth: 150 }}>
                  <Chip size="small" color={row.acceptedWorkerCount > 0 ? 'success' : 'default'} label={acceptanceLabel(row.acceptedWorkerCount, row.requestedWorkerCount)} />
                </TableCell>
                <TableCell><StatusChip status={row.status} /></TableCell>
                <TableCell sx={{ minWidth: 210 }}>
                  <Stack spacing={1}>
                    <Button size="small" variant="outlined" onClick={() => openDetails(row)}>View accepted workers</Button>
                    {row.orderType === 'CATERING_ORDER' ? (
                      <TextField select size="small" fullWidth value={row.status} onChange={(e) => void updateStatus(row, e.target.value)}>
                        {statuses.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
                      </TextField>
                    ) : null}
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
            {!loading && !error && visibleRows.length === 0 ? (
              <TableRow>
                <TableCell colSpan={11}>
                  <Typography color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
                    {rows.length === 0 ? 'No bookings or service orders found.' : 'No bookings match the current filters.'}
                  </Typography>
                </TableCell>
              </TableRow>
            ) : null}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={Boolean(selected)} onClose={closeDetails} fullWidth maxWidth="lg">
        <DialogTitle>
          {selected ? `${orderTypeLabel(selected.orderType)} #${selected.id}` : ''} — Customer & Accepted Workers
        </DialogTitle>
        <DialogContent dividers>
          {detailsLoading ? <Stack alignItems="center" sx={{ py: 4 }}><CircularProgress size={28} /></Stack> : null}
          {detailsError ? (
            <Alert severity="error" action={selected ? <Button color="inherit" size="small" onClick={() => void loadDetails(selected)}>Retry</Button> : undefined}>
              {detailsError}
            </Alert>
          ) : null}
          {details?.kind === 'CATERING_ORDER' ? <CateringDetails details={details.data} /> : null}
          {details?.kind === 'SERVICE_REQUEST' ? <ServiceDetails details={details.data} /> : null}
          {details?.kind === 'STAFFING_JOB' ? <StaffingJobDetails details={details.data} /> : null}
        </DialogContent>
        <DialogActions><Button onClick={closeDetails}>Close</Button></DialogActions>
      </Dialog>
    </Stack>
  )
}

function CustomerBlock({ customer }: { customer?: { name: string; mobileNumber: string; email?: string | null; username: string; verified?: boolean | null } | null }) {
  if (!customer) {
    return <Alert severity="info">No customer account is linked to this record.</Alert>
  }
  return (
    <Stack spacing={0.4}>
      <Typography variant="h6" sx={{ fontWeight: 800 }}>Customer</Typography>
      <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
        <Typography sx={{ fontWeight: 700 }}>{customer.name}</Typography>
        <PhoneActions phone={customer.mobileNumber} />
      </Stack>
      <Typography color="text.secondary">{customer.email || 'No email'} • {customer.username} • {customer.verified ? 'Verified' : 'Not verified'}</Typography>
    </Stack>
  )
}

function PhoneActions({ phone }: { phone?: string | null }) {
  const [copied, setCopied] = useState(false)
  if (!phone) return <Typography color="text.secondary">No mobile number</Typography>
  const href = telHref(phone)
  const copy = async () => {
    try {
      await navigator.clipboard.writeText(phone)
      setCopied(true)
      window.setTimeout(() => setCopied(false), 1500)
    } catch {
      setCopied(false)
    }
  }
  return (
    <Stack direction="row" spacing={0.5} alignItems="center">
      <Typography sx={{ fontWeight: 700 }}>📱 {formatPhoneNumber(phone)}</Typography>
      <Button size="small" onClick={() => void copy()}>{copied ? 'Copied' : 'Copy'}</Button>
      {href ? <Button size="small" href={href}>Call</Button> : null}
    </Stack>
  )
}

function CountSummary({ accepted, requested }: { accepted?: number | null; requested?: number | null }) {
  return <Chip color={(accepted ?? 0) > 0 ? 'success' : 'default'} label={acceptanceLabel(accepted, requested)} sx={{ fontWeight: 700 }} />
}

function AcceptedWorkers({ workers, accepted, requested }: { workers: AdminAcceptedWorker[]; accepted?: number | null; requested?: number | null }) {
  return (
    <Stack spacing={1.2}>
      <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
        <Typography variant="h6" sx={{ fontWeight: 800 }}>Accepted Workers ({workers.length})</Typography>
        <CountSummary accepted={accepted ?? workers.length} requested={requested} />
      </Stack>
      {workers.length === 0 ? <Alert severity="info">No worker has accepted this order yet.</Alert> : workers.map((worker, index) => (
        <Paper key={`${worker.workerProfileId}-${worker.acceptanceId ?? worker.assignmentId ?? index}`} variant="outlined" sx={{ p: 1.8, borderRadius: 2 }}>
          <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" spacing={1}>
            <div>
              <Typography sx={{ fontWeight: 800, textTransform: 'capitalize' }}>{worker.name}</Typography>
              <Typography color="success.main" sx={{ fontWeight: 700 }}>
                {worker.serviceCategory ? `${worker.serviceCategory} • ` : ''}{workerLabel(worker.workerType)}
              </Typography>
              {worker.requestedWorkerType && worker.requestedWorkerType !== worker.workerType ? (
                <Typography variant="caption" color="text.secondary">Accepted as: {workerLabel(worker.requestedWorkerType)}</Typography>
              ) : null}
            </div>
            <Stack direction="row" spacing={1} alignItems="flex-start">
              <Chip size="small" variant="outlined" label={`Verification: ${worker.profileStatus?.replace(/_/g, ' ') || 'UNKNOWN'}`}
                color={worker.profileStatus === 'ACTIVE' ? 'success' : worker.profileStatus === 'REJECTED' || worker.profileStatus === 'SUSPENDED' ? 'error' : 'warning'} />
              <Chip color="success" size="small" label={worker.assignmentStatus || worker.acceptanceStatus || 'ACCEPTED'} />
            </Stack>
          </Stack>
          <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" sx={{ mt: 0.6 }}>
            <PhoneActions phone={worker.mobileNumber} />
            <Typography color="text.secondary">• {worker.email || 'No email'}</Typography>
          </Stack>
          <Typography color="text.secondary">Experience: {worker.experienceYears ?? 0} years • Rating: {worker.rating ?? 0}</Typography>
          {worker.skills ? <Typography color="text.secondary">Skills: {worker.skills}</Typography> : null}
          {worker.preferredAreas ? <Typography color="text.secondary">Preferred areas: {worker.preferredAreas}</Typography> : null}
          {worker.languages ? <Typography color="text.secondary">Languages: {worker.languages}</Typography> : null}
          {worker.bio ? <Typography color="text.secondary">Bio: {worker.bio}</Typography> : null}
          <Typography variant="caption" color="text.secondary">Accepted: {worker.acceptedAt ? formatDateTime(worker.acceptedAt) : 'Not recorded'}</Typography>
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
        <Typography color="text.secondary">Event date: {formatDate(details.eventDate || details.eventDateTime)}</Typography>
        <Typography color="text.secondary">Booked on: {formatDateTime(details.createdAt)}</Typography>
        <Typography color="text.secondary">Location: {details.deliveryAddress}</Typography>
        <Typography sx={{ fontWeight: 800 }}>₹{Number(details.totalAmount).toLocaleString('en-IN')} • {details.status}</Typography>
      </Stack>
      <Divider />
      <AcceptedWorkers workers={details.acceptedWorkers} accepted={details.acceptedWorkerCount} />
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
        <Typography color="text.secondary">Event: {formatDate(details.eventDate)} • starts {formatTime(details.startTime)}{details.endTime ? ` – ${formatTime(details.endTime)}` : ''}</Typography>
        <Typography color="text.secondary">Booked on: {formatDateTime(details.createdAt)}</Typography>
        <Typography color="text.secondary">Location: {details.location} • {details.area}</Typography>
        <Typography color="text.secondary">Services: {details.selectedServices?.join(', ') || '—'}</Typography>
        <Typography sx={{ fontWeight: 800 }}>₹{Number(details.totalAmount).toLocaleString('en-IN')} • {details.status}</Typography>
      </Stack>
      {details.staffingJobs?.length ? (
        <>
          <Divider />
          <Typography variant="h6" sx={{ fontWeight: 800 }}>Requested Workers</Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Job</TableCell>
                  <TableCell>Role</TableCell>
                  <TableCell>Accepted / Requested</TableCell>
                  <TableCell>Pay / worker</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {details.staffingJobs.map((job) => (
                  <TableRow key={job.id}>
                    <TableCell>JOB-{job.id}</TableCell>
                    <TableCell>{workerLabel(job.workerType)}</TableCell>
                    <TableCell>{acceptanceLabel(job.acceptedWorkers, job.requiredWorkers)}</TableCell>
                    <TableCell>₹{Number(job.paymentPerWorker || 0).toLocaleString('en-IN')}</TableCell>
                    <TableCell><StatusChip status={job.status} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      ) : null}
      <Divider />
      <AcceptedWorkers workers={details.acceptedWorkers} accepted={details.acceptedWorkerCount} requested={details.requestedWorkerCount} />
    </Stack>
  )
}

function StaffingJobDetails({ details }: { details: AdminStaffingJobDetails }) {
  return (
    <Stack spacing={2}>
      <CustomerBlock customer={details.customer} />
      <Divider />
      <Stack spacing={0.5}>
        <Typography variant="h6" sx={{ fontWeight: 800 }}>Worker Job</Typography>
        <Typography>JOB-{details.id} • {details.serviceCategory || 'Service'} • {workerLabel(details.workerType)} • {details.eventType}</Typography>
        <Typography color="text.secondary">Event: {formatDate(details.eventDate)} • starts {formatTime(details.startTime)}{details.endTime ? ` – ${formatTime(details.endTime)}` : ''}</Typography>
        <Typography color="text.secondary">Created on: {formatDateTime(details.createdAt)}</Typography>
        <Typography color="text.secondary">Location: {[details.location, details.area].filter(Boolean).join(' • ') || '—'}</Typography>
        {details.serviceRequestId ? <Typography color="text.secondary">Linked service request: SR-{details.serviceRequestId}</Typography> : null}
        {details.additionalRequirements ? <Typography color="text.secondary">Requirements: {details.additionalRequirements}</Typography> : null}
        <Typography sx={{ fontWeight: 800 }}>₹{Number(details.paymentPerWorker || 0).toLocaleString('en-IN')} per worker • {details.status}</Typography>
      </Stack>
      <Divider />
      <AcceptedWorkers workers={details.acceptedWorkers} accepted={details.acceptedWorkerCount} requested={details.requiredWorkers} />
    </Stack>
  )
}
