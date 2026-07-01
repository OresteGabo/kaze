# Authentication Guide: Custom Social Login for Ktor + Kotlin Multiplatform

This guide defines a zero-cost, self-managed authentication architecture for Kaze using Kotlin Multiplatform, Compose Multiplatform, and a Ktor backend.

The goal is to support Google, Apple, and Facebook sign-in without paying for managed identity platforms at scale. Kaze uses standard OAuth 2.0 authorization-code flows, verifies provider identity on the Ktor server, then issues its own first-party JWT and refresh token pair.

## Goals

- Avoid Google Identity Platform pricing by using standard OAuth 2.0 credentials instead of managed identity-user billing.
- Keep sessions under Kaze control by issuing Kaze-owned JWT access tokens and Kaze-owned refresh tokens.
- Avoid recurring provider calls for active users by refreshing Kaze sessions locally instead of re-querying Google, Apple, or Meta every month.
- Support Android, iOS, and future desktop/web KMP targets through a shared auth model and platform-specific browser/deep-link handlers.
- Keep provider secrets only on the Ktor backend.

## Non-Goals

- Do not store Google, Apple, or Facebook client secrets in the KMP app.
- Do not use Google Identity Platform/Firebase Authentication for the main login path if the objective is avoiding per-user managed-auth pricing.
- Do not use provider refresh tokens for routine Kaze app sessions.
- Do not trust social profile data from the client without server verification.

## Architecture Diagram

```text
┌────────────────────────────┐
│ Compose Multiplatform App  │
│ Android / iOS / Desktop    │
└──────────────┬─────────────┘
               │ 1. User taps Google / Apple / Facebook
               │
               ▼
┌────────────────────────────┐
│ System Browser / CustomTab │
│ Provider authorization URL │
└──────────────┬─────────────┘
               │ 2. User authenticates with provider
               │
               ▼
┌────────────────────────────┐
│ Ktor Callback Endpoint     │
│ /api/v1/auth/{provider}/callback
└──────────────┬─────────────┘
               │ 3. Validate state + PKCE verifier
               │ 4. Exchange authorization_code server-side
               │ 5. Verify ID token or provider access token
               │ 6. Upsert Kaze user + linked provider identity
               │ 7. Issue Kaze JWT + Kaze refresh token
               │
               ▼
┌────────────────────────────┐
│ App Deep Link Redirect     │
│ kaze://auth/callback?...   │
└──────────────┬─────────────┘
               │ 8. App stores Kaze tokens securely
               │
               ▼
┌────────────────────────────┐
│ Kaze API Requests          │
│ Authorization: Bearer JWT  │
└────────────────────────────┘
```

## Provider Strategy

| Provider | Recommended flow | Server verification | Stable social ID |
|---|---|---|---|
| Google | Standard OAuth 2.0 authorization code with PKCE | Exchange `code` at Google token endpoint, verify `id_token` signature and claims | `sub` |
| Apple | Sign in with Apple REST API authorization code | Exchange `code` at Apple token endpoint, verify `id_token` signature and claims | `sub` |
| Facebook | OAuth dialog authorization code | Exchange `code` for access token, validate token with Meta, fetch `/me` profile | Facebook user `id` |

Note: Google and Apple provide OpenID Connect identity tokens in the normal sign-in flow. Facebook Login commonly uses a Graph API access token rather than an `id_token`; Kaze should treat Meta’s verified user ID as the provider subject.

## Redirect URI Model

Use backend-owned HTTPS redirect URIs for all providers:

```text
https://api.kazerwanda.com/api/v1/auth/google/callback
https://api.kazerwanda.com/api/v1/auth/apple/callback
https://api.kazerwanda.com/api/v1/auth/facebook/callback
```

After the backend completes verification and creates a Kaze session, it redirects into the app:

```text
kaze://auth/callback?login_token=ONE_TIME_LOGIN_TOKEN&state=STATE
```

Recommended production pattern:

- The backend should not put long-lived refresh tokens directly in the deep link.
- The backend should create a short-lived one-time `login_token`.
- The app calls `POST /api/v1/auth/session/claim` with the one-time token.
- The backend returns the Kaze access token and refresh token over HTTPS.
- The one-time token is immediately revoked after first use.

## Step-by-Step Provider Setup

### Google Cloud Console

1. Open Google Cloud Console and select the Kaze project.
2. Go to `APIs & Services` -> `OAuth consent screen`.
3. Configure app name, support email, privacy policy URL, terms URL, and authorized domains.
4. Go to `APIs & Services` -> `Credentials`.
5. Create an `OAuth client ID`.
6. Use `Web application` for the backend server flow.
7. Add authorized redirect URI:

```text
https://api.kazerwanda.com/api/v1/auth/google/callback
```

8. Store the Google client ID and client secret only in backend environment variables.
9. Request minimal scopes:

```text
openid email profile
```

10. Do not enable Google Identity Platform for this flow.

Recommended environment variables:

```bash
GOOGLE_OAUTH_CLIENT_ID=...
GOOGLE_OAUTH_CLIENT_SECRET=...
GOOGLE_OAUTH_REDIRECT_URI=https://api.kazerwanda.com/api/v1/auth/google/callback
```

### Apple Developer Portal

1. Enroll in the Apple Developer Program.
2. Create or confirm the Kaze App ID.
3. Enable `Sign in with Apple` for the App ID.
4. Create a `Services ID` for web/server OAuth sign-in.
5. Configure the return URL:

```text
https://api.kazerwanda.com/api/v1/auth/apple/callback
```

6. Create a Sign in with Apple private key.
7. Store the Key ID, Team ID, Services ID, and private key securely on the backend.
8. Generate the Apple client secret JWT server-side when exchanging authorization codes.

Recommended environment variables:

```bash
APPLE_TEAM_ID=...
APPLE_KEY_ID=...
APPLE_SERVICE_ID=com.kaze.auth
APPLE_PRIVATE_KEY_PEM=...
APPLE_REDIRECT_URI=https://api.kazerwanda.com/api/v1/auth/apple/callback
```

Important Apple note:

- Apple may only return the user’s name and email the first time a user authorizes the app.
- Persist Apple email/name immediately when present.
- Always rely on Apple `sub` as the stable account link key.

### Meta for Developers

1. Create a Meta app in Meta for Developers.
2. Add the `Facebook Login` product.
3. Configure valid OAuth redirect URI:

```text
https://api.kazerwanda.com/api/v1/auth/facebook/callback
```

4. Configure app domains and privacy policy URL.
5. Request minimal permissions:

```text
email public_profile
```

6. Store App ID and App Secret only on the backend.
7. Use the OAuth dialog endpoint to request an authorization code.
8. Exchange the code for a user access token server-side.
9. Validate the token with Meta and fetch the user profile from Graph API.

Recommended environment variables:

```bash
FACEBOOK_APP_ID=...
FACEBOOK_APP_SECRET=...
FACEBOOK_REDIRECT_URI=https://api.kazerwanda.com/api/v1/auth/facebook/callback
```

## Database Schema

Use separate tables for users, provider identities, refresh tokens, and short-lived OAuth state.

Production note:

- Kaze now uses the PostgreSQL schema from [dev_schema.sql](server/src/main/resources/db/dev_schema.sql).
- The seed data lives in [dev_seed.sql](server/src/main/resources/db/dev_seed.sql).
- All primary and foreign IDs are `VARCHAR(120)` and are represented as Kotlin `String`.
- The social provider table is `user_auth_providers`, not `auth_provider_accounts`.
- The provider column is `provider`.
- The stable social subject column is `provider_subject`.

```sql
CREATE TABLE app_users (
    id VARCHAR(120) PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    display_name VARCHAR(240),
    password_hash TEXT,
    roles TEXT[] NOT NULL DEFAULT ARRAY['CUSTOMER']::TEXT[],
    disabled BOOLEAN NOT NULL DEFAULT false,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX app_users_email_idx
ON app_users (lower(email));

CREATE TABLE user_auth_providers (
    id VARCHAR(120) PRIMARY KEY,
    user_id VARCHAR(120) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    provider VARCHAR(40) NOT NULL,
    provider_subject VARCHAR(320) NOT NULL,
    email VARCHAR(320) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    display_name VARCHAR(240),
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (provider, provider_subject)
);

CREATE INDEX user_auth_providers_user_id_idx
ON user_auth_providers(user_id);

CREATE TABLE auth_refresh_tokens (
    id VARCHAR(120) PRIMARY KEY,
    user_id VARCHAR(120) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE,
    family_id VARCHAR(120) NOT NULL,
    device_id VARCHAR(240),
    device_label VARCHAR(240),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_token_id VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at TIMESTAMPTZ
);

CREATE INDEX auth_refresh_tokens_user_id_idx
ON auth_refresh_tokens(user_id);

CREATE TABLE oauth_login_attempts (
    id VARCHAR(120) PRIMARY KEY,
    provider VARCHAR(40) NOT NULL,
    state_hash TEXT NOT NULL UNIQUE,
    code_verifier_hash TEXT NOT NULL,
    code_verifier TEXT NOT NULL,
    nonce_hash TEXT,
    nonce TEXT,
    app_redirect_uri TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ
);

CREATE TABLE auth_one_time_login_tokens (
    id VARCHAR(120) PRIMARY KEY,
    user_id VARCHAR(120) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ
);
```

### Duplicate Account Prevention

Use this account-linking order:

1. If `(provider, provider_subject)` exists, sign in that user.
2. Else if provider email is verified and matches an existing Kaze user email, link the provider to that user after passing safety checks.
3. Else create a new user and link the provider.

Safety checks:

- Never link on unverified email.
- Handle Apple private relay emails as real emails but avoid assuming they match other providers.
- Require explicit user confirmation before linking two active accounts with different emails.
- Store provider subjects as opaque strings; never infer meaning from them.

### Kotlin Persistence Models

Current backend persistence models map directly to the schema:

```kotlin
@Serializable
data class AppUser(
    val id: String,
    val email: String,
    val displayName: String?,
    val passwordHash: String?,
    val roles: List<String>,
    val disabled: Boolean,
    val lastLoginAt: Instant?,
)

@Serializable
data class UserAuthProvider(
    val id: String,
    val userId: String,
    val provider: String,
    val providerSubject: String,
    val email: String,
    val emailVerified: Boolean,
)

@Serializable
data class AuthRefreshToken(
    val id: String,
    val userId: String,
    val tokenHash: String,
    val familyId: String,
    val expiresAt: Instant,
    val revokedAt: Instant?,
)
```

Security rule:

- `password_hash` and `token_hash` are persistence-only fields.
- They must never be returned by API response DTOs.
- API response models should expose only safe user/session information.

## Ktor Backend Implementation

### Gradle Dependencies

```kotlin
dependencies {
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("com.auth0:java-jwt:$javaJwtVersion")
    implementation("com.auth0:jwks-rsa:$jwksRsaVersion")
}
```

### Auth Configuration Model

```kotlin
enum class SocialProviderType {
    GOOGLE,
    APPLE,
    FACEBOOK,
}

data class OAuthProviderConfig(
    val clientId: String,
    val clientSecret: String? = null,
    val redirectUri: String,
    val authorizeUrl: String,
    val tokenUrl: String,
    val scopes: List<String>,
)

data class SocialAuthConfig(
    val appDeepLinkRedirect: String,
    val google: OAuthProviderConfig,
    val apple: OAuthProviderConfig,
    val facebook: OAuthProviderConfig,
)
```

### PKCE Utilities

```kotlin
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

private val secureRandom = SecureRandom()

fun randomUrlSafeToken(bytes: Int = 32): String {
    val data = ByteArray(bytes)
    secureRandom.nextBytes(data)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(data)
}

fun sha256Base64Url(value: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
}
```

### Start OAuth Route

The app requests an authorization URL from Ktor instead of building it itself. This keeps provider configuration centralized.

```kotlin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.socialAuthRoutes(service: SocialAuthService) {
    route("/api/v1/auth") {
        get("/{provider}/start") {
            val provider = call.parameters["provider"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val appRedirectUri = call.request.queryParameters["appRedirectUri"] ?: "kaze://auth/callback"
            val result = service.createAuthorizationRequest(provider, appRedirectUri)
            call.respond(result)
        }
    }
}

@kotlinx.serialization.Serializable
data class AuthorizationStartResponse(
    val authorizationUrl: String,
    val state: String,
)
```

The generated authorization URL should include:

```text
client_id
redirect_uri
response_type=code
scope
state
code_challenge
code_challenge_method=S256
nonce
```

Provider-specific notes:

- Google: include `access_type=offline` only if Kaze truly needs Google API refresh tokens. For normal login, avoid it.
- Apple: use `response_mode=form_post` if requesting `name` or `email`.
- Facebook: request `email,public_profile`; PKCE support may vary, so still use `state` and server-side secret exchange.

### Callback Routes

```kotlin
fun Route.socialAuthCallbackRoutes(service: SocialAuthService) {
    route("/api/v1/auth") {
        get("/google/callback") {
            val code = call.request.queryParameters["code"]
            val state = call.request.queryParameters["state"]
            val redirect = service.completeOAuthCallback(
                provider = SocialProviderType.GOOGLE,
                code = code,
                state = state,
            )
            call.respondRedirect(redirect)
        }

        post("/apple/callback") {
            val form = call.receiveParameters()
            val code = form["code"]
            val state = form["state"]
            val redirect = service.completeOAuthCallback(
                provider = SocialProviderType.APPLE,
                code = code,
                state = state,
            )
            call.respondRedirect(redirect)
        }

        get("/facebook/callback") {
            val code = call.request.queryParameters["code"]
            val state = call.request.queryParameters["state"]
            val redirect = service.completeOAuthCallback(
                provider = SocialProviderType.FACEBOOK,
                code = code,
                state = state,
            )
            call.respondRedirect(redirect)
        }
    }
}
```

### Token Exchange Service

```kotlin
suspend fun exchangeAuthorizationCode(
    provider: SocialProviderType,
    code: String,
    codeVerifier: String?,
): ProviderTokenResponse {
    val config = configFor(provider)
    val parameters = Parameters.build {
        append("grant_type", "authorization_code")
        append("client_id", config.clientId)
        config.clientSecret?.let { append("client_secret", it) }
        append("code", code)
        append("redirect_uri", config.redirectUri)
        codeVerifier?.let { append("code_verifier", it) }
        if (provider == SocialProviderType.APPLE) {
            append("client_secret", appleClientSecretJwt())
        }
    }

    return httpClient.post(config.tokenUrl) {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(FormDataContent(parameters))
    }.body()
}

@kotlinx.serialization.Serializable
data class ProviderTokenResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val expires_in: Long? = null,
    val refresh_token: String? = null,
    val id_token: String? = null,
)
```

### Provider Identity Verification

```kotlin
data class VerifiedSocialIdentity(
    val provider: String,
    val providerSubject: String,
    val email: String?,
    val emailVerified: Boolean,
    val displayName: String?,
    val avatarUrl: String?,
)
```

Google verification:

- Fetch Google JWKS.
- Verify `id_token` signature.
- Validate `iss`, `aud`, `exp`, and optional `nonce`.
- Use `sub` as `providerSubject`.

Apple verification:

- Fetch Apple JWKS.
- Verify `id_token` signature.
- Validate `iss = https://appleid.apple.com`, `aud`, `exp`, and optional `nonce`.
- Use `sub` as `providerSubject`.
- Persist email/name on first login if present.

Facebook verification:

- Exchange code for access token.
- Validate with Meta debug token endpoint using an app access token.
- Confirm `is_valid`, `app_id`, expiry, and user ID.
- Fetch profile:

```text
GET https://graph.facebook.com/me?fields=id,name,email,picture&access_token=...
```

- Use `id` as `providerSubject`.

### Repository Functions

The current repository contract should expose schema-aligned lookup and linking methods:

```kotlin
fun findUserBySocialProvider(provider: String, subject: String): AppUser?

fun linkSocialProviderToUser(
    userId: String,
    provider: String,
    subject: String,
    email: String,
    emailVerified: Boolean = false,
    displayName: String? = null,
    avatarUrl: String? = null,
): UserAuthProvider
```

Expected behavior:

- `findUserBySocialProvider` joins `user_auth_providers` with `app_users`.
- `linkSocialProviderToUser` inserts into `user_auth_providers`.
- Existing password and refresh-token storage remains hashed and server-only.

### Kaze JWT Issuing

```kotlin
fun issueKazeAccessToken(user: AppUser): String =
    JWT.create()
        .withIssuer("kaze-api")
        .withAudience("kaze-mobile")
        .withSubject(user.id.toString())
        .withClaim("email", user.email)
        .withClaim("roles", user.roles)
        .withIssuedAt(Date())
        .withExpiresAt(Date.from(Instant.now().plusSeconds(15 * 60)))
        .sign(Algorithm.HMAC256(jwtSecret))
```

Recommended access-token lifetime:

- Mobile access token: 15 minutes.
- Refresh token: 30 to 90 days.
- One-time login token: 60 to 120 seconds.

### Ktor JWT Protection

```kotlin
install(Authentication) {
    jwt("kaze-jwt") {
        realm = "kaze-api"
        verifier(jwtVerifier)
        validate { credential ->
            val userId = credential.subject
            if (userId != null) JWTPrincipal(credential.payload) else null
        }
    }
}

routing {
    authenticate("kaze-jwt") {
        get("/api/v1/me") {
            val principal = call.principal<JWTPrincipal>()
            call.respond(mapOf("userId" to principal?.subject))
        }
    }
}
```

## Self-Managed Refresh Tokens

Provider refresh tokens should not power normal Kaze sessions. Kaze should issue its own opaque refresh token and store only its hash on the server.

### Refresh Flow

```text
1. App stores Kaze refresh token securely.
2. Access token expires after ~15 minutes.
3. App calls POST /api/v1/auth/refresh with refresh token.
4. Server hashes token and finds active row.
5. Server rotates refresh token.
6. Server returns new access token + new refresh token.
7. Old refresh token is marked replaced.
```

### Refresh Endpoint

```kotlin
@kotlinx.serialization.Serializable
data class RefreshRequest(val refreshToken: String)

@kotlinx.serialization.Serializable
data class AuthSessionResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
)

post("/api/v1/auth/refresh") {
    val request = call.receive<RefreshRequest>()
    val session = authService.rotateRefreshToken(request.refreshToken)
    call.respond(session)
}
```

### Remember Me Without Provider Fees

For “Remember Me”:

- Store Kaze refresh token locally after login.
- Rotate it on each refresh.
- Do not call Google, Apple, or Facebook during normal app launches.
- Re-contact the social provider only when:
  - the user explicitly links/unlinks accounts,
  - account risk requires re-verification,
  - the Kaze refresh token is expired/revoked,
  - the user signs in again after logout.

Client storage recommendation:

- Android: EncryptedSharedPreferences or platform secure storage through `SecureStore`.
- iOS: Keychain through the KMP secure-store abstraction.
- SQLDelight: OK for non-secret session metadata, but avoid storing raw refresh tokens unless the database is encrypted.

Server storage recommendation:

- Store only SHA-256 or Argon2id hashes of refresh tokens.
- Keep token family IDs for reuse detection.
- If an old refresh token is reused after rotation, revoke the entire token family.

## Compose Multiplatform Client Logic

### Start Login

```kotlin
suspend fun startSocialLogin(provider: String) {
    val start = api.getAuthorizationStart(provider)
    secureStore.put("oauth.state", start.state)
    browser.open(start.authorizationUrl)
}
```

Use platform-specific browser launchers:

- Android: Chrome Custom Tabs or `ACTION_VIEW`.
- iOS: `ASWebAuthenticationSession`.
- Desktop: system browser.

Do not use embedded WebViews for OAuth.

### Deep Link Handling

Register app scheme:

```text
kaze://auth/callback
```

Expected callback:

```text
kaze://auth/callback?login_token=abc123&state=xyz
```

KMP flow:

```kotlin
suspend fun handleAuthDeepLink(uri: String) {
    val parsed = parseUri(uri)
    val state = parsed.query["state"]
    val loginToken = parsed.query["login_token"]

    val expectedState = secureStore.get("oauth.state")
    require(state != null && state == expectedState) {
        "Invalid OAuth state"
    }

    val session = api.claimOneTimeLoginToken(loginToken ?: error("Missing login token"))
    secureStore.put("auth.access_token", session.accessToken)
    secureStore.put("auth.refresh_token", session.refreshToken)
    secureStore.remove("oauth.state")
}
```

### Claim One-Time Login Token

```kotlin
@kotlinx.serialization.Serializable
data class ClaimLoginTokenRequest(val loginToken: String)

post("/api/v1/auth/session/claim") {
    val request = call.receive<ClaimLoginTokenRequest>()
    val session = authService.claimOneTimeLoginToken(request.loginToken)
    call.respond(session)
}
```

## PKCE And State Validation

### State

`state` protects against CSRF and callback injection.

Requirements:

- Generate random state on the server.
- Store only a hash of state in `oauth_login_attempts`.
- Include raw state in the provider authorization URL.
- Validate callback state before exchanging the code.
- Expire state quickly, usually within 5 to 10 minutes.
- Mark state as consumed after successful callback.

### PKCE

PKCE protects the authorization code if intercepted.

Requirements:

- Generate a `code_verifier`.
- Send `code_challenge = BASE64URL(SHA256(code_verifier))`.
- Use `code_challenge_method=S256`.
- Store only a hash of `code_verifier`.
- Send the raw `code_verifier` during token exchange.

Recommended implementation for Kaze:

- Server creates state + PKCE and stores them.
- Client opens only the final authorization URL.
- Server uses stored verifier during callback.

This works because Kaze routes provider callbacks through the Ktor backend first.

## Logout And Account Revocation

### Local Logout

```text
POST /api/v1/auth/logout
Authorization: Bearer <access-token>
```

Behavior:

- Revoke the current Kaze refresh token or all device tokens for the current session.
- Delete local access/refresh tokens from the app.
- Return the user to the login screen.

### Full Account Disconnect

If a user disconnects Google, Apple, or Facebook:

- Remove only the provider link if the account has another login method.
- If it is the only login method, require adding another provider or password first.
- Optionally call provider revocation endpoints where supported.

## Security Best Practices

- Use HTTPS for every backend redirect URI.
- Use Universal Links/App Links where possible; custom schemes are acceptable but weaker.
- Never place provider client secrets in the KMP app.
- Never trust `email` from the client.
- Verify ID token signatures and claims server-side.
- Validate `audience`, `issuer`, `expiry`, and `nonce`.
- Keep scopes minimal: `openid email profile` for Google, `name email` for Apple, `email public_profile` for Facebook.
- Store refresh tokens as hashes in PostgreSQL.
- Rotate Kaze refresh tokens on every use.
- Detect refresh-token reuse and revoke the token family.
- Rate-limit `/auth/start`, `/auth/callback`, `/auth/session/claim`, and `/auth/refresh`.
- Log authentication events without storing raw tokens or authorization codes.
- Keep provider access tokens short-lived and avoid storing them unless needed for a specific user-approved integration.
- Do not use embedded WebViews for social login.
- Add account-linking confirmation when two providers share an email.
- Treat Apple private relay email as a valid email but not proof that it belongs to another provider identity.
- Add admin tooling to revoke sessions by user/device.

## Authentication Cases And Current Compliance Audit

This section is the source of truth for the authentication cases Kaze must handle. The audit reflects
the repository as of **2026-07-01**. A feature mentioned in UI copy is not considered implemented
unless the client, server, persistence, and tests enforce it.

Status legend:

- **Implemented**: enforced by the current code and persistence model.
- **Partial**: some building blocks exist, but the complete security behavior is not enforced.
- **Missing**: no production implementation exists yet.
- **External check**: behavior depends on deployment or provider-console configuration and must be verified outside the repository.

### Identity And Account Linking

| Case | Required behavior | Status | Current evidence and gap |
|---|---|---|---|
| Email normalization and uniqueness | Lowercase/normalize email and keep one `app_users` row per email. | **Implemented** | `AuthService.normalizeEmail`, normalized inserts, and the unique `app_users.email` constraint enforce this. Do not strip provider-specific `+tag` or dot semantics. |
| Existing provider identity signs in again | Resolve by `(provider, provider_subject)` before considering email. | **Implemented** | `JdbcAuthRepository.upsertExternalUser` first resolves the stable provider subject. |
| Password first, then Google/Apple/Facebook | Link a verified provider email to the existing user instead of creating a second user. | **Implemented** | `upsertExternalUser` links verified matching email to the existing user. |
| Social first, then password | Add `PASSWORD` to the same user while already authenticated. | **Implemented** | `PUT /auth/me/password` updates the existing row and adds a password-provider link. |
| Unverified provider email matches an account | Refuse automatic linking. | **Implemented** | `upsertExternalUser` returns `external_email_not_verified`. |
| Password-signup email ownership | Verify the mailbox before treating the address as verified or enabling sensitive operations. | **Missing** | Password signup currently creates the `PASSWORD` provider with `email_verified=true` without sending a challenge. |
| Provider email changes | Continue resolving by provider subject; treat the new email as a separately verified profile change. | **Partial** | Stable subject lookup works, but there is no reviewed email-change workflow. |
| Apple private-relay email | Treat the relay as a valid address and never infer that it matches a non-relay address. | **Partial** | The guide states the rule; no explicit relay or account-link confirmation flow exists. |
| Two active accounts with different emails | Require explicit authenticated confirmation before merging. | **Missing** | There is no account-merge endpoint or UI. |
| Concurrent signup/social callbacks | Produce one user and an idempotent successful result under races. | **Partial** | Database uniqueness prevents duplicates, but racing inserts can still make one request fail instead of cleanly resolving the winner. |
| Disconnecting a provider | Allow unlink only when another usable login method remains. | **Missing** | No provider-disconnect endpoint is registered. |
| Disabled account | Reject password login, social login, refresh, and API access immediately. | **Missing** | `app_users.disabled` exists, but normal auth lookups and JWT validation do not enforce it. |

### Passwords, Recovery, And Step-Up Authentication

| Case | Required behavior | Status | Current evidence and gap |
|---|---|---|---|
| Password storage | Store only a salted adaptive hash and support work-factor migration. | **Implemented / review** | BCrypt cost 12 is used. Add an algorithm/work-factor migration plan; prefer Argon2id for new deployments when operationally available. |
| Password strength | For password-only login, require at least 15 characters, allow at least 64, accept spaces/Unicode, and reject common/compromised values. | **Missing** | Kaze currently requires only 8 characters and has no breached-password blocklist. This follows current [NIST SP 800-63B](https://pages.nist.gov/800-63-4/sp800-63b.html) guidance. |
| Password change | Require current password or recent provider/passkey reauthentication. | **Partial** | Existing password users must provide the current password. A social-only user can add a password with any still-valid access token; recent-authentication time is not checked. |
| Password change consequences | Rotate/revoke other sessions and notify the user. | **Missing** | Password updates do not revoke refresh-token families or send a security notice. |
| Forgotten password | Use a random, hashed, short-lived, single-use token and return the same response for existing and unknown accounts. | **Missing** | No forgot/reset routes or token table exist. Follow the [OWASP Forgot Password Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html). |
| Email change | Require recent reauthentication, verify the new address, notify the old address, and preserve uniqueness. | **Missing** | Email cannot currently be changed through the profile endpoint. |
| MFA for privileged users | Require a second factor or passkey for `STAFF` and `ADMIN`, with recovery codes and factor reset controls. | **Missing** | Roles exist, but no MFA authenticators, challenges, or recovery codes exist. |
| High-risk actions | Require recent step-up auth for password/email/provider changes, data export/deletion, staff actions, and payment-security changes. | **Missing** | A valid access token is currently sufficient for password addition and profile changes. |
| Account enumeration | Keep login and recovery responses/timing indistinguishable where possible. | **Partial** | Wrong-password login is generic, but signup and `password_login_not_configured` reveal account state. |

### OAuth And Social Provider Failures

| Case | Required behavior | Status | Current evidence and gap |
|---|---|---|---|
| Authorization-code flow | Use backend code exchange with state, nonce, PKCE, and one-time app login token. | **Implemented** | OAuth attempts store hashed state/nonce/verifier metadata; callback attempts and app login tokens are consumed once. |
| Token verification | Verify signature, issuer, audience, expiry, nonce, subject, and provider email claim rules. | **Implemented / partial** | Browser OAuth verifies the required claims and nonce. Direct `/auth/google` and `/auth/apple` ID-token routes pass `nonce=null`; replay protection should be defined for native credential flows. |
| Multiple Google client IDs | Accept every explicitly configured Android/iOS/web audience. | **Partial** | Direct Google sign-in verifies against `googleClientIds.first()` rather than selecting the matching configured audience. |
| User cancels or denies consent | Return a safe actionable error and leave no session behind. | **Partial** | Callback failures are handled, but provider-specific cancellation/denial tests are incomplete. |
| Missing provider email/name | Continue an already-linked identity by subject; require a verified email only when creating/linking a new account. | **Partial** | Token verification currently requires an email before repository subject lookup. |
| Provider outage/revocation | Fail safely, preserve the Kaze account, and offer another linked login/recovery method. | **Partial** | Generic provider failure exists; recovery and alternate-method guidance are incomplete. |
| App redirect/deep link | Accept only exact configured HTTPS/App Link/Universal Link destinations. | **Missing — high priority** | `/auth/{provider}/start` accepts caller-provided `appRedirectUri` without an allowlist. |
| Provider-console redirect configuration | Production redirect URIs, bundle IDs, package names, fingerprints, and secrets must match exactly. | **External check** | Verify in Google Cloud, Apple Developer, and Meta consoles for every environment. |

### Sessions, Devices, And Token Storage

| Case | Required behavior | Status | Current evidence and gap |
|---|---|---|---|
| Access/refresh token lifetimes | Use short access tokens and bounded refresh-token lifetime. | **Implemented / review** | Expiry is configured and verified. Reassess the current 12-hour access-token lifetime for privileged accounts. |
| Refresh rotation and replay | Rotate on every use; replay of a revoked token revokes its family. | **Implemented** | PostgreSQL row locking, replacement links, and family revocation are implemented. |
| One-device logout | Revoke the presented refresh token and clear local credentials. | **Implemented / partial** | Refresh-token revocation is database-backed; access-token denial is only process-local. |
| Multi-instance logout | A revoked access token must be rejected by every Cloud Run instance. | **Missing** | `revokedAccessTokenIds` is an in-memory map, so another instance can accept the token until expiry. |
| Active session list | Show device label, platform, first/last use, approximate location, and allow remote revocation. | **Missing** | Device columns exist, but there are no list/revoke-device routes or screens backed by session data. |
| Per-install device identity | Generate a random stable installation ID and store it in platform-secure storage. | **Missing** | Every client currently sends the literal `deviceId = "kaze-device"`, so devices cannot be distinguished. |
| Android token storage | Encrypt auth secrets with an Android Keystore key. | **Implemented / partial** | AES-GCM with an Android Keystore key is used, but the key is not gated by user authentication. |
| iOS token storage | Store auth secrets in Keychain with an appropriate accessibility/access-control policy. | **Implemented / partial** | Keychain storage exists, but no Face ID/Touch ID access-control flags are configured. |
| Web token storage | Keep bearer/refresh secrets out of JavaScript-readable web storage in production. | **Missing — high priority for web** | Auth tokens are stored in `sessionStorage`; use an HTTPS BFF with `HttpOnly`, `Secure`, `SameSite` cookies where possible. See [OWASP Session Management](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html). |
| Secure transport | Require HTTPS outside explicitly scoped local development. | **Partial** | Android cleartext is limited to local hosts. iOS currently sets `NSAllowsArbitraryLoads=true`, which must not ship in production. |
| No-store responses | Prevent tokens/private auth responses from being cached. | **Implemented** | Auth routes and sensitive responses set restrictive cache headers. |

### Abuse Resistance, Authorization, And Account Lifecycle

| Case | Required behavior | Status | Current evidence and gap |
|---|---|---|---|
| Brute force and credential stuffing | Layer per-account, per-IP, and risk-based throttling; avoid easy account-lockout denial of service. | **Partial** | Ktor applies 20 auth requests/minute per client IP and route. There is no per-account counter, progressive delay, breached-password detection, or suspicious-login response. |
| New-device alerting | Notify the user after a new device/authenticator signs in and provide a revoke action. | **Missing** | No unique device identity or auth-event notification exists. |
| Auth audit log | Record success/failure, provider linking, password/reset, authenticator changes, refresh replay, logout, and administrative recovery without secrets. | **Missing** | Request logs exist, but there is no durable authentication-event model. |
| Object ownership/tenant isolation | Every protected lookup must verify the user, role, resource owner, and hotel/venue/event tenant. | **Partial — ongoing audit required** | Guest ownership and selected event/staff checks exist, but there is no completed endpoint-by-endpoint authorization matrix. |
| Role/permission change | Invalidate or version existing JWT authorization claims when roles change. | **Missing** | Roles are copied into access JWTs and remain valid until token expiry. |
| Lost/stolen device | Let the user revoke the device, its refresh families, device trust, and device-bound credentials. | **Missing** | Only the locally presented refresh token can currently be revoked. |
| Account deletion | Reauthenticate, revoke all sessions/providers/passkeys, apply retention rules, and notify the user. | **Missing** | Settings directs users to support; no authenticated deletion workflow exists. |
| Support/admin recovery | Require strong identity proof, two-person controls for sensitive recovery where appropriate, and durable audit logs. | **Missing** | No recovery tooling exists. |

## Remembered Sessions, Recognized Devices, Trusted Devices, Biometrics, And Passkeys

These are different security concepts and must not be represented as the same feature:

1. **Remembered session**: a refresh token keeps the user signed in. It is convenience, not proof that a device is trusted.
2. **Recognized device**: Kaze remembers a random per-install identifier and device metadata as a risk signal. An identifier alone is forgeable and is not an authenticator.
3. **Trusted device**: the device proves possession of a non-exportable private key that the user explicitly registered. Trust is revocable, expires, and does not bypass high-risk step-up requirements.
4. **Biometric app unlock**: Android fingerprint/face or Apple Face ID/Touch ID locally unlocks a key or credential. Kaze must never receive or store biometric templates.
5. **Passkey**: a WebAuthn/FIDO public-key credential authenticates to the server. Device biometrics or device PIN commonly activate the credential locally. A synced passkey may be available on several devices, so it is not automatically equivalent to one trusted physical device.

NIST explicitly distinguishes a short-term remembered session from a device authenticator: a cookie or bearer
session token alone is not proof of possession of a trusted physical authenticator. See
[NIST SP 800-63B authenticator requirements](https://pages.nist.gov/800-63-4/sp800-63b.html) and
[session requirements](https://pages.nist.gov/800-63-4/sp800-63b/session/).

### Trusted Device Design

Kaze should implement trusted devices as an optional, explicit binding after a full authentication:

1. Generate a random per-install identifier. Do not use an advertising ID, IMEI, serial number, or invasive fingerprint.
2. Generate a non-exportable device key pair in Android Keystore or Apple Secure Enclave/Keychain.
3. Send the public key and server-issued single-use challenge after recent authentication.
4. Store a server device record with user, public key, label, platform, first/last seen time, trust time, expiry, revocation time, and risk metadata.
5. For recognized-device proof, ask the device to sign a fresh server challenge. Verify signature, challenge, user, expiry, revocation, and intended action.
6. Gate private-key use with the system biometric/device-credential prompt where the platform supports it.
7. Require normal or stronger authentication for password/email/provider changes, payments, account deletion, staff/admin actions, or suspicious context even on a trusted device.
8. Show all recognized/trusted devices and provide “revoke device” and “sign out all sessions” controls.
9. Notify the user when a device is first trusted or used from a meaningfully new context.
10. Define recovery for a replaced/lost phone without weakening normal authentication.

Suggested persistence additions:

```sql
CREATE TABLE auth_devices (
    id VARCHAR(120) PRIMARY KEY,
    user_id VARCHAR(120) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    installation_id_hash TEXT NOT NULL,
    public_key_cose BYTEA,
    platform VARCHAR(40) NOT NULL,
    device_label VARCHAR(240),
    first_seen_at TIMESTAMPTZ NOT NULL,
    last_seen_at TIMESTAMPTZ NOT NULL,
    trusted_at TIMESTAMPTZ,
    trust_expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    UNIQUE (user_id, installation_id_hash)
);
```

Store only the minimum device metadata required for security and explain it in the privacy policy.

### Fingerprint, Face ID, Touch ID, And Device Credential

Biometrics should be a local activation factor, not a biometric database owned by Kaze.

Android implementation requirements:

- Use Credential Manager for initial sign-in/passkeys and `BiometricPrompt` for local reauthorization.
- Support `BIOMETRIC_STRONG` with an intentional `DEVICE_CREDENTIAL` fallback where the risk model allows it.
- For token/app-lock protection, generate a Keystore key with `setUserAuthenticationRequired(true)` and use a `CryptoObject` when appropriate.
- Handle no hardware, no enrollment, temporary lockout, permanent lockout, changed enrollment, canceled prompt, and device-credential fallback.
- Never treat a callback from a custom biometric UI as proof; use the system prompt and protected key operation.

See [Android biometric authentication](https://developer.android.com/identity/sign-in/biometric-auth) and
[Credential Manager passkey/biometric integration](https://developer.android.com/identity/sign-in/single-tap-biometric).

iOS implementation requirements:

- Use LocalAuthentication for local Face ID/Touch ID/device-passcode checks.
- Protect Keychain/private-key items with an access-control policy requiring user presence or current biometric set where appropriate.
- Add a clear `NSFaceIDUsageDescription` before invoking Face ID.
- Handle unavailable/not-enrolled biometrics, lockout, passcode fallback, canceled prompts, and biometric-set changes.
- Keep passkey flows in AuthenticationServices; do not manually handle private keys or biometric data.

See [Apple LocalAuthentication](https://developer.apple.com/documentation/localauthentication).

Current status: **Missing**. Android Keystore and iOS Keychain storage exist, but neither is gated by
biometric/device authentication. There is no biometric service or prompt flow, and iOS lacks
`NSFaceIDUsageDescription`. The Security settings text is currently descriptive copy only.

### Passkeys (WebAuthn/FIDO)

Passkeys should become a first-class provider in `user_auth_providers` or, preferably, a dedicated
credential table because one user can register multiple passkeys:

```sql
CREATE TABLE user_passkey_credentials (
    credential_id TEXT PRIMARY KEY,
    user_id VARCHAR(120) NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    public_key_cose BYTEA NOT NULL,
    sign_count BIGINT NOT NULL DEFAULT 0,
    transports TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    backup_eligible BOOLEAN NOT NULL DEFAULT false,
    backed_up BOOLEAN NOT NULL DEFAULT false,
    label VARCHAR(240),
    created_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ
);
```

Required server flows:

```text
POST   /api/v1/auth/passkeys/registration/options
POST   /api/v1/auth/passkeys/registration/verify
POST   /api/v1/auth/passkeys/authentication/options
POST   /api/v1/auth/passkeys/authentication/verify
GET    /api/v1/auth/me/passkeys
DELETE /api/v1/auth/me/passkeys/{credentialId}
```

Registration and authentication requirements:

- Generate a cryptographically random, short-lived, single-use server challenge and bind it to the intended ceremony and user/session.
- Verify challenge, RP ID hash, allowed origin/app association, credential type, signature, user presence, requested user-verification policy, and credential revocation state.
- Persist only the public key and credential metadata; the private key stays with the platform credential provider.
- Permit multiple credentials, give each a recognizable label, show last use, and let users revoke them.
- Require recent authentication to add or remove a passkey; never allow removal of the last recovery/login method without a safe replacement.
- Handle synced and device-bound credentials, cross-device sign-in, sign-counter limitations, lost devices, provider sync loss, and account recovery.
- Configure Android Digital Asset Links and Apple Associated Domains using the same relying-party domain as the web experience.
- Prefer usernameless/discoverable credentials where the Kaze UX supports them, while keeping account selection and privacy behavior explicit.

Android should use Credential Manager. Apple platforms should use AuthenticationServices. See
[Android passkeys](https://developer.android.com/identity/passkeys),
[Android passkey sign-in](https://developer.android.com/identity/passkeys/sign-in-with-passkeys), and
[Apple supporting passkeys](https://developer.apple.com/documentation/authenticationservices/supporting-passkeys).

Current status: **Missing**. There are no WebAuthn/passkey dependencies, challenge routes, credential
tables, Android asset association, Apple `webcredentials` associated domain, or passkey tests.

### Recommended Delivery Order

1. Fix foundational gaps: password-email verification, password reset, disabled-user enforcement, redirect allowlist, generic recovery responses, and durable auth-event logging.
2. Replace the static client device ID with a random per-install identifier and add session/device list plus remote revocation.
3. Add biometric-gated local app/token unlock on Android and iOS, with device credential/passcode fallback and recovery.
4. Add passkey registration/authentication and credential management using server challenges and platform APIs.
5. Add explicit trusted-device key binding only if Kaze needs a trust signal beyond passkeys and remembered sessions.
6. Add MFA/step-up policy for staff/admin and high-risk customer actions.
7. Run an endpoint-by-endpoint authorization, abuse, privacy, recovery, and multi-instance deployment test before calling auth production-complete.

## Recommended Kaze API Surface

```text
GET  /api/v1/auth/{provider}/start
GET  /api/v1/auth/google/callback
POST /api/v1/auth/apple/callback
GET  /api/v1/auth/facebook/callback
POST /api/v1/auth/session/claim
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /api/v1/auth/me
PUT  /api/v1/auth/me/password
GET  /api/v1/auth/sessions
DELETE /api/v1/auth/sessions/{sessionId}
POST /api/v1/auth/providers/{provider}/disconnect
POST /api/v1/auth/passkeys/registration/options
POST /api/v1/auth/passkeys/registration/verify
POST /api/v1/auth/passkeys/authentication/options
POST /api/v1/auth/passkeys/authentication/verify
GET  /api/v1/auth/me/passkeys
DELETE /api/v1/auth/me/passkeys/{credentialId}
GET  /api/v1/auth/me/devices
DELETE /api/v1/auth/me/devices/{deviceId}
```

`PUT /api/v1/auth/me/password` lets an authenticated social-login user add password login to the
same `app_users` row. Kaze stores a separate `PASSWORD` provider link; it never creates another user
for the same normalized email. If a password already exists, the current password is required before
it can be replaced.

## Recommended Kaze Implementation Phases

### Phase 1: Core Social Login

- Add provider configuration.
- Add OAuth start routes.
- Add callback routes.
- Verify Google and Apple ID tokens.
- Validate Facebook access tokens and fetch profile.
- Upsert user/provider account records.
- Issue Kaze JWT + refresh token.

### Phase 2: Production Session Hardening

- Add one-time login token claim flow.
- Add refresh token rotation.
- Add token family reuse detection.
- Add session list and revoke-device endpoints.
- Add rate limits and audit logs.

### Phase 3: Account Linking

- Link providers to existing accounts.
- Add explicit account-link confirmation UI.
- Add provider disconnect flow.
- Add support for Apple private relay email edge cases.

### Phase 4: Risk Controls

- Suspicious login detection.
- New-device notification.
- Optional step-up verification for organizer/staff accounts.
- Admin session revocation tools.

### Phase 5: Device And Passwordless Authentication

- Generate a unique per-install device ID and add active-device management.
- Add biometric-gated local app unlock on Android and iOS.
- Add passkey registration, authentication, management, and recovery.
- Add explicit trusted-device key binding only where the risk model needs it.
- Add MFA/passkey step-up for privileged roles and sensitive actions.

## Testing Checklist

- Google login creates a Kaze user.
- Apple login creates a Kaze user and stores first-login email/name.
- Facebook login creates a Kaze user using verified Meta user ID.
- Existing provider identity signs into the same user.
- Verified email can link a new provider to an existing user only under safe rules.
- Invalid state is rejected.
- Expired state is rejected.
- Reused callback code is rejected.
- Refresh token rotates on every use.
- Reused old refresh token revokes the token family.
- Logout revokes the current refresh token.
- App restart uses Kaze refresh token without calling social providers.
- Deep link with missing login token is rejected.
- Deep link with wrong state is rejected.
- Password signup does not become trusted until email verification succeeds.
- Disabled users cannot sign in, refresh, or use an existing access token.
- Password reset responses do not reveal whether an account exists.
- Password reset tokens are hashed, single-use, expiring, and invalidate as designed.
- Password/email/provider/passkey changes require recent reauthentication and create an audit event.
- Password changes revoke or intentionally preserve other sessions according to a tested policy.
- Provider disconnect cannot remove the final usable login method.
- Concurrent password/social/passkey registration produces one user and an idempotent result.
- Every installed app instance receives a distinct random device identifier.
- A remembered refresh-token session is never displayed as a cryptographically trusted device.
- Trusted-device proof rejects a wrong challenge, key, user, action, expiry, or revoked device.
- Lost-device revocation invalidates refresh families, trust keys, and device-bound credentials as applicable.
- Android biometric success is tied to a system prompt and protected key operation.
- iOS Face ID/Touch ID success is tied to LocalAuthentication/Keychain access control.
- Biometric unavailable, not enrolled, lockout, enrollment change, cancellation, and fallback paths are tested.
- Passkey registration rejects the wrong RP ID, origin/app association, challenge, or user.
- Passkey authentication rejects replayed/expired challenges, invalid signatures, and revoked credentials.
- Multiple, synced, device-bound, cross-device, renamed, and revoked passkeys are tested.
- Removing the final passkey/login/recovery method is refused.
- New-device and authenticator-change alerts contain no secrets and link to revocation controls.
- Logout and revocation work across multiple server instances.
- Web auth secrets are not exposed to JavaScript-readable storage in the production architecture.
- Each protected endpoint passes user ownership, tenant isolation, and role authorization tests.

## Configuration Example

```hocon
kaze {
  auth {
    appDeepLinkRedirect = "kaze://auth/callback"

    jwt {
      issuer = "kaze-api"
      audience = "kaze-mobile"
      accessTokenTtlSeconds = 900
      refreshTokenTtlDays = 60
    }

    google {
      clientId = ${?GOOGLE_OAUTH_CLIENT_ID}
      clientSecret = ${?GOOGLE_OAUTH_CLIENT_SECRET}
      redirectUri = ${?GOOGLE_OAUTH_REDIRECT_URI}
    }

    apple {
      teamId = ${?APPLE_TEAM_ID}
      keyId = ${?APPLE_KEY_ID}
      serviceId = ${?APPLE_SERVICE_ID}
      privateKeyPem = ${?APPLE_PRIVATE_KEY_PEM}
      redirectUri = ${?APPLE_REDIRECT_URI}
    }

    facebook {
      appId = ${?FACEBOOK_APP_ID}
      appSecret = ${?FACEBOOK_APP_SECRET}
      redirectUri = ${?FACEBOOK_REDIRECT_URI}
    }
  }
}
```

## Operational Notes

- Use separate OAuth clients/apps for development, staging, and production.
- Never reuse production provider credentials in local development.
- Keep redirect URIs exact; provider consoles usually require exact scheme, host, path, and sometimes trailing slash matching.
- Add monitoring for login success rate, callback failures, invalid state, and token refresh failures.
- Add a support flow for users who lose access to a social account.
- Treat remembered sessions, recognized devices, trusted devices, biometrics, and passkeys as separate controls.
- Keep recovery at least as strong as the authenticators it can replace.

## References

- [Ktor Authentication and Authorization](https://ktor.io/docs/server-auth.html)
- [Ktor Bearer Authentication](https://ktor.io/docs/server-bearer-auth.html)
- [Google OAuth 2.0 for Web Server Applications](https://developers.google.com/identity/protocols/oauth2/web-server)
- [Sign in with Apple REST API](https://developer.apple.com/documentation/signinwithapplerestapi)
- [Apple Token Validation](https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens)
- [Apple Authorization Request Endpoint](https://developer.apple.com/documentation/signinwithapplerestapi/request-an-authorization-to-the-sign-in-with-apple-server.)
- [Meta Facebook Login: Manually Build a Login Flow](https://developers.facebook.com/docs/facebook-login/guides/advanced/manual-flow)
- [Meta Graph API: Access Tokens](https://developers.facebook.com/docs/facebook-login/guides/access-tokens)
- [OAuth 2.0 Redirect URI Guidance](https://www.oauth.com/oauth2-servers/redirect-uris/)
- [NIST SP 800-63B: Authentication and Authenticator Management](https://pages.nist.gov/800-63-4/sp800-63b.html)
- [NIST SP 800-63B: Session Management](https://pages.nist.gov/800-63-4/sp800-63b/session/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [OWASP Forgot Password Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html)
- [OWASP Session Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)
- [OWASP OAuth 2.0 Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/OAuth2_Cheat_Sheet.html)
- [Android Credential Manager and Passkeys](https://developer.android.com/identity/passkeys)
- [Android Biometric Authentication](https://developer.android.com/identity/sign-in/biometric-auth)
- [Apple LocalAuthentication](https://developer.apple.com/documentation/localauthentication)
- [Apple Passkeys](https://developer.apple.com/documentation/authenticationservices/supporting-passkeys)
