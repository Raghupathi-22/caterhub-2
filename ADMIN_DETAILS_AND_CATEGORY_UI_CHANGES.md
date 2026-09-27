# CaterHub – Service Category UI + Admin Booking Details

## Android UI
- Reworked the customer Home Service Categories into category-specific premium visual styles.
- Each category now has its own accent, gradient, icon container shape, badge, arrow treatment, and card geometry.
- No Android backend/API behavior was changed by the UI work.

## Admin booking details
Added admin-only details APIs and UI for customer and worker acceptance information.

### Regular food/customer bookings
`GET /api/v1/admin/orders/{bookingId}/details`

Returns:
- customer name
- customer mobile number
- customer email/username/verification
- booking/event details
- accepted workers
- worker name/mobile/email
- worker role
- worker profile information
- assignment status/timestamps
- all worker assignments

### Service requests / Catering Staff
`GET /api/v1/admin/service-requests/{serviceRequestId}/details`

Returns:
- customer details
- service/event details
- staffing requirements
- required/accepted/remaining worker counts
- accepted worker names/mobile/email
- worker role/profile details
- acceptance status/time

### Staffing linkage
`V20__Link_Staffing_To_Service_Requests.sql` adds `service_request_id` to `staffing_requests` so new Catering Staff requests can be traced exactly to the customer's service request.

A backward-compatible lookup is also included for older Catering Staff records created before V20.

## Deployment order
1. Deploy the backend so Flyway applies V20.
2. Verify the admin API endpoints.
3. Deploy the web frontend.
4. Install/test the Android build for the category UI.

The backend must be deployed before relying on the new admin details endpoints.
