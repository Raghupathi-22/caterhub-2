# CaterHub fixed build notes

This source contains fixes for the issues found in the uploaded project.

## Fixed
1. Flyway duplicate V13 migration:
   - V13__create_events.sql
   - V14__Service_Requests.sql
2. Backend API context path is /api/v1 so it matches the Android production base URL.
3. Security public endpoints now match the context-path-relative routes.
4. Event API is /api/v1/events instead of /api/v1/api/events.
5. Event ownership checks were added.
6. Marriage/event checklists were expanded.
7. Android "Plan My Complete Event" entry point added.
8. Event planner supports event type, date picker, start/end time picker, location, guests and budget.
9. 2Factor SMS integration was changed to the documented OTP endpoint:
   POST /API/V1/OTP/SEND
10. Voice OTP fallback was added using 2Factor Voice/OBD API.
11. OTP UI now has "Call me with OTP".
12. OTP credentials are environment variables only.

## Required Railway environment variables

TWOFACTOR_API_KEY=<NEW_API_KEY>
TWOFACTOR_OTP_TEMPLATE=<EXACT_APPROVED_DLT_OTP_TEMPLATE_NAME>
OTP_SMS_ENABLED=true
OTP_VOICE_ENABLED=true

Optional:
TWOFACTOR_BASE_URL=https://2factor.in
TWOFACTOR_VOICE_BASE_URL=https://2factor.in
TWOFACTOR_TIMEOUT_SECONDS=30

IMPORTANT:
- Do not put the 2Factor API key in Android code.
- Do not commit the API key to Git.
- The API key previously posted in chat should be regenerated before production use.
- The OTP template name must exactly match the approved 2Factor/DLT template.
- Voice OTP requires the 2Factor voice service/account to be enabled and funded.

## Database note

The duplicate V13 was fixed by moving Service Requests to V14. Do not delete or manually edit Flyway history on a database that has already been migrated without checking the history table first.

## Verification

The available execution environment did not contain Maven and could not download the Gradle distribution, so a full backend/Android build could not be executed here. The source was statically inspected and the modified Java/Kotlin files have balanced syntax blocks.
