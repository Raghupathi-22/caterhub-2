import { Alert, Button, Grid, MenuItem, Paper, Stack, Switch, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { useAuthStore } from '../store/authStore'
import type { AdminOffer } from '../types/models'

export function AdminOffersPage() {
  const businessId = useAuthStore((state) => state.user?.business_id ?? 1)
  const [offers, setOffers] = useState<AdminOffer[]>([])
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    couponCode: '',
    description: '',
    discountType: 'PERCENTAGE' as AdminOffer['discountType'],
    discountValue: '',
    validFrom: '',
    validUntil: '',
  })

  const load = useCallback(() => {
    adminApi
      .getOffers(businessId)
      .then(setOffers)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load offers.')))
  }, [businessId])

  useEffect(() => {
    load()
  }, [load])

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
        validFrom: form.validFrom,
        validUntil: form.validUntil,
      })
      setForm({ couponCode: '', description: '', discountType: 'PERCENTAGE', discountValue: '', validFrom: '', validUntil: '' })
      load()
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to create offer.'))
    }
  }

  const toggle = async (id: number, active: boolean) => {
    try {
      const updated = await adminApi.setOfferActive(id, active)
      setOffers((current) => current.map((item) => (item.id === id ? updated : item)))
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to update offer.'))
    }
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Offers</Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Paper sx={{ p: 2 }}>
        <form onSubmit={submit}>
          <Grid container spacing={2}>
            <Grid item xs={12} md={2}><TextField required fullWidth label="Code" value={form.couponCode} onChange={(e) => setForm((p) => ({ ...p, couponCode: e.target.value }))} /></Grid>
            <Grid item xs={12} md={2}>
              <TextField required fullWidth select label="Type" value={form.discountType} onChange={(e) => setForm((p) => ({ ...p, discountType: e.target.value as AdminOffer['discountType'] }))}>
                <MenuItem value="PERCENTAGE">Percentage</MenuItem>
                <MenuItem value="FLAT_AMOUNT">Flat Amount</MenuItem>
                <MenuItem value="FREE_DELIVERY">Free Delivery</MenuItem>
                <MenuItem value="BUY_ONE_GET_ONE">Buy 1 Get 1</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} md={2}><TextField required fullWidth type="number" label="Value" value={form.discountValue} onChange={(e) => setForm((p) => ({ ...p, discountValue: e.target.value }))} /></Grid>
            <Grid item xs={12} md={2}><TextField required fullWidth type="datetime-local" label="From" InputLabelProps={{ shrink: true }} value={form.validFrom} onChange={(e) => setForm((p) => ({ ...p, validFrom: e.target.value }))} /></Grid>
            <Grid item xs={12} md={2}><TextField required fullWidth type="datetime-local" label="Until" InputLabelProps={{ shrink: true }} value={form.validUntil} onChange={(e) => setForm((p) => ({ ...p, validUntil: e.target.value }))} /></Grid>
            <Grid item xs={12} md={2}><Button type="submit" fullWidth variant="contained" sx={{ height: '100%' }}>Create</Button></Grid>
            <Grid item xs={12}><TextField required fullWidth label="Description" value={form.description} onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))} /></Grid>
          </Grid>
        </form>
      </Paper>
      <Paper>
        <Table size="small">
          <TableHead><TableRow><TableCell>Code</TableCell><TableCell>Description</TableCell><TableCell>Type</TableCell><TableCell>Value</TableCell><TableCell>Active</TableCell></TableRow></TableHead>
          <TableBody>
            {offers.map((offer) => (
              <TableRow key={offer.id}>
                <TableCell>{offer.couponCode}</TableCell>
                <TableCell>{offer.description}</TableCell>
                <TableCell>{offer.discountType}</TableCell>
                <TableCell>{offer.discountValue}</TableCell>
                <TableCell><Switch checked={offer.isActive} onChange={(e) => void toggle(offer.id, e.target.checked)} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Stack>
  )
}
