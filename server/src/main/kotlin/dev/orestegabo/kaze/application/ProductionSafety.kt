package dev.orestegabo.kaze.application

import dev.orestegabo.kaze.auth.DEFAULT_DEV_JWT_SECRET
import dev.orestegabo.kaze.auth.JwtConfig
import dev.orestegabo.kaze.infrastructure.DatabaseConfig
import dev.orestegabo.kaze.infrastructure.DatabaseSchemaMode
import io.ktor.server.application.Application

internal fun Application.validateProductionSafety(
    databaseConfig: DatabaseConfig,
    jwtConfig: JwtConfig,
) {
    if (!isProductionEnvironment()) return

    require(jwtConfig.requireJwtForApi) {
        "Refusing to start production with private API routes unauthenticated. Set KAZE_JWT_REQUIRE_FOR_API=true."
    }
    require(jwtConfig.secret != DEFAULT_DEV_JWT_SECRET && jwtConfig.secret.length >= MIN_PRODUCTION_JWT_SECRET_LENGTH) {
        "Refusing to start production with a missing, dev, or short KAZE_JWT_SECRET."
    }
    require(databaseConfig.schemaMode != DatabaseSchemaMode.DROP && databaseConfig.schemaMode != DatabaseSchemaMode.CREATE_DROP) {
        "Refusing to start production with destructive KAZE_DB_SCHEMA_MODE=${databaseConfig.schemaMode.name.lowercase()}."
    }
    val corsHosts = (
        environment.config.propertyOrNull("kaze.security.cors.allowedHosts")?.getString()
            ?: System.getenv("KAZE_CORS_ALLOWED_HOSTS")
        )
        ?.split(",")
        ?.map { it.trim().lowercase() }
        ?.filter { it.isNotEmpty() }
        .orEmpty()
    require(corsHosts.isNotEmpty() && corsHosts.none { it.startsWith("localhost") || it.startsWith("127.0.0.1") }) {
        "Refusing to start production without explicit non-local KAZE_CORS_ALLOWED_HOSTS."
    }
}

internal fun isProductionEnvironment(): Boolean {
    val markers = listOf(
        System.getenv("KAZE_ENV"),
        System.getenv("APP_ENV"),
        System.getenv("ENVIRONMENT"),
        System.getenv("KTOR_ENV"),
    ).mapNotNull { it?.trim()?.lowercase()?.takeIf(String::isNotEmpty) }

    return markers.any { it in PRODUCTION_ENV_NAMES } ||
        System.getenv("K_SERVICE")?.isNotBlank() == true
}

private val PRODUCTION_ENV_NAMES = setOf("prod", "production", "live")
private const val MIN_PRODUCTION_JWT_SECRET_LENGTH = 32
