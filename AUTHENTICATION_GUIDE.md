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

```sql
CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email TEXT,
    display_name TEXT,
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX app_users_email_unique_idx
ON app_users (lower(email))
WHERE email IS NOT NULL;

CREATE TABLE auth_provider_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    provider_type TEXT NOT NULL CHECK (provider_type IN ('GOOGLE', 'APPLE', 'FACEBOOK')),
    social_id TEXT NOT NULL,
    email TEXT,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    display_name TEXT,
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (provider_type, social_id)
);

CREATE INDEX auth_provider_accounts_user_id_idx
ON auth_provider_accounts(user_id);

CREATE TABLE auth_refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    device_id TEXT,
    device_label TEXT,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_token_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at TIMESTAMPTZ
);

CREATE INDEX auth_refresh_tokens_user_id_idx
ON auth_refresh_tokens(user_id);

CREATE TABLE oauth_login_attempts (
    id UUID PRIMARY KEY,
    provider_type TEXT NOT NULL CHECK (provider_type IN ('GOOGLE', 'APPLE', 'FACEBOOK')),
    state_hash TEXT NOT NULL UNIQUE,
    code_verifier_hash TEXT NOT NULL,
    nonce_hash TEXT,
    app_redirect_uri TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ
);

CREATE TABLE auth_one_time_login_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ
);
```

### Duplicate Account Prevention

Use this account-linking order:

1. If `(provider_type, social_id)` exists, sign in that user.
2. Else if provider email is verified and matches an existing Kaze user email, link the provider to that user after passing safety checks.
3. Else create a new user and link the provider.

Safety checks:

- Never link on unverified email.
- Handle Apple private relay emails as real emails but avoid assuming they match other providers.
- Require explicit user confirmation before linking two active accounts with different emails.
- Store provider social IDs as opaque strings; never infer meaning from them.

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
    val providerType: SocialProviderType,
    val socialId: String,
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
- Use `sub` as `socialId`.

Apple verification:

- Fetch Apple JWKS.
- Verify `id_token` signature.
- Validate `iss = https://appleid.apple.com`, `aud`, `exp`, and optional `nonce`.
- Use `sub` as `socialId`.
- Persist email/name on first login if present.

Facebook verification:

- Exchange code for access token.
- Validate with Meta debug token endpoint using an app access token.
- Confirm `is_valid`, `app_id`, expiry, and user ID.
- Fetch profile:

```text
GET https://graph.facebook.com/me?fields=id,name,email,picture&access_token=...
```

- Use `id` as `socialId`.

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
GET  /api/v1/auth/sessions
DELETE /api/v1/auth/sessions/{sessionId}
POST /api/v1/auth/providers/{provider}/disconnect
```

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
- Keep a migration path for adding password login or passkeys later.

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
