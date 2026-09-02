# CaterHub Unified Web Frontend

Single React + Vite frontend for both customer and admin experiences.

## Route Areas

- Customer: `/`, `/login`, `/register`, `/services`, `/booking/:categoryId`, `/my-bookings`, `/profile`, `/worker/*`
- Admin: `/admin`, `/admin/login`, `/admin/dashboard`, `/admin/bookings`, `/admin/workers`, `/admin/customers`, `/admin/services`, `/admin/offers`, `/admin/events`, `/admin/support`

## Environment

Create `.env` from `.env.example`:

```env
VITE_API_BASE_URL=https://api.caterhub.in/api/v1
VITE_SUPPORT_PHONE=+919999999999
```

## Scripts

```bash
npm install
npm run dev
npm run build
npm run start
npm run preview
```

For Railway, build and start are configured in the repository root `railway.json`.
