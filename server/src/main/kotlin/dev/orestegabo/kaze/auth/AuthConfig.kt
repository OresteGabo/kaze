package dev.orestegabo.kaze.auth

import io.ktor.server.application.Application
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

internal data class JwtConfig(
    val issuer: String,
    val audience: String,
    val realm: String,
    val secret: String,
    val accessTokenTtlSeconds: Long,
    val refreshTokenTtlSeconds: Long,
    val oneTimeLoginTokenTtlSeconds: Long,
    val requireJwtForApi: Boolean,
    val googleClientIds: Set<String>,
    val appleClientIds: Set<String>,
    val socialAuth: SocialAuthConfig,
)

internal data class SocialAuthConfig(
    val appDeepLinkRedirect: String,
    val google: OAuthProviderConfig,
    val apple: AppleOAuthProviderConfig,
    val facebook: OAuthProviderConfig,
)

internal data class OAuthProviderConfig(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val authorizeUrl: String,
    val tokenUrl: String,
    val scopes: List<String>,
) {
    val isConfigured: Boolean
        get() = clientId.isNotBlank() && redirectUri.isNotBlank()
}

internal data class AppleOAuthProviderConfig(
    val clientId: String,
    val teamId: String,
    val keyId: String,
    val privateKeyPem: String,
    val redirectUri: String,
    val authorizeUrl: String,
    val tokenUrl: String,
    val scopes: List<String>,
) {
    val isConfigured: Boolean
        get() = clientId.isNotBlank() && teamId.isNotBlank() && keyId.isNotBlank() &&
            privateKeyPem.isNotBlank() && redirectUri.isNotBlank()
}

internal fun Application.loadJwtConfig(): JwtConfig =
    run {
        val googleConfig = OAuthProviderConfig(
            clientId = configString("kaze.security.oauth.google.clientId", "", "GOOGLE_OAUTH_CLIENT_ID"),
            clientSecret = configString("kaze.security.oauth.google.clientSecret", "", "GOOGLE_OAUTH_CLIENT_SECRET"),
            redirectUri = configString("kaze.security.oauth.google.redirectUri", "", "GOOGLE_OAUTH_REDIRECT_URI"),
            authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth",
            tokenUrl = "https://oauth2.googleapis.com/token",
            scopes = listOf("openid", "email", "profile"),
        )
        val appleConfig = AppleOAuthProviderConfig(
            clientId = configString("kaze.security.oauth.apple.clientId", "", "APPLE_SERVICE_ID"),
            teamId = configString("kaze.security.oauth.apple.teamId", "", "APPLE_TEAM_ID"),
            keyId = configString("kaze.security.oauth.apple.keyId", "", "APPLE_KEY_ID"),
            privateKeyPem = configString("kaze.security.oauth.apple.privateKeyPem", "", "APPLE_PRIVATE_KEY_PEM"),
            redirectUri = configString("kaze.security.oauth.apple.redirectUri", "", "APPLE_REDIRECT_URI"),
            authorizeUrl = "https://appleid.apple.com/auth/authorize",
            tokenUrl = "https://appleid.apple.com/auth/token",
            scopes = listOf("name", "email"),
        )

        JwtConfig(
            issuer = configString("kaze.security.jwt.issuer", "kaze-api", "KAZE_JWT_ISSUER"),
            audience = configString("kaze.security.jwt.audience", "kaze-mobile", "KAZE_JWT_AUDIENCE"),
            realm = configString("kaze.security.jwt.realm", "Kaze API", "KAZE_JWT_REALM"),
            secret = configString("kaze.security.jwt.secret", DEFAULT_DEV_JWT_SECRET, "KAZE_JWT_SECRET"),
            accessTokenTtlSeconds = configString("kaze.security.jwt.accessTokenTtlSeconds", DEFAULT_ACCESS_TOKEN_TTL_SECONDS.toString(), "KAZE_JWT_ACCESS_TOKEN_TTL_SECONDS").toLong(),
            refreshTokenTtlSeconds = configString("kaze.security.jwt.refreshTokenTtlSeconds", DEFAULT_REFRESH_TOKEN_TTL_SECONDS.toString(), "KAZE_JWT_REFRESH_TOKEN_TTL_SECONDS").toLong(),
            oneTimeLoginTokenTtlSeconds = configString("kaze.security.jwt.oneTimeLoginTokenTtlSeconds", DEFAULT_ONE_TIME_LOGIN_TOKEN_TTL_SECONDS.toString(), "KAZE_JWT_ONE_TIME_LOGIN_TOKEN_TTL_SECONDS").toLong(),
            requireJwtForApi = configString("kaze.security.jwt.requireForApi", "false", "KAZE_JWT_REQUIRE_FOR_API").toBooleanStrictOrNull() ?: false,
            googleClientIds = configCsv("kaze.security.oauth.googleClientIds", "KAZE_GOOGLE_CLIENT_IDS")
                .ifEmpty { googleConfig.clientId.takeIf { it.isNotBlank() }?.let(::setOf) ?: emptySet() },
            appleClientIds = configCsv("kaze.security.oauth.appleClientIds", "KAZE_APPLE_CLIENT_IDS")
                .ifEmpty { appleConfig.clientId.takeIf { it.isNotBlank() }?.let(::setOf) ?: emptySet() },
            socialAuth = SocialAuthConfig(
                appDeepLinkRedirect = configString("kaze.security.oauth.appDeepLinkRedirect", "kaze://auth/callback", "KAZE_AUTH_APP_DEEP_LINK_REDIRECT"),
                google = googleConfig,
                apple = appleConfig,
                facebook = OAuthProviderConfig(
                    clientId = configString("kaze.security.oauth.facebook.appId", "", "FACEBOOK_APP_ID"),
                    clientSecret = configString("kaze.security.oauth.facebook.appSecret", "", "FACEBOOK_APP_SECRET"),
                    redirectUri = configString("kaze.security.oauth.facebook.redirectUri", "", "FACEBOOK_REDIRECT_URI"),
                    authorizeUrl = "https://www.facebook.com/v19.0/dialog/oauth",
                    tokenUrl = "https://graph.facebook.com/v19.0/oauth/access_token",
                    scopes = listOf("email", "public_profile"),
                ),
            ),
        )
    }.also { config ->
        environment.log.info(
            "Social auth config loaded. googleConfigured={}, appleConfigured={}, facebookConfigured={}",
            config.socialAuth.google.isConfigured && config.socialAuth.google.clientSecret.isNotBlank(),
            config.socialAuth.apple.isConfigured,
            config.socialAuth.facebook.isConfigured && config.socialAuth.facebook.clientSecret.isNotBlank(),
        )
    }

private fun Application.configString(path: String, default: String, envVar: String? = null): String =
    environment.config.propertyOrNull(path)?.getString()?.trim()?.takeIf { it.isNotEmpty() }
        ?: envVar?.let(System::getenv)?.trim()?.takeIf { it.isNotEmpty() }
        ?: default

private fun Application.configCsv(path: String, envVar: String? = null): Set<String> =
    (
        environment.config.propertyOrNull(path)?.getString()
            ?: envVar?.let(System::getenv)
        )
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?: emptySet()

private const val DEFAULT_DEV_JWT_SECRET = "change-this-dev-only-kaze-jwt-secret-at-least-32-chars"
private val DEFAULT_ACCESS_TOKEN_TTL_SECONDS = 12.hours.inWholeSeconds
private val DEFAULT_REFRESH_TOKEN_TTL_SECONDS = 60.days.inWholeSeconds
private const val DEFAULT_ONE_TIME_LOGIN_TOKEN_TTL_SECONDS = 120L
