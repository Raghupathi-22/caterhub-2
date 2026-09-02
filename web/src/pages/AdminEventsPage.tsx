import { Alert, Button, Grid, MenuItem, Paper, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { useAuthStore } from '../store/authStore'
import { StatusChip } from '../components/StatusChip'
import type { AdminEvent } from '../types/models'

const statusValues: AdminEvent['status'][] = ['DRAFT', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED']

export function AdminEventsPage() {
  const businessId = useAuthStore((state) => state.user?.business_id ?? 1)
  const [events, setEvents] = useState<AdminEvent[]>([])
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    campaignName: '',
    campaignType: '',
    campaignDescription: '',
    startDate: '',
    endDate: '',
  })

  const load = useCallback(() => {
    adminApi
      .getEvents(businessId)
      .then(setEvents)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load events.')))
  }, [businessId])

  useEffect(() => {
    load()
  }, [load])

  const submit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    try {
      await adminApi.createEvent({
        businessId,
        campaignName: form.campaignName,
        campaignType: form.campaignType,
        campaignDescription: form.campaignDescription,
        startDate: form.startDate,
        endDate: form.endDate,
      })
      setForm({ campaignName: '', campaignType: '', campaignDescription: '', startDate: '', endDate: '' })
      load()
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to create event campaign.'))
    }
  }

  const updateStatus = async (id: number, status: AdminEvent['status']) => {
    try {
      const updated = await adminApi.updateEventStatus(id, status)
      setEvents((current) => current.map((item) => (item.id === id ? updated : item)))
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Unable to update event status.'))
    }
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Events</Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Paper sx={{ p: 2 }}>
        <form onSubmit={submit}>
          <Grid container spacing={2}>
            <Grid item xs={12} md={3}><TextField required fullWidth label="Campaign Name" value={form.campaignName} onChange={(e) => setForm((p) => ({ ...p, campaignName: e.target.value }))} /></Grid>
            <Grid item xs={12} md={3}><TextField required fullWidth label="Campaign Type" value={form.campaignType} onChange={(e) => setForm((p) => ({ ...p, campaignType: e.target.value }))} /></Grid>
            <Grid item xs={12} md={2}><TextField required fullWidth type="datetime-local" label="Start" InputLabelProps={{ shrink: true }} value={form.startDate} onChange={(e) => setForm((p) => ({ ...p, startDate: e.target.value }))} /></Grid>
            <Grid item xs={12} md={2}><TextField required fullWidth type="datetime-local" label="End" InputLabelProps={{ shrink: true }} value={form.endDate} onChange={(e) => setForm((p) => ({ ...p, endDate: e.target.value }))} /></Grid>
            <Grid item xs={12} md={2}><Button type="submit" fullWidth variant="contained" sx={{ height: '100%' }}>Create</Button></Grid>
            <Grid item xs={12}><TextField required fullWidth label="Description" value={form.campaignDescription} onChange={(e) => setForm((p) => ({ ...p, campaignDescription: e.target.value }))} /></Grid>
          </Grid>
        </form>
      </Paper>
      <Paper>
        <Table size="small">
          <TableHead><TableRow><TableCell>Name</TableCell><TableCell>Type</TableCell><TableCell>Start</TableCell><TableCell>End</TableCell><TableCell>Status</TableCell><TableCell>Update</TableCell></TableRow></TableHead>
          <TableBody>
            {events.map((item) => (
              <TableRow key={item.id}>
                <TableCell>{item.campaignName}</TableCell>
                <TableCell>{item.campaignType}</TableCell>
                <TableCell>{item.startDate}</TableCell>
                <TableCell>{item.endDate}</TableCell>
                <TableCell><StatusChip status={item.status} /></TableCell>
                <TableCell sx={{ minWidth: 170 }}>
                  <TextField select size="small" fullWidth value={item.status} onChange={(e) => void updateStatus(item.id, e.target.value as AdminEvent['status'])}>
                    {statusValues.map((status) => <MenuItem value={status} key={status}>{status}</MenuItem>)}
                  </TextField>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Stack>
  )
}
