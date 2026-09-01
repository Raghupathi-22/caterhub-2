import { http } from './http'
import type { StaffingJob, WorkerDashboard, WorkerJob, WorkerProfile } from '../types/models'

export interface CreateWorkerProfileRequest {
  workerType: string
  experienceYears: number
  skills?: string
  preferredAreas?: string
  languages?: string
  bio?: string
}

export const workerApi = {
  createMyProfile: async (payload: CreateWorkerProfileRequest): Promise<WorkerProfile> => {
    const response = await http.post<WorkerProfile>('/workers/profiles/me', payload)
    return response.data
  },
  getMyProfile: async (): Promise<WorkerProfile> => {
    const response = await http.get<WorkerProfile>('/workers/profiles/me')
    return response.data
  },
  getMyDashboard: async (): Promise<WorkerDashboard> => {
    const response = await http.get<WorkerDashboard>('/workers/dashboard/me')
    return response.data
  },
  updateAvailability: async (available: boolean): Promise<void> => {
    await http.put('/workers/availability/me', { available })
  },
  getAvailableJobs: async (params: { role?: string; area?: string; search?: string }): Promise<StaffingJob[]> => {
    const response = await http.get<StaffingJob[]>('/workers/jobs', { params })
    return response.data
  },
  acceptJob: async (jobId: number): Promise<void> => {
    await http.post(`/workers/jobs/${jobId}/accept`)
  },
  getMyJobs: async (): Promise<WorkerJob[]> => {
    const response = await http.get<WorkerJob[]>('/workers/jobs/me')
    return response.data
  },
}
