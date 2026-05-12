# Kaze Security Checklist

This checklist is a practical development companion for Kaze. It is inspired by OWASP ASVS, OWASP Top 10, and OWASP Secure Coding Practices, but it is not a replacement for a professional security audit.

Use it during feature work, code review, and production-readiness checks.

## How To Use This File

For every feature, ask:

- [ ] Did I validate the input on the backend?
- [ ] Did I protect access with server-side authorization?
- [ ] Did I avoid exposing sensitive user, guest, event, payment, or pass data?
- [ ] Did I log enough for debugging and security without leaking secrets?
- [ ] Did I test dangerous cases, not only the happy path?

For AI-assisted review, paste one section at a time with:

```text
Review my implementation against this checklist and suggest concrete fixes.
```

## Authentication

- [ ] Secure login flow: use a trusted authentication flow and avoid custom auth logic unless the risk is understood.
- [ ] Strong password storage: never store plain-text passwords; use Argon2id, bcrypt, or scrypt.
- [ ] MFA or passkey support: require stronger authentication for staff, organizer, venue, finance, and platform admin accounts.
- [ ] Brute-force protection: add rate limiting, progressive delays, account lockout, or bot protection for suspicious login attempts.
- [ ] Secure password reset: reset tokens must be random, short-lived, single-use, and never logged.

## Authorization And Access Control

- [ ] Role-based access control: define clear roles for guests, organizers, venue staff, vendors, admins, and platform operators.
- [ ] Object-level authorization: always check that the current user can access the exact event, invitation, pass, stay, reservation, request, or venue resource.
- [ ] Server-side permission checks: never rely on the app UI hiding restricted actions.
- [ ] Least privilege: users, services, databases, storage buckets, API keys, and deployment identities should only have the permissions they need.
- [ ] Admin action protection: require stronger checks for deleting users, changing event access rules, exporting guest data, approving payouts, or modifying production configuration.

## Session And Token Handling

- [ ] Secure session tokens: session IDs, access tokens, and refresh tokens must be random, hard to guess, and transmitted only over HTTPS.
- [ ] Token expiration: access tokens and refresh tokens should expire.
- [ ] Logout invalidation: logout should revoke the active session or refresh token where possible.
- [ ] Refresh token rotation: rotate refresh tokens after use and detect reuse as possible compromise.
- [ ] Safe token storage: store mobile tokens in Android Keystore or iOS Keychain; avoid persistent browser storage for sensitive tokens.

## Input Validation And Output Safety

- [ ] Input validation: validate type, length, format, range, required fields, and cross-field rules on the backend.
- [ ] Allowlist validation: prefer allowlists over blocklists for statuses, roles, event categories, pass types, and currencies.
- [ ] Output encoding: encode output before rendering it in HTML, emails, PDFs, admin dashboards, or shareable pages.
- [ ] File upload validation: validate type, size, extension, and content; store uploads outside executable directories.
- [ ] Injection protection: use parameterized queries or safe repository APIs; never build SQL or shell commands from raw user input.

## API Security

- [ ] Rate limiting: add limits for login, token refresh, search, RSVP, invitation joins, payment-like actions, and public endpoints.
- [ ] Request size limits: cap payload sizes to reduce abuse and denial-of-service risk.
- [ ] API versioning: keep `/api/v1` stable and avoid insecure compatibility behavior for old clients.
- [ ] Strict CORS: allow only trusted origins; never use wildcard CORS for authenticated APIs.
- [ ] Idempotency: use request IDs or idempotency keys for reservations, RSVP changes, service requests, payment-like actions, and background job triggers that must not run twice.

## Data Protection And Privacy

- [ ] Encrypt data in transit: use HTTPS/TLS for app, web, admin, and internal API traffic.
- [ ] Encrypt sensitive data at rest: protect databases, backups, media, exports, and local caches where possible.
- [ ] Minimize collected data: collect only what Kaze needs for events, passes, reservations, services, and support.
- [ ] Protect personal data: treat guest lists, invitations, passes, stays, organizer records, vendor requests, payment references, and venue access rules as sensitive.
- [ ] Data retention: define how long inactive events, expired passes, logs, uploads, and personal data are kept.

## Secrets And Configuration

- [ ] No secrets in code: never commit API keys, database passwords, signing keys, OAuth secrets, private keys, or tokens.
- [ ] Use environment variables or a secrets manager: keep real secrets outside the repository.
- [ ] Secret rotation: keep a rotation path for leaked keys or compromised devices/accounts.
- [ ] Separate environments: use different secrets, databases, OAuth clients, and storage buckets for development, staging, and production.
- [ ] Safe production defaults: production must not run with debug mode, default secrets, destructive database modes, verbose errors, local CORS defaults, or open admin endpoints.

## Error Handling And Logging

- [ ] Safe error messages: do not expose stack traces, SQL errors, secret values, token contents, or internal paths to users.
- [ ] Security logging: log important events such as login failures, token refresh failures, permission failures, invitation joins, RSVP changes, service requests, admin actions, and suspicious activity.
- [ ] No sensitive data in logs: do not log passwords, tokens, OAuth codes, full payment secrets, private guest details, or access pass secrets.
- [ ] Audit trails: record who changed important records, when, and what changed.
- [ ] Monitoring and alerts: alert on unusual errors, auth failure spikes, high API usage, payment-like anomalies, background job failures, and server failures.

## Infrastructure And Deployment

- [ ] Regular backups: back up databases and important files automatically.
- [ ] Restore testing: test that backups can actually be restored.
- [ ] Dependency scanning: scan dependencies for known vulnerabilities and update them regularly.
- [ ] Secure CI/CD: protect deployment keys, restrict who can deploy, and review changes before production.
- [ ] Least exposure: keep databases, admin panels, internal services, storage buckets, and dashboards private unless they must be public.

## Background Jobs And Failure Isolation

- [ ] Move slow or side-effect-heavy work out of request handlers.
- [ ] Background task failures must not fail the whole system or block unrelated operations.
- [ ] Failed background tasks should be quarantined in a dead-letter queue for manual inspection, replay, or cancellation.
- [ ] Retry policies should have limits, backoff, and idempotency protections.
- [ ] Manual replay should preserve audit history and avoid double-sending emails, notifications, reservations, or payment-like operations.

## AI, Business Logic, And Abuse Prevention

- [ ] AI safety boundaries: do not send unnecessary personal, guest, event, pass, or payment data to AI providers.
- [ ] AI-generated code review: review AI-generated code before merging, especially auth, payments, permissions, and data handling.
- [ ] Business logic abuse testing: test duplicate reservations, fake invitation joins, pass sharing, unauthorized event access, refund abuse, repeated OTP/password-reset requests, and privilege escalation.
- [ ] Offline and sync abuse: test stale cached permissions, replayed offline actions, duplicate sync submissions, and revoked access.

## AI Review Prompts

### General Security Review

```text
Review this code against the Kaze security checklist. Focus on authentication, authorization, input validation, session handling, data protection, logging, dependency risks, background task isolation, and business logic abuse. Give concrete fixes, not only theory.
```

### API Endpoint Review

```text
Review this API endpoint for security issues. Check authorization, object-level access control, validation, rate limiting, error handling, logging, idempotency, and abuse cases.
```

### Database And Model Review

```text
Review this database/model design for security and privacy. Check sensitive fields, access control risks, audit trail needs, retention, encryption, and migration risks.
```

### Mobile App Review

```text
Review this mobile app code for security. Check token storage, API usage, local data storage, error handling, permissions, offline-sync risks, and sensitive data exposure.
```

### Before Production

```text
Act like a security reviewer before production launch. Find missing controls, risky defaults, exposed secrets, weak permissions, dependency risks, monitoring gaps, and background job failure modes.
```

## References

- OWASP Application Security Verification Standard: https://owasp.org/www-project-application-security-verification-standard/
- OWASP Top 10: https://owasp.org/www-project-top-ten/
- OWASP Secure Coding Practices Checklist: https://owasp.org/www-project-secure-coding-practices-quick-reference-guide/stable-en/02-checklist/05-checklist
- OWASP Authentication Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html
- OWASP Session Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html
- OWASP Secrets Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
