const DEFAULT_SUPPORT_PHONE = '+919959095202'
const SUPPORT_PHONE_DISPLAY = '99590 95202'

const supportPhone = import.meta.env.VITE_SUPPORT_PHONE?.trim() || DEFAULT_SUPPORT_PHONE
const sanitizedSupportPhone = supportPhone.replace(/[^\d]/g, '')
const callHref = `tel:${supportPhone}`
const whatsappHref = `https://wa.me/${sanitizedSupportPhone}`

function resolveAdminLoginUrl(): string {
  const adminWebUrl = import.meta.env.VITE_ADMIN_WEB_URL?.trim()
  if (!adminWebUrl) return '/admin/login'
  if (adminWebUrl.endsWith('/admin/login')) return adminWebUrl
  return `${adminWebUrl.replace(/\/+$/, '')}/admin/login`
}

export const siteConfig = {
  supportPhone,
  supportPhoneDisplay: SUPPORT_PHONE_DISPLAY,
  sanitizedSupportPhone,
  callHref,
  whatsappHref,
  adminLoginUrl: resolveAdminLoginUrl(),
}
