import { Chip } from '@mui/material'

const statusColorMap: Record<string, 'default' | 'success' | 'warning' | 'error' | 'info'> = {
  PENDING: 'warning',
  CONFIRMED: 'info',
  PREPARING: 'info',
  READY: 'info',
  DELIVERED: 'success',
  CANCELLED: 'error',
  ACTIVE: 'success',
  DRAFT: 'default',
  PAUSED: 'warning',
  COMPLETED: 'success',
}

interface StatusChipProps {
  status: string
}

export function StatusChip({ status }: StatusChipProps) {
  return <Chip size="small" color={statusColorMap[status] ?? 'default'} label={status} />
}
