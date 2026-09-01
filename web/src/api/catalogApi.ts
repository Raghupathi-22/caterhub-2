import { http } from './http'
import type { CatalogCategory, CatalogResponse } from '../types/models'

export const catalogApi = {
  getCatalog: async (): Promise<CatalogResponse> => {
    const response = await http.get<CatalogResponse>('/catalog')
    return response.data
  },
  getCategories: async (): Promise<CatalogCategory[]> => {
    const response = await http.get<CatalogCategory[]>('/catalog/categories')
    return response.data
  },
  getCategory: async (categoryId: string): Promise<CatalogCategory> => {
    const response = await http.get<CatalogCategory>(`/catalog/categories/${categoryId}`)
    return response.data
  },
}
