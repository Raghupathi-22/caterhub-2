import { http } from './http'
import type { Booking, DashboardSummary, EventCampaign, Offer } from '../types/api'

export interface OfferCreateRequest {
  businessId: number
  couponCode: string
  description: string
  discountType: 'PERCENTAGE' | 'FLAT_AMOUNT' | 'FREE_DELIVERY' | 'BUY_ONE_GET_ONE'
  discountValue: number
  minOrderValue?: number
  maxDiscount?: number
  validFrom: string
  validUntil: string
}

export interface EventCreateRequest {
  businessId: number
  campaignName: string
  campaignDescription: string
  campaignType: string
  startDate: string
  endDate: string
  targetAudience?: string
  budget?: number
  status?: 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED'
}

export const adminApi = {
  getDashboardSummary: async (businessId: number): Promise<DashboardSummary> => {
    const response = await http.get<DashboardSummary>('/admin/dashboard', {
      params: { businessId },
    })
    return response.data
  },

  getOrders: async (businessId: number): Promise<Booking[]> => {
    const response = await http.get<Booking[]>('/admin/orders', {
      params: { businessId },
    })
    return response.data
  },

  updateOrderStatus: async (bookingId: number, status: string): Promise<Booking> => {
    const response = await http.put<Booking>(`/admin/orders/${bookingId}/status`, null, {
      params: { status },
    })
    return response.data
  },

  getOffers: async (businessId: number): Promise<Offer[]> => {
    const response = await http.get<Offer[]>('/admin/offers', {
      params: { businessId },
    })
    return response.data
  },

  createOffer: async (payload: OfferCreateRequest): Promise<Offer> => {
    const response = await http.post<Offer>('/admin/offers', payload)
    return response.data
  },

  setOfferActive: async (offerId: number, active: boolean): Promise<Offer> => {
    const response = await http.patch<Offer>(`/admin/offers/${offerId}/active`, null, {
      params: { active },
    })
    return response.data
  },

  getEvents: async (businessId: number): Promise<EventCampaign[]> => {
    const response = await http.get<EventCampaign[]>('/admin/events', {
      params: { businessId },
    })
    return response.data
  },

  createEvent: async (payload: EventCreateRequest): Promise<EventCampaign> => {
    const response = await http.post<EventCampaign>('/admin/events', payload)
    return response.data
  },

  updateEventStatus: async (
    eventId: number,
    status: 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED',
  ): Promise<EventCampaign> => {
    const response = await http.patch<EventCampaign>(`/admin/events/${eventId}/status`, null, {
      params: { status },
    })
    return response.data
  },
}
