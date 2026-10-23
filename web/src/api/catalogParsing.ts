import type { CatalogCategory, CatalogResponse } from '../types/models'

export function isCatalogCategory(value: unknown): value is CatalogCategory {
  if (!value || typeof value !== 'object') return false
  const candidate = value as Partial<CatalogCategory>
  return (
    typeof candidate.id === 'string' &&
    typeof candidate.name === 'string' &&
    typeof candidate.description === 'string' &&
    typeof candidate.serviceType === 'string' &&
    typeof candidate.icon === 'string' &&
    typeof candidate.accent === 'string' &&
    Array.isArray(candidate.services) &&
    Array.isArray(candidate.roles)
  )
}

export function assertCatalogResponse(data: unknown): CatalogResponse {
  if (!data || typeof data !== 'object') {
    throw new Error('Invalid catalog response')
  }
  const candidate = data as Partial<CatalogResponse>
  if (!Array.isArray(candidate.categories) || !candidate.categories.every(isCatalogCategory)) {
    throw new Error('Invalid catalog response')
  }
  return { categories: candidate.categories }
}

export function assertCatalogCategories(data: unknown): CatalogCategory[] {
  if (!Array.isArray(data) || !data.every(isCatalogCategory)) {
    throw new Error('Invalid categories response')
  }
  return data
}

export function assertCatalogCategory(data: unknown): CatalogCategory {
  if (!isCatalogCategory(data)) {
    throw new Error('Invalid category response')
  }
  return data
}

