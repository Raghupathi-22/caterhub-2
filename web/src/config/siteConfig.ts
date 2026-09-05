const supportPhone = import.meta.env.VITE_SUPPORT_PHONE ?? '+919999999999'
const sanitizedSupportPhone = supportPhone.replace(/[^\d]/g, '')

function resolveAdminLoginUrl(): string {
  const adminWebUrl = import.meta.env.VITE_ADMIN_WEB_URL?.trim()
  if (!adminWebUrl) return '/admin/login'
  if (adminWebUrl.endsWith('/admin/login')) return adminWebUrl
  return `${adminWebUrl.replace(/\/+$/, '')}/admin/login`
}

export const siteConfig = {
  supportPhone,
  sanitizedSupportPhone,
  adminLoginUrl: resolveAdminLoginUrl(),
}

