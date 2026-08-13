import { useCallback, useEffect, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Grid,
  MenuItem,
  Paper,
  Stack,
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
import type { EventCampaign } from '../types/api'
import { StatusChip } from '../components/StatusChip'

interface EventFormState {
  campaignName: string
  campaignDescription: string
  campaignType: string
  startDate: string
  endDate: string
  targetAudience: string
  budget: string
}

const defaultEventForm: EventFormState = {
  campaignName: '',
  campaignDescription: '',
  campaignType: '',
  startDate: '',
  endDate: '',
  targetAudience: '',
  budget: '',
}

const statuses: EventCampaign['status'][] = ['DRAFT', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED']

export function EventsPage() {
  const businessId = useAuthStore((state) => state.user?.business_id ?? 1)
  const [events, setEvents] = useState<EventCampaign[]>([])
  const [form, setForm] = useState<EventFormState>(defaultEventForm)
  const [error, setError] = useState('')

  const loadEvents = useCallback(async () => {
    try {
      const data = await adminApi.getEvents(businessId)
      setEvents(data)
    } catch {
      setError('Unable to load events.')
    }
  }, [businessId])

  useEffect(() => {
    void loadEvents()
  }, [loadEvents])

  const submit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    try {
      await adminApi.createEvent({
        businessId,
        campaignName: form.campaignName.trim(),
        campaignDescription: form.campaignDescription.trim(),
        campaignType: form.campaignType.trim(),
        startDate: form.startDate,
        endDate: form.endDate,
        targetAudience: form.targetAudience.trim() || undefined,
        budget: form.budget ? Number(form.budget) : undefined,
        status: 'DRAFT',
      })
      setForm(defaultEventForm)
      await loadEvents()
    } catch {
      setError('Unable to create event campaign.')
    }
  }

  const changeStatus = async (eventId: number, status: EventCampaign['status']) => {
    try {
      const updated = await adminApi.updateEventStatus(eventId, status)
      setEvents((current) => current.map((item) => (item.id === eventId ? updated : item)))
    } catch {
      setError('Unable to update event status.')
    }
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" fontWeight={700}>
        Events & Campaigns
      </Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}

      <Paper sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Create Event
        </Typography>
        <Box component="form" onSubmit={submit}>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                required
                fullWidth
                label="Campaign Name"
                value={form.campaignName}
                onChange={(e) => setForm((prev) => ({ ...prev, campaignName: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                required
                fullWidth
                label="Campaign Type"
                value={form.campaignType}
                onChange={(e) => setForm((prev) => ({ ...prev, campaignType: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                required
                fullWidth
                type="datetime-local"
                label="Start Date"
                InputLabelProps={{ shrink: true }}
                value={form.startDate}
                onChange={(e) => setForm((prev) => ({ ...prev, startDate: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                required
                fullWidth
                type="datetime-local"
                label="End Date"
                InputLabelProps={{ shrink: true }}
                value={form.endDate}
                onChange={(e) => setForm((prev) => ({ ...prev, endDate: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                fullWidth
                label="Target Audience"
                value={form.targetAudience}
                onChange={(e) => setForm((prev) => ({ ...prev, targetAudience: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <TextField
                fullWidth
                type="number"
                label="Budget (INR)"
                value={form.budget}
                onChange={(e) => setForm((prev) => ({ ...prev, budget: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                required
                fullWidth
                label="Campaign Description"
                value={form.campaignDescription}
                onChange={(e) =>
                  setForm((prev) => ({ ...prev, campaignDescription: e.target.value }))
                }
              />
            </Grid>
            <Grid size={{ xs: 12, md: 3 }}>
              <Button fullWidth type="submit" variant="contained" sx={{ height: '100%' }}>
                Upload Event
              </Button>
            </Grid>
          </Grid>
        </Box>
      </Paper>

      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Start</TableCell>
              <TableCell>End</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Update Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {events.map((eventItem) => (
              <TableRow key={eventItem.id}>
                <TableCell>{eventItem.campaignName}</TableCell>
                <TableCell>{eventItem.campaignType}</TableCell>
                <TableCell>{eventItem.startDate}</TableCell>
                <TableCell>{eventItem.endDate}</TableCell>
                <TableCell>
                  <StatusChip status={eventItem.status} />
                </TableCell>
                <TableCell sx={{ minWidth: 180 }}>
                  <TextField
                    select
                    size="small"
                    fullWidth
                    value={eventItem.status}
                    onChange={(e) =>
                      void changeStatus(eventItem.id, e.target.value as EventCampaign['status'])
                    }
                  >
                    {statuses.map((status) => (
                      <MenuItem key={status} value={status}>
                        {status}
                      </MenuItem>
                    ))}
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
