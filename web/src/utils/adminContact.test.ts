import { describe, expect, it } from 'vitest'
import { assertCatalogResponse } from '../api/catalogParsing'
import { buildCustomerQueryParams, catalogSummary, customerDisplayName, formatDate, formatPhoneNumber, telHref } from './adminContact'

// Mirrors the JSON produced by backend GET /api/v1/catalog (CatalogController.CatalogResponse).
const backendCatalog = {
  categories: [
    {
      id: 'catering',
      name: 'Catering & Food',
      description: 'Food and catering services',
      serviceType: 'CATERING',
      icon: 'restaurant',
      accent: '#FF6B35',
      services: ['Veg Catering', 'Non-Veg Catering'],
      roles: [{ id: 'chef', title: 'Chef', workerType: 'CHEF', skills: ['South Indian'] }],
    },
    {
      id: 'decoration',
      name: 'Decoration',
      description: 'Event decoration',
      serviceType: 'DECORATION',
      icon: 'celebration',
      accent: '#08752D',
      services: ['Stage Decoration'],
      roles: [],
    },
  ],
}

describe('catalog response parsing', () => {
  it('accepts the backend catalogue shape and keeps every category', () => {
    const parsed = assertCatalogResponse(backendCatalog)
    expect(parsed.categories.map((c) => c.id)).toEqual(['catering', 'decoration'])
    expect(catalogSummary(parsed.categories)).toEqual({ categories: 2, services: 3, roles: 1 })
  })

  it('accepts an empty catalogue so the page can show an explicit empty state', () => {
    expect(assertCatalogResponse({ categories: [] }).categories).toEqual([])
  })

  it('rejects unexpected response formats instead of rendering an empty page', () => {
    expect(() => assertCatalogResponse(backendCatalog.categories)).toThrow('Invalid catalog response')
    expect(() => assertCatalogResponse({ data: backendCatalog })).toThrow('Invalid catalog response')
    expect(() => assertCatalogResponse('<!doctype html>')).toThrow('Invalid catalog response')
    expect(() => assertCatalogResponse({ categories: [{ id: 'x' }] })).toThrow('Invalid catalog response')
  })
})

describe('customer query params', () => {
  it('uses defaults and omits blank search', () => {
    expect(buildCustomerQueryParams({})).toEqual({ page: 0, size: 20 })
    expect(buildCustomerQueryParams({ search: '   ' })).toEqual({ page: 0, size: 20 })
  })

  it('clamps paging and trims search', () => {
    expect(buildCustomerQueryParams({ page: -2, size: 500, search: '  98765 ' })).toEqual({ page: 0, size: 100, search: '98765' })
    expect(buildCustomerQueryParams({ page: 3, size: 0 })).toEqual({ page: 3, size: 1 })
  })
})

describe('contact display helpers', () => {
  it('never fabricates a customer name', () => {
    expect(customerDisplayName({ fullName: 'Asha Rao' })).toBe('Asha Rao')
    expect(customerDisplayName({ fullName: '  ' })).toBeNull()
    expect(customerDisplayName({ fullName: null })).toBeNull()
  })

  it('formats stored mobile numbers', () => {
    expect(formatPhoneNumber('+919876543210')).toBe('+91 98765 43210')
    expect(formatPhoneNumber('9876543210')).toBe('98765 43210')
    expect(formatPhoneNumber('+1 555 0100')).toBe('+1 555 0100')
    expect(formatPhoneNumber(undefined)).toBe('')
  })

  it('builds tel links only for dialable numbers', () => {
    expect(telHref('+91 98765 43210')).toBe('tel:+919876543210')
    expect(telHref('')).toBeNull()
    expect(telHref('n/a')).toBeNull()
  })

  it('formats dates defensively', () => {
    expect(formatDate(null)).toBe('-')
    expect(formatDate('not-a-date')).toBe('-')
    expect(formatDate('2024-05-01T10:00:00')).toMatch(/2024/)
  })
})
