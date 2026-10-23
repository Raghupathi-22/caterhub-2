import type { AdminOrderSummary } from '../types/models'

export type OrderTiming = 'ALL' | 'UPCOMING' | 'PAST'

export interface OrderFilters {
  search?: string
  status?: string
  category?: string
  fromDate?: string
  toDate?: string
  timing?: OrderTiming
}

/** Event dates/times are stored as Hyderabad wall-clock values; returns "now" in that zone as `YYYY-MM-DDTHH:mm`. */
export function businessNowKey(now: Date = new Date()): string {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Kolkata',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(now)
  const get = (type: string) => parts.find((part) => part.type === type)?.value ?? '00'
  return `${get('year')}-${get('month')}-${get('day')}T${get('hour')}:${get('minute')}`
}

export function eventStartKey(row: Pick<AdminOrderSummary, 'eventDate' | 'startTime'>): string {
  const date = (row.eventDate || '').slice(0, 10)
  const time = (row.startTime || '00:00').slice(0, 5)
  return `${date}T${time}`
}

/** An event is upcoming only while its start is strictly after now (IST). */
export function isUpcomingOrder(row: Pick<AdminOrderSummary, 'eventDate' | 'startTime'>, nowKey: string): boolean {
  return Boolean(row.eventDate) && eventStartKey(row) > nowKey
}

export function orderCategory(row: Pick<AdminOrderSummary, 'orderType' | 'serviceCategory' | 'serviceType'>): string {
  if (row.orderType === 'CATERING_ORDER') return 'Catering'
  return row.serviceCategory || row.serviceType || 'Service'
}

export function orderTypeLabel(orderType: AdminOrderSummary['orderType']): string {
  if (orderType === 'CATERING_ORDER') return 'Catering order'
  if (orderType === 'STAFFING_JOB') return 'Worker job'
  return 'Service request'
}

/** "7 accepted / 10 requested" when the requested count is known, otherwise "7 accepted". */
export function acceptanceLabel(accepted?: number | null, requested?: number | null): string {
  const acceptedCount = Math.max(0, accepted ?? 0)
  return requested && requested > 0 ? `${acceptedCount} accepted / ${requested} requested` : `${acceptedCount} accepted`
}

export function filterOrders(rows: AdminOrderSummary[], filters: OrderFilters, nowKey: string): AdminOrderSummary[] {
  const search = filters.search?.trim().toLowerCase().replace(/^#/, '') ?? ''
  const searchDigits = search.replace(/\D/g, '')
  return rows.filter((row) => {
    if (filters.status && row.status !== filters.status) return false
    if (filters.category && orderCategory(row) !== filters.category) return false
    const eventDate = (row.eventDate || '').slice(0, 10)
    if (filters.fromDate && (!eventDate || eventDate < filters.fromDate)) return false
    if (filters.toDate && (!eventDate || eventDate > filters.toDate)) return false
    if (filters.timing === 'UPCOMING' && !isUpcomingOrder(row, nowKey)) return false
    if (filters.timing === 'PAST' && isUpcomingOrder(row, nowKey)) return false
    if (!search) return true
    if (String(row.id) === search || row.reference?.toLowerCase().includes(search)) return true
    if (row.customer?.name?.toLowerCase().includes(search)) return true
    if (searchDigits.length >= 3 && row.customer?.mobileNumber?.replace(/\D/g, '').includes(searchDigits)) return true
    return false
  })
}
