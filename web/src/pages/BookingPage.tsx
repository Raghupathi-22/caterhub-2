import { Alert, Box, Button, Card, CardContent, Checkbox, FormControlLabel, Grid, MenuItem, Stack, TextField, Typography } from '@mui/material'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { bookingApi } from '../api/bookingApi'
import { catalogApi } from '../api/catalogApi'
import { apiErrorMessage } from '../api/http'
import { EVENT_TYPES } from '../catalog/eventTypes'
import type { CatalogCategory } from '../types/models'

export function BookingPage() {
  const navigate = useNavigate()
  const { categoryId } = useParams()
  const [category, setCategory] = useState<CatalogCategory | null>(null)
  const [selectedServices, setSelectedServices] = useState<string[]>([])
  const [eventType, setEventType] = useState('')
  const [eventDate, setEventDate] = useState('')
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [guestCount, setGuestCount] = useState('100')
  const [location, setLocation] = useState('')
  const [area, setArea] = useState('')
  const [instructions, setInstructions] = useState('')
  const [totalAmount, setTotalAmount] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (!categoryId) return
    catalogApi
      .getCategory(categoryId)
      .then(setCategory)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load category for booking.')))
  }, [categoryId])

  const timeValid = useMemo(() => {
    if (!startTime || !endTime) return false
    return endTime > startTime
  }, [startTime, endTime])

  const canSubmit = Boolean(
    category &&
      selectedServices.length > 0 &&
      eventType &&
      eventDate &&
      startTime &&
      endTime &&
      timeValid &&
      location.trim() &&
      area.trim() &&
      totalAmount &&
      Number(totalAmount) > 0,
  )

  const toggleService = (service: string) => {
    setSelectedServices((current) => (current.includes(service) ? current.filter((value) => value !== service) : [...current, service]))
  }

  const submit = async () => {
    if (!category || !canSubmit) return
    setError('')
    setSuccess('')
    setSubmitting(true)
    try {
      await bookingApi.createBooking({
        categoryId: category.id,
        serviceType: category.serviceType,
        selectedServices,
        eventType,
        eventDate,
        startTime,
        endTime,
        guestCount: Number(guestCount),
        location,
        area,
        instructions,
        totalAmount: Number(totalAmount),
      })
      setSuccess('Booking submitted successfully')
      setTimeout(() => navigate('/my-bookings'), 800)
    } catch (e: unknown) {
      setError(apiErrorMessage(e, 'Booking submission failed.'))
    } finally {
      setSubmitting(false)
    }
  }

  if (!category) {
    return <Typography>Loading booking form...</Typography>
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>
        Book {category.name}
      </Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      {success ? <Alert severity="success">{success}</Alert> : null}

      <Card>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Select services</Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap' }}>
                {category.services.map((service) => (
                  <FormControlLabel
                    key={service}
                    control={<Checkbox checked={selectedServices.includes(service)} onChange={() => toggleService(service)} />}
                    label={service}
                  />
                ))}
              </Box>
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth select label="Event Type" value={eventType} onChange={(e) => setEventType(e.target.value)}>
                <MenuItem value="">Select event type</MenuItem>
                {EVENT_TYPES.map((value) => <MenuItem key={value} value={value}>{value}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth type="date" label="Date" value={eventDate} onChange={(e) => setEventDate(e.target.value)} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth type="time" label="Start Time" value={startTime} onChange={(e) => setStartTime(e.target.value)} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                type="time"
                label="End Time"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                InputLabelProps={{ shrink: true }}
                error={Boolean(endTime && startTime && !timeValid)}
                helperText={endTime && startTime && !timeValid ? 'End time must be after start time' : ' '}
              />
            </Grid>
            {category.serviceType === 'CATERING_FOOD' ? (
              <Grid item xs={12} md={6}>
                <TextField fullWidth type="number" label="Guest Count" value={guestCount} onChange={(e) => setGuestCount(e.target.value)} />
              </Grid>
            ) : null}
            <Grid item xs={12} md={6}>
              <TextField fullWidth type="number" label="Estimated Amount (INR)" value={totalAmount} onChange={(e) => setTotalAmount(e.target.value)} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="Event Address" value={location} onChange={(e) => setLocation(e.target.value)} />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField fullWidth label="Area / Locality" value={area} onChange={(e) => setArea(e.target.value)} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth multiline minRows={3} label="Special Instructions" value={instructions} onChange={(e) => setInstructions(e.target.value)} />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      <Button variant="contained" disabled={!canSubmit || submitting} onClick={() => void submit()}>
        Continue
      </Button>
    </Stack>
  )
}
