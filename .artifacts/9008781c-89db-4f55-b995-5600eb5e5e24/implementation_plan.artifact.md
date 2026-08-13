# Migrate CaterHub Authentication to Name + Mobile + OTP

Replace email + password authentication with Name + Mobile Number + OTP authentication for both Customers and Workers, while preserving existing user accounts, bookings, worker jobs, and security.

## User Review Required

> [!IMPORTANT]
> Password and email are no longer required for authentication. Existing user accounts will be preserved and linked by mobile number when logging in via OTP.

## Proposed Changes

### Backend - Phase A: OTP Architecture
- Create OTP Entity, Repository, and Service with rate-limiting, secure random generation, expiration, single-use check, and hash storage.
- Create `OtpSender` interface and `SmsOtpSender` / `MockOtpSender` implementation.
- Create OTP request/response DTOs.

### Backend - Phase B & C: Database Migration & OTP APIs
- Create Flyway migration script (`V11__Otp_Authentication.sql`) to add OTP table and make password/email nullable in `users` table.
- Create `OtpController` with `/api/v1/auth/otp/send` and `/api/v1/auth/otp/verify`.

### Backend - Phase D, E, F: Authentication & Role Routing
- Update `AuthService` and `AuthController` to support Name + Mobile + OTP registration and login for both Customers and Workers.
- Ensure proper JWT generation and role assignment (`ROLE_CUSTOMER`, `ROLE_WORKER`).

### Android - Phase G, H, I: UI & Onboarding
- Update Android DTOs, ApiServices, Repositories, and ViewModels.
- Redesign Login, Registration, and Worker onboarding screens with modern Material 3 UI for Name + Mobile + OTP.
- Ensure correct role-based routing (Customers to Customer Home, Workers to Worker Dashboard).

## Verification Plan

### Automated Tests
- Run backend unit tests (`mvn test`).
- Run Android unit tests.

### Manual Verification
- Deploy backend and Android app to test end-to-end customer registration, OTP verification, booking, logout, and worker onboarding/dashboard flow.
