import { http } from './http'
import type { CatalogCategory, CatalogResponse } from '../types/models'

function isCatalogCategory(value: unknown): value is CatalogCategory {
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

function assertCatalogResponse(data: unknown): CatalogResponse {
  if (!data || typeof data !== 'object') {
    throw new Error('Invalid catalog response')
  }
  const candidate = data as Partial<CatalogResponse>
  if (!Array.isArray(candidate.categories) || !candidate.categories.every(isCatalogCategory)) {
    throw new Error('Invalid catalog response')
  }
  return { categories: candidate.categories }
}

function assertCatalogCategories(data: unknown): CatalogCategory[] {
  if (!Array.isArray(data) || !data.every(isCatalogCategory)) {
    throw new Error('Invalid categories response')
  }
  return data
}

function assertCatalogCategory(data: unknown): CatalogCategory {
  if (!isCatalogCategory(data)) {
    throw new Error('Invalid category response')
  }
  return data
}

export const catalogApi = {
  getCatalog: async (): Promise<CatalogResponse> => {
    const response = await http.get('/catalog')
    return assertCatalogResponse(response.data)
  },
  getCategories: async (): Promise<CatalogCategory[]> => {
    const response = await http.get('/catalog/categories')
    return assertCatalogCategories(response.data)
  },
  getCategory: async (categoryId: string): Promise<CatalogCategory> => {
    const response = await http.get(`/catalog/categories/${categoryId}`)
    return assertCatalogCategory(response.data)
  },
}
