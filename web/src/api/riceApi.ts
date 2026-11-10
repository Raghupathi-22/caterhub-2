import { http } from './http'
import { compactParams } from '../utils/riceOrders'
import type {
  RiceOrder,
  RiceOrderStatus,
  RicePaymentStatus,
  RiceProduct,
  RiceProductUpdate,
  RiceSample,
  RiceSampleStatus,
} from '../types/models'

export interface RiceOrderFilters {
  status?: RiceOrderStatus | ''
  paymentStatus?: RicePaymentStatus | ''
  from?: string
  to?: string
  search?: string
}

export interface RiceSampleFilters {
  status?: RiceSampleStatus | ''
  from?: string
  to?: string
  search?: string
}

export interface RiceOrderUpdate {
  status?: RiceOrderStatus
  paymentStatus?: RicePaymentStatus
  adminNotes?: string
}

export interface RiceSampleUpdate {
  status?: RiceSampleStatus
  pickupLocation?: string
  pickupTime?: string
  pickupInstructions?: string
  adminNotes?: string
}

export const riceAdminApi = {
  getOrders: async (filters: RiceOrderFilters): Promise<RiceOrder[]> =>
    (await http.get<RiceOrder[]>('/admin/rice/orders', { params: compactParams(filters) })).data,
  updateOrder: async (id: number, payload: RiceOrderUpdate): Promise<RiceOrder> =>
    (await http.patch<RiceOrder>(`/admin/rice/orders/${id}/status`, payload)).data,
  getSamples: async (filters: RiceSampleFilters): Promise<RiceSample[]> =>
    (await http.get<RiceSample[]>('/admin/rice/samples', { params: compactParams(filters) })).data,
  updateSample: async (id: number, payload: RiceSampleUpdate): Promise<RiceSample> =>
    (await http.patch<RiceSample>(`/admin/rice/samples/${id}`, payload)).data,
  getProduct: async (): Promise<RiceProduct> => (await http.get<RiceProduct>('/admin/rice/product')).data,
  updateProduct: async (payload: RiceProductUpdate): Promise<RiceProduct> =>
    (await http.put<RiceProduct>('/admin/rice/product', payload)).data,
}
