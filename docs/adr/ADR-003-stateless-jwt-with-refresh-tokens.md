# ADR-003: Stateless JWT Authentication with Refresh Token Rotation

## Status
**Accepted**

## Context
A daily wage marketplace API serves both web browsers and future mobile applications (Android/iOS). The application must scale horizontally behind a load balancer without sticky session affinity or shared distributed HTTP session storage.

Using only long-lived JWTs introduces a major security vulnerability: if a user's token is intercepted, it remains valid until expiration with no mechanism for immediate revocation. Conversely, short-lived tokens without refresh mechanisms force users to log in repeatedly, destroying user experience.

## Decision
We implemented a **Dual-Token Stateless Authentication Strategy** using Spring Security and JWT (HMAC-SHA256):

1. **Access Token:**
   - Lifetime: 60 minutes (`jwt.expiration-ms=3600000`).
   - Stateless: Contains user claims (`sub=email`, `userId`, `role`, `exp`). Verified cryptographically on every request by `JwtAuthenticationFilter` without querying the database.
2. **Refresh Token:**
   - Lifetime: 7 days (`jwt.refresh-expiration-ms=604800000`).
   - Stored in the database (`refresh_tokens` table) with `revoked` flag and expiry timestamp.
   - Endpoint `/api/auth/refresh` allows exchanging a valid refresh token for a new access token and rotated refresh token.
   - Endpoint `/api/auth/logout` revokes the refresh token immediately.
3. **Password Security:**
   - Passwords hashed using BCrypt (`BCryptPasswordEncoder` with strength 10).
   - Passwords are never returned in DTOs or logged in SLF4J MDC/console.
4. **Authorization:**
   - Role-Based Access Control (RBAC): `ROLE_CUSTOMER`, `ROLE_WORKER`, `ROLE_ADMIN`.
   - Method security (`@PreAuthorize`) ensures resource ownership (e.g. workers cannot modify another worker's profile, customers can only view their own bookings).

## Consequences

### Positive:
- **Scalability:** Read and transactional API requests validate the JWT signature in-memory in microseconds without database roundtrips.
- **Revocation Capability:** Compromised accounts or user logout immediately invalidates refresh tokens, terminating the session once the short-lived access token expires.
- **CSRF Immunity:** Stateless APIs reading tokens from the `Authorization: Bearer <token>` header are immune to standard browser Cross-Site Request Forgery (CSRF) attacks, permitting `csrf.disable()`.

### Negative:
- An intercepted access token remains valid for up to 60 minutes until expiry. (In hyper-sensitive environments, token blacklisting in Redis can be layered on top).

## Alternatives Considered
- **Server-Side HTTP Sessions:** Rejected due to horizontal scaling constraints and session replication overhead across multiple container instances.
- **Single Indefinite JWT:** Rejected as an unacceptable security risk in production.
