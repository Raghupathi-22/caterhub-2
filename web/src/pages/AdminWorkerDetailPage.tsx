import { Alert, Box, Button, Card, CardContent, Divider, IconButton, Snackbar, Stack, Tooltip, Typography } from '@mui/material'
import ContentCopyIcon from '@mui/icons-material/ContentCopy'
import PhoneIcon from '@mui/icons-material/Phone'
import { useCallback, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { StatusChip } from '../components/StatusChip'
import type { WorkerProfile } from '../types/models'
import { formatPhoneNumber, telHref } from '../utils/adminContact'

export function AdminWorkerDetailPage() {
  const { id } = useParams()
  const [worker, setWorker] = useState<WorkerProfile | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [copyMessage, setCopyMessage] = useState('')

  const load = useCallback(() => {
    const profileId = Number(id)
    if (!id || !Number.isFinite(profileId)) {
      setError('Invalid worker id.')
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    adminApi
      .getWorkerProfile(profileId)
      .then(setWorker)
      .catch((e: unknown) => {
        setWorker(null)
        setError(apiErrorMessage(e, 'Unable to load worker profile.'))
      })
      .finally(() => setLoading(false))
  }, [id])

  useEffect(() => {
    load()
  }, [load])

  const copyPhone = async (phone: string) => {
    try {
      await navigator.clipboard.writeText(phone)
      setCopyMessage('Mobile number copied')
    } catch {
      setCopyMessage('Unable to copy. Please copy the number manually.')
    }
  }

  if (loading) return <Typography>Loading worker detail...</Typography>
  if (error || !worker) {
    return (
      <Alert severity="error" action={<Button color="inherit" size="small" onClick={load}>Retry</Button>}>
        {error || 'Worker profile not found.'}
      </Alert>
    )
  }

  const phone = worker.phoneNumber?.trim()
  const callHref = telHref(phone)

  return (
    <Card>
      <CardContent>
        <Stack spacing={1}>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>{worker.fullName}</Typography>
          <Typography>{worker.workerType.replace(/_/g, ' ')}</Typography>
          <StatusChip status={worker.status} />

          <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 2, px: 2, py: 1.5, my: 1 }}>
            <Typography variant="overline" color="text.secondary">Registered mobile</Typography>
            {phone ? (
              <Stack direction="row" alignItems="center" spacing={1} flexWrap="wrap" useFlexGap>
                <Typography variant="h6" sx={{ fontWeight: 700, letterSpacing: 0.5 }}>{formatPhoneNumber(phone)}</Typography>
                <Tooltip title="Copy mobile number">
                  <IconButton aria-label="Copy mobile number" onClick={() => copyPhone(phone)} size="small">
                    <ContentCopyIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                {callHref ? (
                  <Button component="a" href={callHref} size="small" variant="outlined" startIcon={<PhoneIcon />}>Call</Button>
                ) : null}
              </Stack>
            ) : (
              <Typography color="text.secondary">Not available</Typography>
            )}
          </Box>

          <Divider />
          <Typography color="text.secondary">Experience: {worker.experienceYears} years</Typography>
          <Typography color="text.secondary">Skills: {worker.skills || '-'}</Typography>
          <Typography color="text.secondary">Areas: {worker.preferredAreas || '-'}</Typography>
          <Typography color="text.secondary">Languages: {worker.languages || '-'}</Typography>
          <Typography color="text.secondary">Bio: {worker.bio || '-'}</Typography>
          {worker.rejectionReason ? <Alert severity="warning">{worker.rejectionReason}</Alert> : null}
        </Stack>
      </CardContent>
      <Snackbar
        open={Boolean(copyMessage)}
        autoHideDuration={2500}
        onClose={() => setCopyMessage('')}
        message={copyMessage}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </Card>
  )
}
