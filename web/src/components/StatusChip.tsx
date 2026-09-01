import { Chip } from '@mui/material'

const statusColorMap: Record<string, 'default' | 'success' | 'warning' | 'error' | 'info'> = {
  PENDING: 'warning',
  CONFIRMED: 'info',
  ASSIGNED: 'info',
  PREPARING: 'info',
  READY: 'info',
  DELIVERED: 'success',
  COMPLETED: 'success',
  ACTIVE: 'success',
  CANCELLED: 'error',
  REJECTED: 'error',
  DRAFT: 'default',
  IN_PROGRESS: 'warning',
}

export function StatusChip({ status }: { status: string }) {
  return <Chip size="small" color={statusColorMap[status] ?? 'default'} label={status.replace(/_/g, ' ')} />
}
