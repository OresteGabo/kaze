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
            clientId = configString("kaze.security.oauth.google.clientId", ""),
            clientSecret = configString("kaze.security.oauth.google.clientSecret", ""),
            redirectUri = configString("kaze.security.oauth.google.redirectUri", ""),
            authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth",
            tokenUrl = "https://oauth2.googleapis.com/token",
            scopes = listOf("openid", "email", "profile"),
        )
        val appleConfig = AppleOAuthProviderConfig(
            clientId = configString("kaze.security.oauth.apple.clientId", ""),
            teamId = configString("kaze.security.oauth.apple.teamId", ""),
            keyId = configString("kaze.security.oauth.apple.keyId", ""),
            privateKeyPem = configString("kaze.security.oauth.apple.privateKeyPem", ""),
            redirectUri = configString("kaze.security.oauth.apple.redirectUri", ""),
            authorizeUrl = "https://appleid.apple.com/auth/authorize",
            tokenUrl = "https://appleid.apple.com/auth/token",
            scopes = listOf("name", "email"),
        )

        JwtConfig(
            issuer = configString("kaze.security.jwt.issuer", "kaze-api"),
            audience = configString("kaze.security.jwt.audience", "kaze-mobile"),
            realm = configString("kaze.security.jwt.realm", "Kaze API"),
            secret = configString("kaze.security.jwt.secret", DEFAULT_DEV_JWT_SECRET),
            accessTokenTtlSeconds = configString("kaze.security.jwt.accessTokenTtlSeconds", DEFAULT_ACCESS_TOKEN_TTL_SECONDS.toString()).toLong(),
            refreshTokenTtlSeconds = configString("kaze.security.jwt.refreshTokenTtlSeconds", DEFAULT_REFRESH_TOKEN_TTL_SECONDS.toString()).toLong(),
            oneTimeLoginTokenTtlSeconds = configString("kaze.security.jwt.oneTimeLoginTokenTtlSeconds", DEFAULT_ONE_TIME_LOGIN_TOKEN_TTL_SECONDS.toString()).toLong(),
            requireJwtForApi = configString("kaze.security.jwt.requireForApi", "false").toBooleanStrictOrNull() ?: false,
            googleClientIds = configCsv("kaze.security.oauth.googleClientIds")
                .ifEmpty { googleConfig.clientId.takeIf { it.isNotBlank() }?.let(::setOf) ?: emptySet() },
            appleClientIds = configCsv("kaze.security.oauth.appleClientIds")
                .ifEmpty { appleConfig.clientId.takeIf { it.isNotBlank() }?.let(::setOf) ?: emptySet() },
            socialAuth = SocialAuthConfig(
                appDeepLinkRedirect = configString("kaze.security.oauth.appDeepLinkRedirect", "kaze://auth/callback"),
                google = googleConfig,
                apple = appleConfig,
                facebook = OAuthProviderConfig(
                    clientId = configString("kaze.security.oauth.facebook.appId", ""),
                    clientSecret = configString("kaze.security.oauth.facebook.appSecret", ""),
                    redirectUri = configString("kaze.security.oauth.facebook.redirectUri", ""),
                    authorizeUrl = "https://www.facebook.com/v19.0/dialog/oauth",
                    tokenUrl = "https://graph.facebook.com/v19.0/oauth/access_token",
                    scopes = listOf("email", "public_profile"),
                ),
            ),
        )
    }

private fun Application.configString(path: String, default: String): String =
    environment.config.propertyOrNull(path)?.getString()?.trim()?.takeIf { it.isNotEmpty() } ?: default

private fun Application.configCsv(path: String): Set<String> =
    environment.config.propertyOrNull(path)
        ?.getString()
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?: emptySet()

private const val DEFAULT_DEV_JWT_SECRET = "change-this-dev-only-kaze-jwt-secret-at-least-32-chars"
private val DEFAULT_ACCESS_TOKEN_TTL_SECONDS = 12.hours.inWholeSeconds
private val DEFAULT_REFRESH_TOKEN_TTL_SECONDS = 60.days.inWholeSeconds
private const val DEFAULT_ONE_TIME_LOGIN_TOKEN_TTL_SECONDS = 120L
