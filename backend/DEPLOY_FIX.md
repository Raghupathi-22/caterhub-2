# CaterHub deployment fix

## What was fixed

- Removed the automatic Flyway `repair()` call from application startup.
- Current migration set contains exactly one file for every version V1..V14; there is no `V13__Service_Requests.sql`.
- `V14__Service_Requests.sql` is the only service-request migration.
- Updated `backend/src.zip` so it contains the current source instead of an obsolete migration snapshot.
- Docker build now fails immediately if duplicate Flyway migration versions are present.
- Docker build uses a clean Maven package.
- Added `railway.json` to force Railway to use the backend Dockerfile and the `/api/v1/health/live` liveness check.
- Spring Boot listens on Railway `$PORT` (`server.port=${PORT:${SERVER_PORT:8080}}`).
- Flyway runs `repair()` then `migrate()` so a failed V12 / rewritten checksum cannot keep the process from binding.
- OTP SMS/voice integrations remain disabled by default for local startup and can be enabled explicitly in Railway.

## Railway variables

Set these in the backend service:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- Or Railway MySQL vars: `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`
- `JWT_SECRET`
- `OTP_SMS_ENABLED=true`
- `TWOFACTOR_API_KEY=<your 2Factor API key>`
- `TWOFACTOR_OTP_TEMPLATE=<your approved 2Factor OTP template>`
- `OTP_VOICE_ENABLED=true` if voice OTP is required

Do not put API keys in source code.

## Important

The August 24 Railway log was produced by an artifact that still contained both:

- `V13__create_events.sql`
- `V13__Service_Requests.sql`

The current source does not contain the second file. Redeploy the current backend source/Dockerfile so Railway builds a fresh artifact. The Docker build now rejects duplicate migration versions instead of allowing a broken image to deploy.
