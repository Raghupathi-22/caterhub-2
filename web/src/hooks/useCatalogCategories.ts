import { useQuery } from '@tanstack/react-query'
import { catalogApi } from '../api/catalogApi'

export function useCatalogCategories() {
  return useQuery({
    queryKey: ['catalog', 'categories'],
    queryFn: catalogApi.getCategories,
    staleTime: 1000 * 60 * 5,
  })
}

