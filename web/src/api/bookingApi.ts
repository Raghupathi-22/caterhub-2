import { http } from './http'
import type { BookingDTO, ServiceRequestDTO } from '../types/models'

export interface CreateUnifiedBookingInput {
  categoryId: string
  serviceType: string
  selectedServices: string[]
  eventType: string
  eventDate: string
  startTime: string
  endTime: string
  guestCount?: number
  location: string
  area: string
  instructions?: string
  totalAmount: number
}

function combineDateTime(date: string, time: string): string {
  return `${date}T${time}:00`
}

export const bookingApi = {
  createBooking: async (input: CreateUnifiedBookingInput): Promise<void> => {
    if (input.serviceType === 'CATERING_FOOD') {
      await http.post('/bookings/me', {
        eventType: input.eventType,
        guestCount: input.guestCount ?? 1,
        mealType: input.selectedServices[0] ?? 'Meals',
        eventDateTime: combineDateTime(input.eventDate, input.startTime),
        deliveryAddress: `${input.location}, ${input.area}`,
        specialInstructions: [
          `Category: ${input.categoryId}`,
          `Services: ${input.selectedServices.join(', ')}`,
          `Time: ${input.startTime} - ${input.endTime}`,
          input.instructions?.trim(),
        ]
          .filter(Boolean)
          .join('\n'),
        estimatedAmount: input.totalAmount,
      })
      return
    }

    await http.post('/service-requests', {
      serviceType: input.serviceType,
      eventType: input.eventType,
      eventDate: input.eventDate,
      startTime: input.startTime,
      endTime: input.endTime,
      location: input.location,
      area: input.area,
      selectedServices: input.selectedServices,
      instructions: input.instructions,
      quoteBased: false,
      totalAmount: input.totalAmount,
    })
  },

  getMyBookings: async (): Promise<BookingDTO[]> => {
    const response = await http.get<BookingDTO[]>('/bookings/me')
    return response.data
  },

  getMyServiceRequests: async (): Promise<ServiceRequestDTO[]> => {
    const response = await http.get<ServiceRequestDTO[]>('/service-requests/me')
    return response.data
  },
}
