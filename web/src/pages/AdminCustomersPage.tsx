import {
  Alert,
  Button,
  Chip,
  InputAdornment,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import { useCallback, useEffect, useRef, useState } from 'react'
import { adminApi } from '../api/adminApi'
import { apiErrorMessage } from '../api/http'
import type { AdminCustomerPage } from '../types/models'
import { customerDisplayName, formatDate, formatPhoneNumber, telHref } from '../utils/adminContact'

const PAGE_SIZES = [10, 20, 50]
const minorColumn = { display: { xs: 'none', md: 'table-cell' } }

export function AdminCustomersPage() {
  const [data, setData] = useState<AdminCustomerPage | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const requestId = useRef(0)

  useEffect(() => {
    const handle = window.setTimeout(() => {
      setSearch(searchInput.trim())
      setPage(0)
    }, 350)
    return () => window.clearTimeout(handle)
  }, [searchInput])

  const load = useCallback(() => {
    const current = ++requestId.current
    setLoading(true)
    setError('')
    adminApi
      .getCustomers({ page, size, search })
      .then((result) => {
        if (current === requestId.current) setData(result)
      })
      .catch((e: unknown) => {
        if (current === requestId.current) {
          setData(null)
          setError(apiErrorMessage(e, 'Unable to load customers.'))
        }
      })
      .finally(() => {
        if (current === requestId.current) setLoading(false)
      })
  }, [page, size, search])

  useEffect(() => {
    load()
  }, [load])

  const customers = data?.content ?? []

  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'stretch', sm: 'center' }} spacing={1}>
        <Typography variant="h4" sx={{ fontWeight: 700 }}>Customers</Typography>
        <TextField
          size="small"
          placeholder="Search by name or mobile"
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
          inputProps={{ maxLength: 100, 'aria-label': 'Search customers by name or mobile number' }}
          InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> }}
          sx={{ minWidth: { sm: 300 } }}
        />
      </Stack>

      {error ? (
        <Alert severity="error" action={<Button color="inherit" size="small" onClick={load}>Retry</Button>}>{error}</Alert>
      ) : null}

      {!error ? (
        <Paper>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Mobile</TableCell>
                  <TableCell sx={minorColumn}>Email</TableCell>
                  <TableCell sx={minorColumn}>Registered</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading && customers.length === 0 ? (
                  <TableRow><TableCell colSpan={5}><Typography color="text.secondary">Loading customers...</Typography></TableCell></TableRow>
                ) : null}
                {!loading && customers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5}>
                      <Typography color="text.secondary">{search ? 'No customers match your search.' : 'No customers found.'}</Typography>
                    </TableCell>
                  </TableRow>
                ) : null}
                {customers.map((customer) => {
                  const name = customerDisplayName(customer)
                  const callHref = telHref(customer.phoneNumber)
                  return (
                    <TableRow key={customer.id} sx={{ opacity: loading ? 0.6 : 1 }}>
                      <TableCell>
                        {name ? <Typography sx={{ fontWeight: 600 }}>{name}</Typography> : <Typography color="text.secondary" fontStyle="italic">Name not provided</Typography>}
                      </TableCell>
                      <TableCell sx={{ whiteSpace: 'nowrap' }}>
                        {callHref ? <a href={callHref}>{formatPhoneNumber(customer.phoneNumber)}</a> : formatPhoneNumber(customer.phoneNumber) || '-'}
                      </TableCell>
                      <TableCell sx={minorColumn}>{customer.email || '-'}</TableCell>
                      <TableCell sx={{ ...minorColumn, whiteSpace: 'nowrap' }}>{formatDate(customer.registeredAt)}</TableCell>
                      <TableCell>
                        <Chip size="small" label={customer.active ? 'Active' : 'Inactive'} color={customer.active ? 'success' : 'default'} />
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            component="div"
            count={data?.totalElements ?? 0}
            page={data && page >= (data.totalPages || 1) ? 0 : page}
            onPageChange={(_, next) => setPage(next)}
            rowsPerPage={size}
            onRowsPerPageChange={(event) => {
              setSize(Number(event.target.value))
              setPage(0)
            }}
            rowsPerPageOptions={PAGE_SIZES}
          />
        </Paper>
      ) : null}
    </Stack>
  )
}
