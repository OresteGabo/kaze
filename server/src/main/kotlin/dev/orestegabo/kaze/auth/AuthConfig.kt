package dev.orestegabo.kaze.auth

import io.ktor.server.application.Application
import kotlin.time.Duration.Companion.hours

internal data class JwtConfig(
    val issuer: String,
    val audience: String,
    val realm: String,
    val secret: String,
    val accessTokenTtlSeconds: Long,
    val requireJwtForApi: Boolean,
    val googleClientIds: Set<String>,
    val appleClientIds: Set<String>,
)

internal fun Application.loadJwtConfig(): JwtConfig =
    JwtConfig(
        issuer = configString("kaze.security.jwt.issuer", "kaze-api"),
        audience = configString("kaze.security.jwt.audience", "kaze-mobile"),
        realm = configString("kaze.security.jwt.realm", "Kaze API"),
        secret = configString("kaze.security.jwt.secret", DEFAULT_DEV_JWT_SECRET),
        accessTokenTtlSeconds = configString("kaze.security.jwt.accessTokenTtlSeconds", DEFAULT_ACCESS_TOKEN_TTL_SECONDS.toString()).toLong(),
        requireJwtForApi = configString("kaze.security.jwt.requireForApi", "false").toBooleanStrictOrNull() ?: false,
        googleClientIds = configCsv("kaze.security.oauth.googleClientIds"),
        appleClientIds = configCsv("kaze.security.oauth.appleClientIds"),
    )

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
