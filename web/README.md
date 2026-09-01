# Cetaring Web Frontend

A modern React 19 + TypeScript web frontend for the Cetaring catering booking platform.

## Features

- **Authentication**: Register, login, and token refresh with JWT
- **Protected Routes**: Role-based access control
- **Responsive Design**: Material-UI components for beautiful UI
- **State Management**: Zustand for auth state
- **API Integration**: Axios with interceptors for API calls
- **Type Safety**: Full TypeScript support

## Project Structure

```
src/
├── api/              # API calls and Axios configuration
├── components/       # Reusable UI components
├── pages/           # Page-level components
├── store/           # Zustand stores (auth, etc.)
├── theme/           # Material-UI theme configuration
├── App.tsx          # Main app component with routing
├── main.tsx         # Entry point
└── index.css        # Global styles
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm/yarn/pnpm
- Backend API running on http://localhost:8080/api/v1

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

The application will be available at `http://localhost:3000`

### Building for Production

```bash
npm run build
```

Output will be in the `dist` directory.

### Testing

```bash
npm run test
```

### Linting

```bash
npm run lint
```

## Environment Variables

Create a `.env` file in the root directory:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Authentication Flow

1. **Register**: Create a new account with email, username, phone, and password
2. **Login**: Authenticate with email/username and password
3. **Token Management**: 
   - Access token stored in auth store (volatile)
   - Refresh token stored in localStorage for persistence
   - Automatic token refresh on expiration
4. **Protected Routes**: Routes wrapped with ProtectedRoute component require authentication

## API Integration

All API calls are made through Axios with automatic:
- Authorization header injection
- Token refresh on 401 response
- Error handling and logging
- Request/response interceptors

## Styling

The application uses Material-UI (MUI) v5 with:
- Custom theme with primary color #FF6B35
- Responsive grid system
- Pre-built components
- Dark mode support (extensible)

## Deployment

### Docker

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:18-alpine
WORKDIR /app
RUN npm install -g serve
COPY --from=build /app/dist ./dist
EXPOSE 3000
CMD ["serve", "-s", "dist", "-l", "3000"]
```

### AWS S3 + CloudFront

1. Build the project: `npm run build`
2. Upload `dist` folder to S3
3. Configure CloudFront to point to S3
4. Update backend CORS settings

## Testing Guidelines

- Unit tests for utility functions and hooks
- Integration tests for API calls and auth flows
- E2E tests for critical user journeys
- Target 80%+ coverage for critical paths

## Troubleshooting

### CORS Errors

Ensure backend has CORS configured:

```java
@Configuration
public class CorsConfiguration {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("*");
            }
        };
    }
}
```

### API Not Responding

1. Verify backend is running on port 8080
2. Check backend logs for errors
3. Verify VITE_API_BASE_URL in .env file
4. Check browser console for network errors

## Phase 1 Completion Checklist

- [x] React project initialized with Vite
- [x] Material-UI theme configured
- [x] Authentication API integration
- [x] Login and Register pages
- [x] Protected routes
- [x] Auth state management with Zustand
- [x] Axios with interceptors
- [x] Error handling
- [ ] npm install (requires Node.js)
- [ ] npm run dev (requires dependencies)

## Next Steps

1. Install dependencies: `npm install`
2. Configure backend API URL in `.env`
3. Run development server: `npm run dev`
4. Test authentication flow
5. Begin Phase 2: Booking System
