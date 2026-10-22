import { Alert, Stack, Typography } from '@mui/material'

export function AdminCategoriesPage() {
  return (
    <Stack spacing={1}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Categories</Typography>
      <Alert severity="info">Categories are served by centralized backend ServiceCatalog.</Alert>
    </Stack>
  )
}

export function AdminReportsPage() {
  return (
    <Stack spacing={1}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Reports</Typography>
      <Alert severity="info">Detailed analytics endpoints are not yet exposed by backend beyond dashboard summary.</Alert>
    </Stack>
  )
}

export function AdminSettingsPage() {
  return (
    <Stack spacing={1}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Settings</Typography>
      <Typography color="text.secondary">Use backend environment variables for production operational settings.</Typography>
    </Stack>
  )
}

export function ProfilePage() {
  return (
    <Stack spacing={1}>
      <Typography variant="h4" sx={{ fontWeight: 700 }}>Profile</Typography>
      <Typography color="text.secondary">Profile management can be handled through /users/me APIs.</Typography>
    </Stack>
  )
}
