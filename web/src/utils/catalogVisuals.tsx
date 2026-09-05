import {
  CameraAltOutlined,
  CelebrationOutlined,
  Diversity3Outlined,
  LocalShippingOutlined,
  LunchDiningOutlined,
  MiscellaneousServicesOutlined,
  MusicNoteOutlined,
  SpaOutlined,
  TempleHinduOutlined,
  WeekendOutlined,
} from '@mui/icons-material'
import type { ElementType } from 'react'

const iconByServiceType: Record<string, ElementType> = {
  CATERING_FOOD: LunchDiningOutlined,
  DECORATION: CelebrationOutlined,
  ENTERTAINMENT: MusicNoteOutlined,
  BEAUTY: SpaOutlined,
  PHOTOGRAPHY_VIDEO: CameraAltOutlined,
  RELIGIOUS_CEREMONY: TempleHinduOutlined,
  EVENT_SUPPORT: Diversity3Outlined,
  RENTALS: WeekendOutlined,
  TRANSPORT_LOGISTICS: LocalShippingOutlined,
  OTHER_EVENT_SERVICES: MiscellaneousServicesOutlined,
}

export function getCategoryIcon(serviceType: string, fallback: string): ElementType {
  return iconByServiceType[serviceType] ?? iconByServiceType[fallback] ?? MiscellaneousServicesOutlined
}

