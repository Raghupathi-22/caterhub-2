import { Alert, Button, Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { StatusChip } from '../components/StatusChip'
import type { WorkerProfile } from '../types/models'
import { formatPhoneNumber, telHref } from '../utils/adminContact'

export function AdminWorkersPage() {
  const [workers, setWorkers] = useState<WorkerProfile[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = () => {
    setLoading(true)
    setError('')
    adminApi
      .getWorkerProfiles()
      .then(setWorkers)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load workers.')))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Workers</Typography>
      {error ? <Alert severity="error" action={<Button color="inherit" size="small" onClick={load}>Retry</Button>}>{error}</Alert> : null}
      {loading ? <Typography color="text.secondary">Loading workers...</Typography> : null}
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead><TableRow><TableCell>Worker</TableCell><TableCell>Phone</TableCell><TableCell>Role</TableCell><TableCell>Skills</TableCell><TableCell>Area</TableCell><TableCell>Verification</TableCell><TableCell>Action</TableCell></TableRow></TableHead>
          <TableBody>
            {!loading && !error && workers.length === 0 ? (
              <TableRow><TableCell colSpan={7}><Typography color="text.secondary">No workers found.</Typography></TableCell></TableRow>
            ) : null}
            {workers.map((worker) => {
              const callHref = telHref(worker.phoneNumber)
              return (
                <TableRow key={worker.id}>
                  <TableCell>{worker.fullName}</TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>
                    {callHref ? <a href={callHref}>{formatPhoneNumber(worker.phoneNumber)}</a> : '-'}
                  </TableCell>
                  <TableCell>{worker.workerType.replace(/_/g, ' ')}</TableCell>
                  <TableCell>{worker.skills || '-'}</TableCell>
                  <TableCell>{worker.preferredAreas || '-'}</TableCell>
                  <TableCell><StatusChip status={worker.status} /></TableCell>
                  <TableCell><Button component={RouterLink} to={`/admin/workers/${worker.id}`}>View</Button></TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </Stack>
  )
}
