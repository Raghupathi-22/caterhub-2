# CaterHub Unified Web Frontend

Single React + Vite frontend for both customer and admin experiences.

## Route Areas

- Customer: `/`, `/login`, `/register`, `/services`, `/booking/:categoryId`, `/my-bookings`, `/profile`, `/worker/*`
- Admin: `/admin`, `/admin/login`, `/admin/dashboard`, `/admin/bookings`, `/admin/workers`, `/admin/customers`, `/admin/services`, `/admin/offers`, `/admin/events`, `/admin/support`

## Environment

Create `.env` from `.env.example`:

```env
VITE_API_BASE_URL=https://caterhub-2-production.up.railway.app/api/v1
VITE_SUPPORT_PHONE=+919959095202
VITE_ADMIN_WEB_URL=
VITE_PLAY_STORE_URL=
```

- `VITE_ADMIN_WEB_URL` is optional. Keep it empty for the unified app route (`/admin/login`), or set it to your separate admin web base URL.
- `VITE_PLAY_STORE_URL` should be set to the official CaterHub Google Play listing URL.

## Scripts

```bash
npm install
npm run dev
npm run build
npm run start
npm run preview
```

For Railway, build and start are configured in the repository root `railway.json`.
