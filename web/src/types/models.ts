export interface User {
  id: number
  username: string
  email: string | null
  phone_number: string
  first_name: string | null
  last_name: string | null
  profile_image_url?: string | null
  is_active: boolean
  is_verified: boolean
  business_id?: number | null
  roles: string[]
}

export interface AuthResponse {
  access_token: string
  refresh_token: string
  token_type: string
  expires_in: number
  user: User
}

export interface CatalogRole {
  id: string
  title: string
  workerType: string
  skills: string[]
}

export interface CatalogCategory {
  id: string
  name: string
  description: string
  serviceType: string
  icon: string
  accent: string
  services: string[]
  roles: CatalogRole[]
}

export interface CatalogResponse {
  categories: CatalogCategory[]
}

export interface BookingDTO {
  id: number
  bookingReference?: string
  eventType: string
  guestCount: number
  mealType: string
  eventDateTime: string
  deliveryAddress: string
  specialInstructions?: string
  totalAmount: number
  status: string
  createdAt?: string
}

export interface ServiceRequestDTO {
  id: number
  serviceType: string
  eventType: string
  eventDate: string
  startTime: string
  endTime: string
  location: string
  area: string
  selectedServices: string[]
  instructions?: string
  details?: string
  quoteBased: boolean
  totalAmount: number
  status: string
  createdAt?: string
}

export interface WorkerProfile {
  id: number
  userId: number
  username: string
  fullName: string
  workerType: string
  status: string
  experienceYears: number
  skills?: string
  preferredAreas?: string
  languages?: string
  bio?: string
  rejectionReason?: string
}

export interface WorkerDashboard {
  profile?: WorkerProfile
  profileCompletionPercent: number
  availableForWork: boolean
  nearbyOpportunities: StaffingJob[]
  myJobs: WorkerJob[]
}

export interface StaffingJob {
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
  alreadyAccepted: boolean
}

export interface WorkerJob {
  acceptanceId: number
  jobId: number
  eventType: string
  workerType: string
  eventDate: string
  startTime: string
  endTime: string
  location: string
  area: string
  payment: number
  status: string
}

export interface AdminDashboardSummary {
  totalOrders: number
  pendingOrders: number
  deliveredOrders: number
  cancelledOrders: number
  totalRevenue: number
  averageOrderValue: number
}

export interface AdminOffer {
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

export interface AdminEvent {
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
