# CaterHub current fixes

This patch is for the current `main-UI-screen` architecture.

## Fixes
1. Plan My Event 403: includes the current event catalog/checklist implementation where catalog and preview calls are public, matching commit 10707d7.
2. Expired/missing JWT: backend returns 401 for unauthenticated requests and Android automatically refreshes the access token using the refresh token.
3. Book Catering Staff / Decorations & Equipment: successful submit now goes to My Bookings instead of silently popping back.
4. My Bookings now also displays customer staff requests and decoration/equipment requests.
5. Staff/decor request history endpoints added.
6. Event schema repair V17 included for environments that missed event marketplace tables.

## Copying
Copy each file to the exact same relative path in the current CaterHub project, replacing the existing file.

IMPORTANT: this patch targets the latest `main-UI-screen` architecture (EventPlannerViewModel/EventDashboard DTOs). Do not mix these Event files with the older EventViewModel/EventResponse architecture.

## Backend
After copying backend files, deploy/restart the backend so the new endpoints and V17 migration are actually running.

## Android
After copying app files:
- Clean/Rebuild the app.
- Uninstall the old APK if the app keeps stale local session state, then install the new APK.
- Login again with OTP.
- Test Plan My Event.
- Test Book Catering.
- Test Book Catering Staff.
- Test Decorations & Equipment.
- Open My Bookings and verify all three categories.

### Google Play SMS Retriever hash

Production OTP SMS must use the hash for application ID `com.caterhub.app` and the Google Play App Signing certificate, not the local debug/upload certificate.

1. In Play Console, open **Setup > App integrity > App signing** and download the **App signing key certificate**.
2. Use Google's SMS Retriever app-hash calculation with `com.caterhub.app` and that certificate. Confirm the resulting 11-character hash against a build installed from Google Play Internal Testing.
3. Configure the hash in the existing 2Factor account-side OTP SMS template:
   `<#> Your CaterHub verification code is {OTP}`
   followed by the app hash on the final line.

The backend uses 2Factor's OTP endpoint and does not inject the SMS template or app hash in source code.
