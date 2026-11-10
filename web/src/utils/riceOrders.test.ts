import { describe, expect, it } from 'vitest'
import type { RiceProduct } from '../types/models'
import {
  allowedPaymentStatuses,
  compactParams,
  allowedSampleStatuses,
  deliverySummary,
  formatInr,
  formatSampleWeight,
  isOrderLocked,
  isSampleLocked,
  productToForm,
  toDateTimeInput,
  toLocalDateTime,
  validateProductForm,
  validateSampleSchedule,
} from './riceOrders'

const product: RiceProduct = {
  id: 1,
  code: 'VILLAGE_FARM_RICE',
  name: 'Village Farm Rice',
  highlights: ['Village-grown'],
  active: true,
  basePackKg: 50,
  basePackPrice: 5299,
  minOrderKg: 50,
  orderStepKg: 1,
  maxOrderKg: 5000,
  deliveryCharge: null,
  sampleEnabled: true,
  sampleWeightGrams: 500,
  samplePrice: 0,
  sampleFulfillmentType: 'PICKUP',
}

describe('riceOrders utils', () => {
  it('formats rupees and sample weight', () => {
    expect(formatInr(5299)).toBe('₹5,299')
    expect(formatInr(7948.5)).toBe('₹7,948.50')
    expect(formatInr(null)).toBe('—')
    expect(formatSampleWeight(500)).toBe('500 g')
    expect(formatSampleWeight(1000)).toBe('1 kg')
  })

  it('locks terminal orders and samples', () => {
    expect(isOrderLocked({ status: 'DELIVERED' })).toBe(true)
    expect(isOrderLocked({ status: 'PROCESSING' })).toBe(false)
    expect(isSampleLocked({ status: 'COLLECTED' })).toBe(true)
    expect(isSampleLocked({ status: 'PICKUP_SCHEDULED' })).toBe(false)
  })

  it('only allows refund for paid orders', () => {
    expect(allowedPaymentStatuses('PENDING')).not.toContain('REFUNDED')
    expect(allowedPaymentStatuses('PAID')).toContain('REFUNDED')
  })

  it('never lets a scheduled sample go back to requested', () => {
    expect(allowedSampleStatuses('REQUESTED')).toContain('REQUESTED')
    expect(allowedSampleStatuses('PICKUP_SCHEDULED')).not.toContain('REQUESTED')
  })

  it('requires pickup location and time to schedule a sample', () => {
    expect(validateSampleSchedule('PICKUP_SCHEDULED', '', '2026-11-12T10:00')).toBe('Enter the pickup location.')
    expect(validateSampleSchedule('PICKUP_SCHEDULED', 'Farm shop', '')).toBe('Choose the collection date and time.')
    expect(validateSampleSchedule('PICKUP_SCHEDULED', 'Farm shop', '2026-11-12T10:00')).toBeNull()
    expect(validateSampleSchedule('CANCELLED', '', '')).toBeNull()
  })

  it('converts datetime-local values', () => {
    expect(toLocalDateTime('2026-11-12T10:30')).toBe('2026-11-12T10:30:00')
    expect(toLocalDateTime('')).toBeUndefined()
    expect(toDateTimeInput('2026-11-12T10:30:00')).toBe('2026-11-12T10:30')
  })

  it('summarises delivery details', () => {
    expect(
      deliverySummary({ deliveryAddress: '12 Main Rd', area: 'Anna Nagar', pincode: '600040', preferredDeliveryDate: null, preferredDeliverySlot: 'MORNING' }),
    ).toBe('12 Main Rd, Anna Nagar - 600040 (Any date · Morning)')
  })

  it('validates product settings', () => {
    const form = productToForm(product)
    expect(validateProductForm(form)).toBeNull()
    expect(validateProductForm({ ...form, maxOrderKg: 10 })).toBe('Maximum order must be at least the minimum order.')
    expect(validateProductForm({ ...form, imageUrl: 'http://x' })).toBe('Image URL must start with https://')
    expect(validateProductForm({ ...form, basePackPrice: 0 })).toBe('Base pack price must be at least ₹1.')
  })

  it('drops empty filter params', () => {
    expect(compactParams({ status: '', search: ' ', from: '2026-11-01', paymentStatus: undefined })).toEqual({ from: '2026-11-01' })
  })
})
