export interface User {
  id: number
  username: string
  email: string
  phone_number: string
  first_name?: string
  last_name?: string
  business_id?: number
}

export interface AuthResponse {
  access_token: string
  refresh_token: string
  token_type: string
  expires_in: number
  user: User
}

export interface Booking {
  id: number
  bookingReference?: string
  businessId: number
  userId: number
  eventType: string
  guestCount: number
  mealType: string
  eventDateTime: string
  deliveryAddress: string
  specialInstructions?: string
  totalAmount: number
  status: string
  paymentStatus?: string
  createdAt: string
}

export interface DashboardSummary {
  totalOrders: number
  pendingOrders: number
  deliveredOrders: number
  cancelledOrders: number
  totalRevenue: number
  averageOrderValue: number
}

export interface Offer {
  id: number
  businessId: number
  couponCode: string
  description: string
  discountType: 'PERCENTAGE' | 'FLAT_AMOUNT' | 'FREE_DELIVERY' | 'BUY_ONE_GET_ONE'
  discountValue: number
  minOrderValue?: number
  maxDiscount?: number
  validFrom: string
  validUntil: string
  isActive: boolean
}

export interface EventCampaign {
  id: number
  businessId: number
  campaignName: string
  campaignDescription: string
  campaignType: string
  startDate: string
  endDate: string
  targetAudience?: string
  budget?: number
  spent?: number
  status: 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED'
}

export interface ServiceRequest {
  id: number
  serviceType: string
  eventType: string
  eventDate: string
  startTime: string
  location: string
  area: string
  details?: string
  totalAmount: number
  status: string
  createdAt?: string
}

export interface StaffingRequest {
  id: number
  eventType: string
  workerType: string
  eventDate: string
  startTime: string
  endTime: string
  location: string
  area: string
  requiredWorkers: number
  acceptedWorkers: number
  remainingPositions: number
  payment: number
  additionalRequirements?: string
  status: string
  createdAt?: string
}
