import { http } from './http'
import type { AdminDashboardSummary, AdminEvent, AdminOffer, BookingDTO, ServiceRequestDTO, StaffingJob, WorkerProfile } from '../types/models'

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
}

export const adminApi = {
  getDashboardSummary: async (businessId: number): Promise<AdminDashboardSummary> => {
    const response = await http.get<AdminDashboardSummary>('/admin/dashboard', { params: { businessId } })
    return response.data
  },
  getOrders: async (businessId: number): Promise<BookingDTO[]> => {
    const response = await http.get<BookingDTO[]>('/admin/orders', { params: { businessId } })
    return response.data
  },
  updateOrderStatus: async (bookingId: number, status: string): Promise<BookingDTO> => {
    const response = await http.put<BookingDTO>(`/admin/orders/${bookingId}/status`, null, { params: { status } })
    return response.data
  },
  getOffers: async (businessId: number): Promise<AdminOffer[]> => {
    const response = await http.get<AdminOffer[]>('/admin/offers', { params: { businessId } })
    return response.data
  },
  createOffer: async (payload: OfferCreateRequest): Promise<AdminOffer> => {
    const response = await http.post<AdminOffer>('/admin/offers', payload)
    return response.data
  },
  setOfferActive: async (offerId: number, active: boolean): Promise<AdminOffer> => {
    const response = await http.patch<AdminOffer>(`/admin/offers/${offerId}/active`, null, { params: { active } })
    return response.data
  },
  getEvents: async (businessId: number): Promise<AdminEvent[]> => {
    const response = await http.get<AdminEvent[]>('/admin/events', { params: { businessId } })
    return response.data
  },
  createEvent: async (payload: EventCreateRequest): Promise<AdminEvent> => {
    const response = await http.post<AdminEvent>('/admin/events', payload)
    return response.data
  },
  updateEventStatus: async (eventId: number, status: AdminEvent['status']): Promise<AdminEvent> => {
    const response = await http.patch<AdminEvent>(`/admin/events/${eventId}/status`, null, { params: { status } })
    return response.data
  },
  getServiceRequests: async (): Promise<ServiceRequestDTO[]> => {
    const response = await http.get<ServiceRequestDTO[]>('/admin/service-requests')
    return response.data
  },
  getStaffingRequests: async (): Promise<StaffingJob[]> => {
    const response = await http.get<StaffingJob[]>('/admin/staffing-requests')
    return response.data
  },
  updateStaffingRequestStatus: async (requestId: number, status: string): Promise<StaffingJob> => {
    const response = await http.patch<StaffingJob>(`/admin/staffing-requests/${requestId}/status`, null, { params: { status } })
    return response.data
  },
  getWorkerProfiles: async (status?: string): Promise<WorkerProfile[]> => {
    const response = await http.get<WorkerProfile[]>('/workers/profiles', { params: { status } })
    return response.data
  },
  getWorkerProfile: async (profileId: number): Promise<WorkerProfile> => {
    const response = await http.get<WorkerProfile>(`/workers/profiles/${profileId}`)
    return response.data
  },
  updateWorkerStatus: async (profileId: number, payload: { status: string; adminUserId?: number; rejectionReason?: string }): Promise<WorkerProfile> => {
    const response = await http.patch<WorkerProfile>(`/workers/profiles/${profileId}/status`, payload)
    return response.data
  },
}
