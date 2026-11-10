import type { RiceOrder, RiceOrderStatus, RicePaymentStatus, RiceProduct, RiceProductUpdate, RiceSample, RiceSampleStatus } from '../types/models'

export const RICE_ORDER_STATUSES: RiceOrderStatus[] = ['PENDING', 'CONFIRMED', 'PROCESSING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED']
export const RICE_PAYMENT_STATUSES: RicePaymentStatus[] = ['PENDING', 'PAID', 'FAILED', 'REFUNDED']
export const RICE_SAMPLE_STATUSES: RiceSampleStatus[] = ['REQUESTED', 'PICKUP_SCHEDULED', 'COLLECTED', 'CANCELLED']

const ORDER_TERMINAL: RiceOrderStatus[] = ['DELIVERED', 'CANCELLED']
const SAMPLE_TERMINAL: RiceSampleStatus[] = ['COLLECTED', 'CANCELLED']

/** Drops empty filter values so the backend only receives meaningful query params. */
export function compactParams<T extends object>(filters: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(filters).filter(([, value]) => value !== undefined && value !== null && String(value).trim() !== ''),
  ) as Partial<T>
}

export function formatInr(amount: number | null | undefined): string {
  if (amount === null || amount === undefined || Number.isNaN(Number(amount))) return '—'
  const value = Number(amount)
  return `₹${value.toLocaleString('en-IN', { minimumFractionDigits: Number.isInteger(value) ? 0 : 2, maximumFractionDigits: 2 })}`
}

export function formatQuantity(kg: number): string {
  return `${kg.toLocaleString('en-IN')} kg`
}

export function formatSampleWeight(grams: number): string {
  return grams >= 1000 && grams % 1000 === 0 ? `${grams / 1000} kg` : `${grams} g`
}

export function formatDateTime(value?: string | null): string {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

export function humanize(value?: string | null): string {
  if (!value) return '—'
  return value.replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase())
}

export function isOrderLocked(order: Pick<RiceOrder, 'status'>): boolean {
  return ORDER_TERMINAL.includes(order.status)
}

export function isSampleLocked(sample: Pick<RiceSample, 'status'>): boolean {
  return SAMPLE_TERMINAL.includes(sample.status)
}

/** Payment statuses the admin may pick; REFUNDED is only valid once an order has been paid. */
export function allowedPaymentStatuses(current: RicePaymentStatus): RicePaymentStatus[] {
  return RICE_PAYMENT_STATUSES.filter((status) => status !== 'REFUNDED' || current === 'PAID' || current === 'REFUNDED')
}

/** Sample statuses the admin may pick; a sample can never move back to REQUESTED. */
export function allowedSampleStatuses(current: RiceSampleStatus): RiceSampleStatus[] {
  return RICE_SAMPLE_STATUSES.filter((status) => status !== 'REQUESTED' || current === 'REQUESTED')
}

/** Mirrors the backend rule: scheduling a pickup needs a location and a collection time. */
export function validateSampleSchedule(status: RiceSampleStatus, pickupLocation: string, pickupTime: string): string | null {
  if (status !== 'PICKUP_SCHEDULED') return null
  if (!pickupLocation.trim()) return 'Enter the pickup location.'
  if (!pickupTime) return 'Choose the collection date and time.'
  return null
}

/** Converts a datetime-local value (yyyy-MM-ddTHH:mm) to the ISO LocalDateTime the backend expects. */
export function toLocalDateTime(value: string): string | undefined {
  if (!value) return undefined
  return value.length === 16 ? `${value}:00` : value
}

/** Converts a backend LocalDateTime into a datetime-local input value. */
export function toDateTimeInput(value?: string | null): string {
  return value ? value.slice(0, 16) : ''
}

export function deliverySummary(order: Pick<RiceOrder, 'deliveryAddress' | 'area' | 'pincode' | 'preferredDeliveryDate' | 'preferredDeliverySlot'>): string {
  const when = [order.preferredDeliveryDate ?? 'Any date', order.preferredDeliverySlot ? humanize(order.preferredDeliverySlot) : null]
    .filter(Boolean)
    .join(' · ')
  return `${order.deliveryAddress}, ${order.area} - ${order.pincode} (${when})`
}

export function productToForm(product: RiceProduct): RiceProductUpdate {
  return {
    name: product.name,
    tagline: product.tagline ?? '',
    description: product.description ?? '',
    highlights: product.highlights ?? [],
    imageUrl: product.imageUrl ?? '',
    active: product.active,
    basePackKg: product.basePackKg,
    basePackPrice: product.basePackPrice,
    minOrderKg: product.minOrderKg,
    orderStepKg: product.orderStepKg,
    maxOrderKg: product.maxOrderKg,
    deliveryCharge: product.deliveryCharge ?? null,
    paymentNote: product.paymentNote ?? '',
    sampleEnabled: product.sampleEnabled,
    sampleWeightGrams: product.sampleWeightGrams,
    samplePickupLocation: product.samplePickupLocation ?? '',
    samplePickupInstructions: product.samplePickupInstructions ?? '',
  }
}

export function validateProductForm(form: RiceProductUpdate): string | null {
  if (!form.name.trim()) return 'Product name is required.'
  if (!(form.basePackKg >= 1)) return 'Base pack size must be at least 1 kg.'
  if (!(form.basePackPrice >= 1)) return 'Base pack price must be at least ₹1.'
  if (!(form.minOrderKg >= 1)) return 'Minimum order must be at least 1 kg.'
  if (!(form.orderStepKg >= 1)) return 'Order step must be at least 1 kg.'
  if (!(form.maxOrderKg >= form.minOrderKg)) return 'Maximum order must be at least the minimum order.'
  if (form.deliveryCharge !== null && form.deliveryCharge !== undefined && form.deliveryCharge < 0) return 'Delivery charge cannot be negative.'
  if (!(form.sampleWeightGrams >= 1)) return 'Sample weight must be at least 1 gram.'
  if (form.imageUrl && !/^https:\/\/\S+$/.test(form.imageUrl)) return 'Image URL must start with https://'
  if (form.highlights.length > 10) return 'Use at most 10 highlights.'
  return null
}
