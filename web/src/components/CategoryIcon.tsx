import RestaurantIcon from '@mui/icons-material/Restaurant'
import CelebrationIcon from '@mui/icons-material/Celebration'
import MusicNoteIcon from '@mui/icons-material/MusicNote'
import SpaIcon from '@mui/icons-material/Spa'
import PhotoCameraIcon from '@mui/icons-material/PhotoCamera'
import TempleHinduIcon from '@mui/icons-material/TempleHindu'
import GroupsIcon from '@mui/icons-material/Groups'
import ChairIcon from '@mui/icons-material/Chair'
import LocalShippingIcon from '@mui/icons-material/LocalShipping'
import MiscellaneousServicesIcon from '@mui/icons-material/MiscellaneousServices'
import type { SvgIconProps } from '@mui/material'

const iconMap: Record<string, typeof RestaurantIcon> = {
  restaurant: RestaurantIcon,
  celebration: CelebrationIcon,
  music_note: MusicNoteIcon,
  spa: SpaIcon,
  photo_camera: PhotoCameraIcon,
  temple_hindu: TempleHinduIcon,
  groups: GroupsIcon,
  chair: ChairIcon,
  local_shipping: LocalShippingIcon,
  miscellaneous_services: MiscellaneousServicesIcon,
}

export function CategoryIcon({ icon, ...props }: SvgIconProps & { icon: string }) {
  const Icon = iconMap[icon] ?? MiscellaneousServicesIcon
  return <Icon {...props} />
}
