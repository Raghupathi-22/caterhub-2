import { describe, expect, it } from 'vitest'
import type { AdminOrderSummary } from '../types/models'
import { acceptanceLabel, businessNowKey, filterOrders, isUpcomingOrder, orderCategory } from './adminOrders'

const row = (overrides: Partial<AdminOrderSummary>): AdminOrderSummary => ({
  id: 1,
  orderType: 'SERVICE_REQUEST',
  reference: 'SR-1',
  eventType: 'Wedding',
  eventDate: '2026-10-25',
  startTime: '18:00:00',
  bookedAt: '2026-10-01T10:00:00',
  area: 'Madhapur',
  totalAmount: 0,
  status: 'PENDING',
  acceptedWorkerCount: 0,
  customer: { id: 9, name: 'Asha Rao', username: 'asha', mobileNumber: '+919876543210' },
  ...overrides,
})

describe('adminOrders', () => {
  it('computes IST now regardless of the browser zone', () => {
    // 2026-10-21T20:00Z is 01:30 on 22 Oct in Hyderabad
    expect(businessNowKey(new Date('2026-10-21T20:00:00Z'))).toBe('2026-10-22T01:30')
  })

  it('treats events starting at or before now as past', () => {
    const now = '2026-10-22T10:00'
    expect(isUpcomingOrder(row({ eventDate: '2026-10-22', startTime: '10:00:00' }), now)).toBe(false)
    expect(isUpcomingOrder(row({ eventDate: '2026-10-22', startTime: '10:30:00' }), now)).toBe(true)
    expect(isUpcomingOrder(row({ eventDate: '2026-09-27', startTime: null }), now)).toBe(false)
  })

  it('formats accepted vs requested counts', () => {
    expect(acceptanceLabel(7, 10)).toBe('7 accepted / 10 requested')
    expect(acceptanceLabel(2, null)).toBe('2 accepted')
    expect(acceptanceLabel(undefined, 0)).toBe('0 accepted')
  })

  it('derives categories for each order type', () => {
    expect(orderCategory(row({ orderType: 'CATERING_ORDER' }))).toBe('Catering')
    expect(orderCategory(row({ serviceCategory: 'Decoration', serviceType: 'DECORATION' }))).toBe('Decoration')
    expect(orderCategory(row({ orderType: 'STAFFING_JOB', serviceCategory: null, serviceType: 'DJ' }))).toBe('DJ')
  })

  it('filters by id, phone, status, category, date range and timing', () => {
    const rows = [
      row({ id: 1, reference: 'SR-1', eventDate: '2026-09-27', status: 'COMPLETED', serviceCategory: 'Decoration' }),
      row({ id: 22, reference: 'BK-22', orderType: 'CATERING_ORDER', eventDate: '2026-11-02' }),
      row({ id: 3, reference: 'JOB-3', orderType: 'STAFFING_JOB', eventDate: '2026-12-01', customer: null }),
    ]
    const now = '2026-10-22T10:00'
    expect(filterOrders(rows, { search: '#22' }, now).map((r) => r.id)).toEqual([22])
    expect(filterOrders(rows, { search: 'job-3' }, now).map((r) => r.id)).toEqual([3])
    expect(filterOrders(rows, { search: '98765' }, now).map((r) => r.id)).toEqual([1, 22])
    expect(filterOrders(rows, { status: 'COMPLETED' }, now).map((r) => r.id)).toEqual([1])
    expect(filterOrders(rows, { category: 'Catering' }, now).map((r) => r.id)).toEqual([22])
    expect(filterOrders(rows, { fromDate: '2026-10-01', toDate: '2026-11-30' }, now).map((r) => r.id)).toEqual([22])
    expect(filterOrders(rows, { timing: 'UPCOMING' }, now).map((r) => r.id)).toEqual([22, 3])
    expect(filterOrders(rows, { timing: 'PAST' }, now).map((r) => r.id)).toEqual([1])
  })
})
