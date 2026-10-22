import { http } from './http'
import type { CatalogCategory, CatalogResponse, PublicOffer } from '../types/models'
import { assertCatalogCategories, assertCatalogCategory, assertCatalogResponse } from './catalogParsing'

function isPublicOffer(value: unknown): value is PublicOffer {
  if (!value || typeof value !== 'object') return false
  const candidate = value as Partial<PublicOffer>
  return (
    typeof candidate.id === 'number' &&
    typeof candidate.couponCode === 'string' &&
    typeof candidate.description === 'string' &&
    typeof candidate.discountType === 'string' &&
    typeof candidate.discountValue === 'number' &&
    typeof candidate.validFrom === 'string' &&
    typeof candidate.validUntil === 'string'
  )
}

function assertPublicOffers(data: unknown): PublicOffer[] {
  if (!Array.isArray(data) || !data.every(isPublicOffer)) {
    throw new Error('Invalid offers response')
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
  getPublicOffers: async (): Promise<PublicOffer[]> => {
    const response = await http.get('/catalog/offers')
    return assertPublicOffers(response.data)
  },
}
