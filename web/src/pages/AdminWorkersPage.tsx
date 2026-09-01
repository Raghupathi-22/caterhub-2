import { Alert, Button, Paper, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import { StatusChip } from '../components/StatusChip'
import type { WorkerProfile } from '../types/models'

export function AdminWorkersPage() {
  const [workers, setWorkers] = useState<WorkerProfile[]>([])
  const [error, setError] = useState('')

  const load = () => {
    adminApi
      .getWorkerProfiles()
      .then(setWorkers)
      .catch((e: unknown) => setError(apiErrorMessage(e, 'Unable to load workers.')))
  }

  useEffect(() => {
    load()
  }, [])

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Workers</Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Paper>
        <Table size="small">
          <TableHead><TableRow><TableCell>Worker</TableCell><TableCell>Role</TableCell><TableCell>Skills</TableCell><TableCell>Area</TableCell><TableCell>Verification</TableCell><TableCell>Action</TableCell></TableRow></TableHead>
          <TableBody>
            {workers.map((worker) => (
              <TableRow key={worker.id}>
                <TableCell>{worker.fullName}</TableCell>
                <TableCell>{worker.workerType.replace(/_/g, ' ')}</TableCell>
                <TableCell>{worker.skills || '-'}</TableCell>
                <TableCell>{worker.preferredAreas || '-'}</TableCell>
                <TableCell><StatusChip status={worker.status} /></TableCell>
                <TableCell><Button component={RouterLink} to={`/admin/workers/${worker.id}`}>View</Button></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Stack>
  )
}
