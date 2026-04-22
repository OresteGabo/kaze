package dev.orestegabo.kaze.infrastructure

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal data class DatabaseConfig(
    val driver: String,
    val jdbcUrl: String,
    val user: String,
    val password: String,
    val maximumPoolSize: Int,
    val schemaMode: DatabaseSchemaMode,
)

internal fun Application.loadDatabaseConfig(): DatabaseConfig =
    run {
        val databaseUrlEnv = System.getenv("DATABASE_URL")?.trim()?.takeIf { it.isNotEmpty() }
        val parsedConnection = parseDatabaseConnection(
            databaseUrlEnv
                ?: environment.config.propertyOrNull("kaze.database.url")?.getString()
                ?: "jdbc:postgresql://localhost:5432/kaze",
        )
        val explicitUser = System.getenv("DATABASE_USER")
            ?: System.getenv("KAZE_DB_USER")
            ?: environment.config.propertyOrNull("kaze.database.user")?.getString()
        val explicitPassword = System.getenv("DATABASE_PASSWORD")
            ?: System.getenv("KAZE_DB_PASSWORD")
            ?: environment.config.propertyOrNull("kaze.database.password")?.getString()

        DatabaseConfig(
            driver = environment.config.propertyOrNull("kaze.database.driver")?.getString() ?: "org.postgresql.Driver",
            jdbcUrl = parsedConnection.jdbcUrl,
            user = when {
                explicitUser != null -> explicitUser
                parsedConnection.user != null -> parsedConnection.user
                else -> "postgres"
            },
            password = when {
                explicitPassword != null -> explicitPassword
                parsedConnection.password != null -> parsedConnection.password
                else -> "Muhirehonore@1*"
            },
            maximumPoolSize = environment.config.propertyOrNull("kaze.database.maximumPoolSize")?.getString()?.toInt() ?: 5,
            schemaMode = DatabaseSchemaMode.fromConfig(
                System.getenv("KAZE_DB_SCHEMA_MODE")
                    ?: System.getProperty("kaze.database.schema.mode")
                    ?: environment.config.propertyOrNull("kaze.database.schema.mode")?.getString(),
            ),
        )
    }

internal fun DatabaseConfig.createDataSource(): HikariDataSource =
    HikariDataSource(
        HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = this@createDataSource.jdbcUrl
            username = user.takeIf { it.isNotBlank() }
            password = this@createDataSource.password.takeIf { it.isNotBlank() }
            maximumPoolSize = this@createDataSource.maximumPoolSize
            minimumIdle = 0
            isReadOnly = false
            isAutoCommit = true
            connectionTimeout = 10_000
            idleTimeout = 60_000
            maxLifetime = 300_000
            keepaliveTime = 30_000
            poolName = "kaze-hikari"
            validate()
        },
    )

internal data class ParsedDatabaseConnection(
    val jdbcUrl: String,
    val user: String? = null,
    val password: String? = null,
)

internal fun parseDatabaseConnection(rawUrl: String): ParsedDatabaseConnection {
    val trimmed = rawUrl.trim()
    if (trimmed.startsWith("jdbc:postgresql://", ignoreCase = true)) {
        return ParsedDatabaseConnection(jdbcUrl = trimmed)
    }

    if (trimmed.startsWith("postgresql://", ignoreCase = true) || trimmed.startsWith("postgres://", ignoreCase = true)) {
        val normalizedUrl = when {
            trimmed.startsWith("postgres://", ignoreCase = true) ->
                "postgresql://${trimmed.removePrefix("postgres://")}"
            else -> trimmed
        }
        val uri = URI(normalizedUrl)
        val userInfo = uri.userInfo.orEmpty()
        val username = userInfo.substringBefore(':', missingDelimiterValue = "").ifBlank { null }?.urlDecode()
        val password = userInfo.substringAfter(':', missingDelimiterValue = "").ifBlank { null }?.urlDecode()
        val host = uri.host ?: error("DATABASE_URL is missing a host")
        val portSegment = if (uri.port > 0) ":${uri.port}" else ""
        val path = uri.rawPath?.takeIf { it.isNotBlank() } ?: "/kaze"
        val query = uri.rawQuery?.let { "?$it" }.orEmpty()

        return ParsedDatabaseConnection(
            jdbcUrl = "jdbc:postgresql://$host$portSegment$path$query",
            user = username,
            password = password,
        )
    }

    return ParsedDatabaseConnection(jdbcUrl = trimmed)
}

private fun String.urlDecode(): String =
    URLDecoder.decode(this, StandardCharsets.UTF_8)

internal enum class DatabaseSchemaMode {
    NONE,
    CREATE,
    DROP,
    CREATE_DROP,
    ;

    companion object {
        fun fromConfig(value: String?): DatabaseSchemaMode =
            when (value?.trim()?.lowercase()) {
                null, "", "none", "off", "false" -> NONE
                "create", "update" -> CREATE
                "drop" -> DROP
                "create-drop", "create_drop" -> CREATE_DROP
                else -> error(
                    "Unsupported database schema mode '$value'. Use none, create, drop, or create-drop.",
                )
            }
    }
}
