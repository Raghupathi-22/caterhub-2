# CaterHub startup crash fix

The uploaded project had a startup-order bug in `MainActivity`.

`ApiClient.authApiService` was accessed before `ApiClient.initialize(applicationContext)`.
`ApiClient` creates an `AuthTokenAuthenticator` that calls `requireNotNull(authLocalDataSource)`,
so the app could throw an exception during `onCreate()` and immediately close.

Fixed:
1. Initialize `ApiClient` before constructing repositories.
2. Added the Android INTERNET permission required by Retrofit/OkHttp.

No booking, OTP, event, worker, or UI flow was intentionally removed.
