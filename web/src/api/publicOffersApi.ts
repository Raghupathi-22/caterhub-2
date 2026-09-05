import { http } from './http'

export interface PublicOffer {
  id: number
  title: string
  description: string
  applicableCategory?: string | null
  validFrom: string
  validUntil: string
  ctaLabel: string
}

export const publicOffersApi = {
  getActiveOffers: async (): Promise<PublicOffer[]> => {
    const response = await http.get<PublicOffer[]>('/offers/active')
    return response.data
  },
}

