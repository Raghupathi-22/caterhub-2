import type { AdminCustomer, CatalogCategory } from '../types/models'

export interface CustomerQuery {
  page?: number
  size?: number
  search?: string
}

/** Builds query params for GET /admin/customers, omitting blank search terms. */
export function buildCustomerQueryParams(query: CustomerQuery): Record<string, string | number> {
  const params: Record<string, string | number> = {
    page: Math.max(0, Math.floor(query.page ?? 0)),
    size: Math.min(100, Math.max(1, Math.floor(query.size ?? 20))),
  }
  const search = query.search?.trim()
  if (search) params.search = search.slice(0, 100)
  return params
}

/** Returns the customer's real name, or null so the UI can show an explicit placeholder instead of inventing one. */
export function customerDisplayName(customer: Pick<AdminCustomer, 'fullName'>): string | null {
  const name = customer.fullName?.trim()
  return name ? name : null
}

/** Formats stored Indian mobile numbers (+91XXXXXXXXXX or 10 digits) for display; other values are shown as stored. */
export function formatPhoneNumber(phone?: string | null): string {
  if (!phone) return ''
  const trimmed = phone.trim()
  const digits = trimmed.replace(/\D/g, '')
  if (digits.length === 12 && digits.startsWith('91')) return `+91 ${digits.slice(2, 7)} ${digits.slice(7)}`
  if (digits.length === 10) return `${digits.slice(0, 5)} ${digits.slice(5)}`
  return trimmed
}

/** Builds a tel: URI from a stored phone number, or null when there is nothing dialable. */
export function telHref(phone?: string | null): string | null {
  if (!phone) return null
  const cleaned = phone.trim().replace(/[^\d+]/g, '')
  return /\d{6,}/.test(cleaned) ? `tel:${cleaned}` : null
}

export function formatDate(value?: string | null): string {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
}

export function catalogSummary(categories: CatalogCategory[]): { categories: number; services: number; roles: number } {
  return {
    categories: categories.length,
    services: categories.reduce((total, category) => total + category.services.length, 0),
    roles: categories.reduce((total, category) => total + category.roles.length, 0),
  }
}
