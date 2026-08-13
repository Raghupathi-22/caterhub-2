import { useCallback, useEffect, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Grid,
  MenuItem,
  Paper,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import { adminApi } from '../api/adminApi'
import { useAuthStore } from '../store/authStore'
import type { Offer } from '../types/api'
import { StatusChip } from '../components/StatusChip'

interface FormState {
  couponCode: string
  description: string
  discountType: Offer['discountType']
  discountValue: string
  minOrderValue: string
  maxDiscount: string
  validFrom: string
  validUntil: string
}

const defaultForm: FormState = {
  couponCode: '',
  description: '',
  discountType: 'PERCENTAGE',
  discountValue: '',
  minOrderValue: '',
  maxDiscount: '',
  validFrom: '',
  validUntil: '',
}

export function OffersPage() {
  const businessId = useAuthStore((state) => state.user?.business_id ?? 1)
  const [offers, setOffers] = useState<Offer[]>([])
  const [form, setForm] = useState<FormState>(defaultForm)
  const [error, setError] = useState('')

  const loadOffers = useCallback(async () => {
    try {
      const data = await adminApi.getOffers(businessId)
      setOffers(data)
    } catch {
      setError('Unable to load offers.')
    }
  }, [businessId])

  useEffect(() => {
    void loadOffers()
  }, [loadOffers])

  const submit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    try {
      await adminApi.createOffer({
        businessId,
        couponCode: form.couponCode.trim().toUpperCase(),
        description: form.description.trim(),
        discountType: form.discountType,
        discountValue: Number(form.discountValue),
        minOrderValue: form.minOrderValue ? Number(form.minOrderValue) : undefined,
        maxDiscount: form.maxDiscount ? Number(form.maxDiscount) : undefined,
        validFrom: form.validFrom,
        validUntil: form.validUntil,
      })
      setForm(defaultForm)
      await loadOffers()
    } catch {
      setError('Unable to create offer. Ensure values and dates are valid.')
    }
  }

  const toggleActive = async (offer: Offer, active: boolean) => {
    try {
      const updated = await adminApi.setOfferActive(offer.id, active)
      setOffers((current) => current.map((item) => (item.id === offer.id ? updated : item)))
    } catch {
      setError('Unable to update offer state.')
    }
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" fontWeight={700}>
        Offers Management
      </Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}

      <Paper sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Create Offer
        </Typography>
        <Box component="form" onSubmit={submit}>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                required
                fullWidth
                label="Coupon Code"
                value={form.couponCode}
                onChange={(e) => setForm((prev) => ({ ...prev, couponCode: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                required
                fullWidth
                select
                label="Discount Type"
                value={form.discountType}
                onChange={(e) =>
                  setForm((prev) => ({
                    ...prev,
                    discountType: e.target.value as Offer['discountType'],
                  }))
                }
              >
                <MenuItem value="PERCENTAGE">Percentage</MenuItem>
                <MenuItem value="FLAT_AMOUNT">Flat Amount</MenuItem>
                <MenuItem value="FREE_DELIVERY">Free Delivery</MenuItem>
                <MenuItem value="BUY_ONE_GET_ONE">Buy 1 Get 1</MenuItem>
              </TextField>
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                required
                fullWidth
                type="number"
                label="Discount Value"
                value={form.discountValue}
                onChange={(e) => setForm((prev) => ({ ...prev, discountValue: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                fullWidth
                type="number"
                label="Min Order Value"
                value={form.minOrderValue}
                onChange={(e) => setForm((prev) => ({ ...prev, minOrderValue: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                fullWidth
                type="number"
                label="Max Discount"
                value={form.maxDiscount}
                onChange={(e) => setForm((prev) => ({ ...prev, maxDiscount: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                required
                fullWidth
                type="datetime-local"
                label="Valid From"
                InputLabelProps={{ shrink: true }}
                value={form.validFrom}
                onChange={(e) => setForm((prev) => ({ ...prev, validFrom: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                required
                fullWidth
                type="datetime-local"
                label="Valid Until"
                InputLabelProps={{ shrink: true }}
                value={form.validUntil}
                onChange={(e) => setForm((prev) => ({ ...prev, validUntil: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 9 }}>
              <TextField
                required
                fullWidth
                label="Description"
                value={form.description}
                onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Button fullWidth type="submit" variant="contained" sx={{ height: '100%' }}>
                Upload Offer
              </Button>
            </Grid>
          </Grid>
        </Box>
      </Paper>

      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Code</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Value</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Active</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {offers.map((offer) => (
              <TableRow key={offer.id}>
                <TableCell>{offer.couponCode}</TableCell>
                <TableCell>{offer.description}</TableCell>
                <TableCell>{offer.discountType}</TableCell>
                <TableCell>{offer.discountValue}</TableCell>
                <TableCell>
                  <StatusChip status={offer.isActive ? 'ACTIVE' : 'DRAFT'} />
                </TableCell>
                <TableCell>
                  <Switch
                    checked={offer.isActive}
                    onChange={(e) => void toggleActive(offer, e.target.checked)}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Stack>
  )
}
