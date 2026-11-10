import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Grid,
  MenuItem,
  Paper,
  Stack,
  Switch,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  TextField,
  Typography,
} from '@mui/material'
import { useCallback, useEffect, useState, type FormEvent, type ReactNode } from 'react'
import { riceAdminApi, type RiceOrderFilters, type RiceSampleFilters } from '../api/riceApi'
import { apiErrorMessage } from '../api/http'
import type { RiceOrder, RiceOrderStatus, RicePaymentStatus, RiceProductUpdate, RiceSample, RiceSampleStatus } from '../types/models'
import {
  RICE_ORDER_STATUSES,
  RICE_PAYMENT_STATUSES,
  RICE_SAMPLE_STATUSES,
  allowedPaymentStatuses,
  allowedSampleStatuses,
  deliverySummary,
  formatDateTime,
  formatInr,
  formatQuantity,
  formatSampleWeight,
  humanize,
  isOrderLocked,
  isSampleLocked,
  productToForm,
  toDateTimeInput,
  toLocalDateTime,
  validateProductForm,
  validateSampleSchedule,
} from '../utils/riceOrders'

const statusColor: Record<string, 'default' | 'success' | 'warning' | 'error' | 'info'> = {
  PENDING: 'warning',
  REQUESTED: 'warning',
  CONFIRMED: 'info',
  PROCESSING: 'info',
  OUT_FOR_DELIVERY: 'info',
  PICKUP_SCHEDULED: 'info',
  DELIVERED: 'success',
  COLLECTED: 'success',
  PAID: 'success',
  CANCELLED: 'error',
  FAILED: 'error',
  REFUNDED: 'default',
}

function RiceChip({ value }: { value: string }) {
  return <Chip size="small" color={statusColor[value] ?? 'default'} label={humanize(value)} />
}

function StateBlock({ loading, error, empty, emptyText, onRetry, children }: {
  loading: boolean
  error: string
  empty: boolean
  emptyText: string
  onRetry: () => void
  children: ReactNode
}) {
  if (loading) {
    return <Box sx={{ py: 6, display: 'flex', justifyContent: 'center' }}><CircularProgress /></Box>
  }
  if (error) {
    return <Alert severity="error" action={<Button color="inherit" size="small" onClick={onRetry}>Retry</Button>}>{error}</Alert>
  }
  if (empty) {
    return <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}><Typography color="text.secondary">{emptyText}</Typography></Paper>
  }
  return <>{children}</>
}

function DateRangeFilters({ from, to, onChange }: { from?: string; to?: string; onChange: (from: string, to: string) => void }) {
  return (
    <>
      <TextField size="small" type="date" label="From" InputLabelProps={{ shrink: true }} value={from ?? ''} onChange={(e) => onChange(e.target.value, to ?? '')} />
      <TextField size="small" type="date" label="To" InputLabelProps={{ shrink: true }} value={to ?? ''} onChange={(e) => onChange(from ?? '', e.target.value)} />
    </>
  )
}

function SearchBox({ onSearch }: { onSearch: (value: string) => void }) {
  const [value, setValue] = useState('')
  const submit = (event: FormEvent) => {
    event.preventDefault()
    onSearch(value.trim())
  }
  return (
    <Box component="form" onSubmit={submit} sx={{ display: 'flex', gap: 1 }}>
      <TextField size="small" label="Search name, phone or number" value={value} onChange={(e) => setValue(e.target.value)} sx={{ minWidth: 240 }} />
      <Button type="submit" variant="outlined">Search</Button>
    </Box>
  )
}

// ------------------------------------------------------------------ bulk orders

function OrderStatusDialog({ order, onClose, onSaved }: { order: RiceOrder | null; onClose: () => void; onSaved: (order: RiceOrder) => void }) {
  const [status, setStatus] = useState<RiceOrderStatus>('PENDING')
  const [paymentStatus, setPaymentStatus] = useState<RicePaymentStatus>('PENDING')
  const [adminNotes, setAdminNotes] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!order) return
    setStatus(order.status)
    setPaymentStatus(order.paymentStatus)
    setAdminNotes(order.adminNotes ?? '')
    setError('')
  }, [order])

  const save = async () => {
    if (!order || saving) return
    setSaving(true)
    setError('')
    try {
      onSaved(await riceAdminApi.updateOrder(order.id, { status, paymentStatus, adminNotes }))
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to update the rice order.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open={Boolean(order)} onClose={saving ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>Update {order?.orderNumber}</DialogTitle>
      <DialogContent dividers>
        {order ? (
          <Stack spacing={2}>
            <Typography>
              <b>{order.customerName}</b> • {order.mobileNumber}
            </Typography>
            <Typography color="text.secondary">
              {formatQuantity(order.quantityKg)} • Subtotal {formatInr(order.subtotal)} • Delivery {order.deliveryCharge == null ? 'to be confirmed' : formatInr(order.deliveryCharge)} • <b>Total {formatInr(order.totalAmount)}</b>
            </Typography>
            <Typography variant="body2">{deliverySummary(order)}</Typography>
            {order.deliveryNotes ? <Typography variant="body2">Notes: {order.deliveryNotes}</Typography> : null}
            {isOrderLocked(order) ? <Alert severity="info">This order is {humanize(order.status).toLowerCase()}; only payment status and notes can be changed.</Alert> : null}
            <TextField select label="Order status" value={status} onChange={(e) => setStatus(e.target.value as RiceOrderStatus)} disabled={isOrderLocked(order)}>
              {RICE_ORDER_STATUSES.map((s) => <MenuItem key={s} value={s}>{humanize(s)}</MenuItem>)}
            </TextField>
            <TextField select label="Payment status" value={paymentStatus} onChange={(e) => setPaymentStatus(e.target.value as RicePaymentStatus)}>
              {allowedPaymentStatuses(order.paymentStatus).map((s) => <MenuItem key={s} value={s}>{humanize(s)}</MenuItem>)}
            </TextField>
            <TextField label="Admin notes (internal)" multiline minRows={2} value={adminNotes} onChange={(e) => setAdminNotes(e.target.value)} inputProps={{ maxLength: 2000 }} />
            {error ? <Alert severity="error">{error}</Alert> : null}
          </Stack>
        ) : null}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={saving}>Cancel</Button>
        <Button variant="contained" onClick={() => void save()} disabled={saving}>{saving ? 'Saving…' : 'Save'}</Button>
      </DialogActions>
    </Dialog>
  )
}

function BulkOrdersTab() {
  const [filters, setFilters] = useState<RiceOrderFilters>({})
  const [orders, setOrders] = useState<RiceOrder[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState<RiceOrder | null>(null)
  const [notice, setNotice] = useState('')

  const load = useCallback(() => {
    setLoading(true)
    setError('')
    riceAdminApi.getOrders(filters)
      .then(setOrders)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load rice orders.')))
      .finally(() => setLoading(false))
  }, [filters])

  useEffect(() => { load() }, [load])

  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={1} flexWrap="wrap" useFlexGap>
        <TextField select size="small" label="Status" value={filters.status ?? ''} onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value as RiceOrderStatus | '' }))} sx={{ minWidth: 170 }}>
          <MenuItem value="">All statuses</MenuItem>
          {RICE_ORDER_STATUSES.map((s) => <MenuItem key={s} value={s}>{humanize(s)}</MenuItem>)}
        </TextField>
        <TextField select size="small" label="Payment" value={filters.paymentStatus ?? ''} onChange={(e) => setFilters((f) => ({ ...f, paymentStatus: e.target.value as RicePaymentStatus | '' }))} sx={{ minWidth: 150 }}>
          <MenuItem value="">All payments</MenuItem>
          {RICE_PAYMENT_STATUSES.map((s) => <MenuItem key={s} value={s}>{humanize(s)}</MenuItem>)}
        </TextField>
        <DateRangeFilters from={filters.from} to={filters.to} onChange={(from, to) => setFilters((f) => ({ ...f, from, to }))} />
        <SearchBox onSearch={(search) => setFilters((f) => ({ ...f, search }))} />
        <Button onClick={load}>Refresh</Button>
      </Stack>
      {notice ? <Alert severity="success" onClose={() => setNotice('')}>{notice}</Alert> : null}
      <StateBlock loading={loading} error={error} empty={orders.length === 0} emptyText="No bulk rice orders match these filters." onRetry={load}>
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Order</TableCell>
                <TableCell>Customer</TableCell>
                <TableCell align="right">Quantity</TableCell>
                <TableCell align="right">Total</TableCell>
                <TableCell>Delivery</TableCell>
                <TableCell>Created</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Payment</TableCell>
                <TableCell />
              </TableRow>
            </TableHead>
            <TableBody>
              {orders.map((order) => (
                <TableRow key={order.id} hover>
                  <TableCell sx={{ fontWeight: 700, whiteSpace: 'nowrap' }}>{order.orderNumber}</TableCell>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>{order.customerName}</Typography>
                    <Typography variant="body2" color="text.secondary">{order.mobileNumber}</Typography>
                  </TableCell>
                  <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>{formatQuantity(order.quantityKg)}</TableCell>
                  <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                    {formatInr(order.totalAmount)}
                    {order.deliveryCharge == null ? <Typography variant="caption" display="block" color="text.secondary">+ delivery TBC</Typography> : null}
                  </TableCell>
                  <TableCell sx={{ maxWidth: 280 }}><Typography variant="body2">{deliverySummary(order)}</Typography></TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(order.createdAt)}</TableCell>
                  <TableCell><RiceChip value={order.status} /></TableCell>
                  <TableCell><RiceChip value={order.paymentStatus} /></TableCell>
                  <TableCell><Button size="small" variant="outlined" onClick={() => setEditing(order)}>Update</Button></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </StateBlock>
      <OrderStatusDialog
        order={editing}
        onClose={() => setEditing(null)}
        onSaved={(updated) => {
          setOrders((current) => current.map((item) => (item.id === updated.id ? updated : item)))
          setEditing(null)
          setNotice(`${updated.orderNumber} updated.`)
        }}
      />
    </Stack>
  )
}

// ------------------------------------------------------------------ free samples

function SampleDialog({ sample, defaultLocation, defaultInstructions, onClose, onSaved }: {
  sample: RiceSample | null
  defaultLocation: string
  defaultInstructions: string
  onClose: () => void
  onSaved: (sample: RiceSample) => void
}) {
  const [status, setStatus] = useState<RiceSampleStatus>('REQUESTED')
  const [pickupLocation, setPickupLocation] = useState('')
  const [pickupTime, setPickupTime] = useState('')
  const [pickupInstructions, setPickupInstructions] = useState('')
  const [adminNotes, setAdminNotes] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!sample) return
    setStatus(sample.status === 'REQUESTED' ? 'PICKUP_SCHEDULED' : sample.status)
    setPickupLocation(sample.pickupLocation ?? defaultLocation)
    setPickupTime(toDateTimeInput(sample.pickupTime))
    setPickupInstructions(sample.pickupInstructions ?? defaultInstructions)
    setAdminNotes(sample.adminNotes ?? '')
    setError('')
  }, [sample, defaultLocation, defaultInstructions])

  const save = async () => {
    if (!sample || saving) return
    const validation = validateSampleSchedule(status, pickupLocation, pickupTime)
    if (validation) {
      setError(validation)
      return
    }
    setSaving(true)
    setError('')
    try {
      onSaved(await riceAdminApi.updateSample(sample.id, {
        status,
        pickupLocation: pickupLocation.trim(),
        pickupTime: toLocalDateTime(pickupTime),
        pickupInstructions: pickupInstructions.trim(),
        adminNotes,
      }))
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to update the sample request.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open={Boolean(sample)} onClose={saving ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>Sample {sample?.requestNumber}</DialogTitle>
      <DialogContent dividers>
        {sample ? (
          <Stack spacing={2}>
            <Typography><b>{sample.customerName}</b> • {sample.mobileNumber}</Typography>
            <Typography color="text.secondary">
              {formatSampleWeight(sample.sampleWeightGrams)} free sample • {formatInr(sample.price)} • Pickup only (no home delivery)
            </Typography>
            <TextField select label="Status" value={status} onChange={(e) => setStatus(e.target.value as RiceSampleStatus)}>
              {allowedSampleStatuses(sample.status).map((s) => <MenuItem key={s} value={s}>{humanize(s)}</MenuItem>)}
            </TextField>
            <TextField label="Pickup location" value={pickupLocation} onChange={(e) => setPickupLocation(e.target.value)} multiline inputProps={{ maxLength: 1000 }} required={status === 'PICKUP_SCHEDULED'} />
            <TextField label="Collection date & time" type="datetime-local" InputLabelProps={{ shrink: true }} value={pickupTime} onChange={(e) => setPickupTime(e.target.value)} required={status === 'PICKUP_SCHEDULED'} />
            <TextField label="Pickup instructions for customer" value={pickupInstructions} onChange={(e) => setPickupInstructions(e.target.value)} multiline minRows={2} inputProps={{ maxLength: 2000 }} />
            <TextField label="Admin notes (internal)" value={adminNotes} onChange={(e) => setAdminNotes(e.target.value)} multiline minRows={2} inputProps={{ maxLength: 2000 }} />
            {error ? <Alert severity="error">{error}</Alert> : null}
          </Stack>
        ) : null}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={saving}>Cancel</Button>
        <Button variant="contained" onClick={() => void save()} disabled={saving}>{saving ? 'Saving…' : 'Save'}</Button>
      </DialogActions>
    </Dialog>
  )
}

function SamplesTab({ defaultLocation, defaultInstructions }: { defaultLocation: string; defaultInstructions: string }) {
  const [filters, setFilters] = useState<RiceSampleFilters>({})
  const [samples, setSamples] = useState<RiceSample[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState<RiceSample | null>(null)
  const [notice, setNotice] = useState('')

  const load = useCallback(() => {
    setLoading(true)
    setError('')
    riceAdminApi.getSamples(filters)
      .then(setSamples)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load sample requests.')))
      .finally(() => setLoading(false))
  }, [filters])

  useEffect(() => { load() }, [load])

  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={1} flexWrap="wrap" useFlexGap>
        <TextField select size="small" label="Status" value={filters.status ?? ''} onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value as RiceSampleStatus | '' }))} sx={{ minWidth: 180 }}>
          <MenuItem value="">All statuses</MenuItem>
          {RICE_SAMPLE_STATUSES.map((s) => <MenuItem key={s} value={s}>{humanize(s)}</MenuItem>)}
        </TextField>
        <DateRangeFilters from={filters.from} to={filters.to} onChange={(from, to) => setFilters((f) => ({ ...f, from, to }))} />
        <SearchBox onSearch={(search) => setFilters((f) => ({ ...f, search }))} />
        <Button onClick={load}>Refresh</Button>
      </Stack>
      {notice ? <Alert severity="success" onClose={() => setNotice('')}>{notice}</Alert> : null}
      <StateBlock loading={loading} error={error} empty={samples.length === 0} emptyText="No free sample requests match these filters." onRetry={load}>
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Request</TableCell>
                <TableCell>Customer</TableCell>
                <TableCell>Sample</TableCell>
                <TableCell>Pickup</TableCell>
                <TableCell>Created</TableCell>
                <TableCell>Status</TableCell>
                <TableCell />
              </TableRow>
            </TableHead>
            <TableBody>
              {samples.map((sample) => (
                <TableRow key={sample.id} hover>
                  <TableCell sx={{ fontWeight: 700, whiteSpace: 'nowrap' }}>{sample.requestNumber}</TableCell>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>{sample.customerName}</Typography>
                    <Typography variant="body2" color="text.secondary">{sample.mobileNumber}</Typography>
                  </TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatSampleWeight(sample.sampleWeightGrams)} • {formatInr(sample.price)}</TableCell>
                  <TableCell sx={{ maxWidth: 280 }}>
                    {sample.pickupLocation || sample.pickupTime ? (
                      <>
                        <Typography variant="body2">{sample.pickupLocation ?? 'Location pending'}</Typography>
                        <Typography variant="body2" color="text.secondary">{formatDateTime(sample.pickupTime)}</Typography>
                      </>
                    ) : <Typography variant="body2" color="text.secondary">Awaiting pickup schedule</Typography>}
                  </TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(sample.createdAt)}</TableCell>
                  <TableCell><RiceChip value={sample.status} /></TableCell>
                  <TableCell>
                    <Button size="small" variant="outlined" disabled={isSampleLocked(sample)} onClick={() => setEditing(sample)}>
                      {sample.status === 'REQUESTED' ? 'Schedule pickup' : 'Update'}
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </StateBlock>
      <SampleDialog
        sample={editing}
        defaultLocation={defaultLocation}
        defaultInstructions={defaultInstructions}
        onClose={() => setEditing(null)}
        onSaved={(updated) => {
          setSamples((current) => current.map((item) => (item.id === updated.id ? updated : item)))
          setEditing(null)
          setNotice(`${updated.requestNumber} updated.`)
        }}
      />
    </Stack>
  )
}

// ------------------------------------------------------------------ product settings

function ProductSettingsTab({ form, setForm, onSaved }: {
  form: RiceProductUpdate
  setForm: (updater: (form: RiceProductUpdate) => RiceProductUpdate) => void
  onSaved: (form: RiceProductUpdate) => void
}) {
  const [highlightsText, setHighlightsText] = useState(form.highlights.join('\n'))
  const [deliveryText, setDeliveryText] = useState(form.deliveryCharge == null ? '' : String(form.deliveryCharge))
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const set = <K extends keyof RiceProductUpdate>(key: K, value: RiceProductUpdate[K]) => setForm((f) => ({ ...f, [key]: value }))
  const num = (value: string) => (value === '' ? Number.NaN : Number(value))

  const save = async () => {
    if (saving) return
    const payload: RiceProductUpdate = {
      ...form,
      highlights: highlightsText.split('\n').map((h) => h.trim()).filter(Boolean),
      deliveryCharge: deliveryText.trim() === '' ? null : Number(deliveryText),
      imageUrl: form.imageUrl?.trim() ?? '',
    }
    const validation = validateProductForm(payload)
    if (validation) {
      setError(validation)
      return
    }
    setSaving(true)
    setError('')
    setNotice('')
    try {
      const saved = productToForm(await riceAdminApi.updateProduct(payload))
      onSaved(saved)
      setHighlightsText(saved.highlights.join('\n'))
      setDeliveryText(saved.deliveryCharge == null ? '' : String(saved.deliveryCharge))
      setNotice('Village Farm Rice settings saved. The app will show the new values on next load.')
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to save rice settings.'))
    } finally {
      setSaving(false)
    }
  }

  const pricePreview = form.basePackKg > 0 && form.basePackPrice > 0
    ? `${formatInr(form.basePackPrice)} per ${form.basePackKg} kg • minimum ${form.minOrderKg} kg = ${formatInr(Math.round((form.basePackPrice * form.minOrderKg * 100) / form.basePackKg) / 100)}`
    : 'Enter pack size and price'

  return (
    <Stack spacing={2}>
      <Card variant="outlined"><CardContent>
        <Stack spacing={2}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>Product</Typography>
          <FormControlLabel control={<Switch checked={form.active} onChange={(e) => set('active', e.target.checked)} />} label="Accept rice orders" />
          <TextField label="Name" value={form.name} onChange={(e) => set('name', e.target.value)} required inputProps={{ maxLength: 120 }} />
          <TextField label="Tagline" value={form.tagline ?? ''} onChange={(e) => set('tagline', e.target.value)} inputProps={{ maxLength: 255 }} />
          <TextField label="Description" value={form.description ?? ''} onChange={(e) => set('description', e.target.value)} multiline minRows={3} inputProps={{ maxLength: 4000 }} />
          <TextField label="Highlights (one per line, max 10)" value={highlightsText} onChange={(e) => setHighlightsText(e.target.value)} multiline minRows={3} />
          <TextField label="Image URL (https, optional)" value={form.imageUrl ?? ''} onChange={(e) => set('imageUrl', e.target.value)} helperText="Leave empty to use the built-in farm photograph." />
        </Stack>
      </CardContent></Card>

      <Card variant="outlined"><CardContent>
        <Stack spacing={2}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>Pricing & quantity</Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6} md={4}><TextField fullWidth type="number" label="Base pack (kg)" value={Number.isNaN(form.basePackKg) ? '' : form.basePackKg} onChange={(e) => set('basePackKg', num(e.target.value))} /></Grid>
            <Grid item xs={12} sm={6} md={4}><TextField fullWidth type="number" label="Base pack price (₹)" value={Number.isNaN(form.basePackPrice) ? '' : form.basePackPrice} onChange={(e) => set('basePackPrice', num(e.target.value))} /></Grid>
            <Grid item xs={12} sm={6} md={4}><TextField fullWidth type="number" label="Minimum order (kg)" value={Number.isNaN(form.minOrderKg) ? '' : form.minOrderKg} onChange={(e) => set('minOrderKg', num(e.target.value))} /></Grid>
            <Grid item xs={12} sm={6} md={4}><TextField fullWidth type="number" label="Order step (kg)" value={Number.isNaN(form.orderStepKg) ? '' : form.orderStepKg} onChange={(e) => set('orderStepKg', num(e.target.value))} /></Grid>
            <Grid item xs={12} sm={6} md={4}><TextField fullWidth type="number" label="Maximum order (kg)" value={Number.isNaN(form.maxOrderKg) ? '' : form.maxOrderKg} onChange={(e) => set('maxOrderKg', num(e.target.value))} /></Grid>
            <Grid item xs={12} sm={6} md={4}>
              <TextField fullWidth type="number" label="Delivery charge (₹)" value={deliveryText} onChange={(e) => setDeliveryText(e.target.value)} helperText="Leave empty if confirmed by the team after ordering." />
            </Grid>
          </Grid>
          <Alert severity="info">{pricePreview}</Alert>
          <TextField label="Payment note shown to customers" value={form.paymentNote ?? ''} onChange={(e) => set('paymentNote', e.target.value)} multiline minRows={2} inputProps={{ maxLength: 2000 }} />
        </Stack>
      </CardContent></Card>

      <Card variant="outlined"><CardContent>
        <Stack spacing={2}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>Free sample (pickup only, always ₹0)</Typography>
          <FormControlLabel control={<Switch checked={form.sampleEnabled} onChange={(e) => set('sampleEnabled', e.target.checked)} />} label="Offer free samples" />
          <TextField type="number" label="Sample weight (grams)" value={Number.isNaN(form.sampleWeightGrams) ? '' : form.sampleWeightGrams} onChange={(e) => set('sampleWeightGrams', num(e.target.value))} sx={{ maxWidth: 260 }} />
          <TextField label="Default pickup location" value={form.samplePickupLocation ?? ''} onChange={(e) => set('samplePickupLocation', e.target.value)} multiline inputProps={{ maxLength: 1000 }} />
          <TextField label="Default pickup instructions" value={form.samplePickupInstructions ?? ''} onChange={(e) => set('samplePickupInstructions', e.target.value)} multiline minRows={2} inputProps={{ maxLength: 2000 }} />
        </Stack>
      </CardContent></Card>

      {error ? <Alert severity="error">{error}</Alert> : null}
      {notice ? <Alert severity="success" onClose={() => setNotice('')}>{notice}</Alert> : null}
      <Box><Button variant="contained" size="large" onClick={() => void save()} disabled={saving}>{saving ? 'Saving…' : 'Save settings'}</Button></Box>
    </Stack>
  )
}

export function AdminRiceOrdersPage() {
  const [tab, setTab] = useState(0)
  const [productForm, setProductForm] = useState<RiceProductUpdate | null>(null)
  const [productError, setProductError] = useState('')
  const [productLoading, setProductLoading] = useState(true)

  const loadProduct = useCallback(() => {
    setProductLoading(true)
    setProductError('')
    riceAdminApi.getProduct()
      .then((product) => setProductForm(productToForm(product)))
      .catch((e: unknown) => setProductError(apiErrorMessage(e, 'Unable to load rice settings.')))
      .finally(() => setProductLoading(false))
  }, [])

  useEffect(() => { loadProduct() }, [loadProduct])

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 700 }}>Rice Orders</Typography>
        <Typography color="text.secondary">Village Farm Rice bulk orders, free pickup samples and product settings.</Typography>
      </Box>
      <Tabs value={tab} onChange={(_, value: number) => setTab(value)} variant="scrollable" allowScrollButtonsMobile>
        <Tab label="Bulk orders" />
        <Tab label="Free samples" />
        <Tab label="Product settings" />
      </Tabs>
      {tab === 0 ? <BulkOrdersTab /> : null}
      {tab === 1 ? (
        <SamplesTab defaultLocation={productForm?.samplePickupLocation ?? ''} defaultInstructions={productForm?.samplePickupInstructions ?? ''} />
      ) : null}
      {tab === 2 ? (
        <StateBlock loading={productLoading} error={productError} empty={!productForm} emptyText="Rice product is not configured." onRetry={loadProduct}>
          {productForm ? (
            <ProductSettingsTab
              form={productForm}
              setForm={(updater) => setProductForm((current) => (current ? updater(current) : current))}
              onSaved={setProductForm}
            />
          ) : null}
        </StateBlock>
      ) : null}
    </Stack>
  )
}
